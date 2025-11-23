#!/usr/bin/env python3
"""
Script to enrich club data from the Transfermarkt API.

This script:
1. Fetches all existing clubs from the local database
2. Searches for each club in the Transfermarkt API by name
3. Retrieves detailed club profiles
4. Downloads club images/logos to assets folder
5. Updates the database with additional information

Usage:
    python populate_clubs_transfermarkt.py [--dry-run] [--limit N]

Arguments:
    --dry-run: Preview changes without updating the database
    --limit N: Only process N clubs (for testing)
"""

import os
import sys
import argparse
import requests
import psycopg2
from datetime import datetime
import time
from typing import Optional, Dict, List, Any
from pathlib import Path
import re

# Configuration
TRANSFERMARKT_API_BASE = "http://localhost:8000/"
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

# Assets configuration
ASSETS_DIR = Path("frontend/assets")
CLUBS_IMG_DIR = ASSETS_DIR / "clubs"

# Rate limiting
REQUEST_DELAY = 1.0  # Seconds between requests to be respectful to the API


def sanitize_filename(name: str) -> str:
    """Convert a name to a safe filename."""
    # Remove special characters and replace spaces with underscores
    name = re.sub(r'[^\w\s-]', '', name)
    name = re.sub(r'[-\s]+', '_', name)
    return name.lower()


def normalize_name(name: str) -> str:
    """Normalize a name for better matching (remove accents, lowercase)."""
    import unicodedata
    # Remove accents
    name = unicodedata.normalize('NFD', name)
    name = ''.join(char for char in name if unicodedata.category(char) != 'Mn')
    return name.lower().strip()


class RateLimiter:
    """Simple rate limiter to avoid overwhelming the API."""
    
    def __init__(self, delay_seconds: float = 1.0):
        self.delay = delay_seconds
        self.last_request_time = 0
    
    def wait(self):
        """Wait if necessary to respect rate limits."""
        now = time.time()
        elapsed = now - self.last_request_time
        if elapsed < self.delay:
            sleep_time = self.delay - elapsed
            time.sleep(sleep_time)
        self.last_request_time = time.time()


class ClubEnricher:
    """Handles enrichment of club data from Transfermarkt API."""
    
    def __init__(self, dry_run: bool = False):
        self.dry_run = dry_run
        self.rate_limiter = RateLimiter(REQUEST_DELAY)
        self.stats = {
            'total': 0,
            'updated': 0,
            'not_found': 0,
            'errors': 0,
            'images_downloaded': 0
        }
        
        # Create assets directories
        self._create_assets_dirs()
        
        # Database connection
        try:
            self.conn = psycopg2.connect(
                host=DB_HOST,
                port=DB_PORT,
                dbname=DB_NAME,
                user=DB_USER,
                password=DB_PASSWORD
            )
            self.cur = self.conn.cursor()
            print("✓ Connected to database successfully")
        except Exception as e:
            print(f"✗ Error connecting to database: {e}")
            sys.exit(1)
    
    def __del__(self):
        """Clean up database connection."""
        if hasattr(self, 'conn'):
            self.conn.close()
    
    def _create_assets_dirs(self):
        """Create assets directories if they don't exist."""
        CLUBS_IMG_DIR.mkdir(parents=True, exist_ok=True)
        print(f"✓ Assets directories ready: {ASSETS_DIR}")
    
    def download_image(self, image_url: str, filename: str) -> Optional[str]:
        """
        Download an image from URL and save it to the clubs assets folder.
        
        Args:
            image_url: URL of the image to download
            filename: Name to save the file as (without extension)
            
        Returns:
            Relative path to saved image, or None if failed
        """
        if not image_url:
            return None
        
        try:
            # Download image
            response = requests.get(image_url, timeout=10)
            if response.status_code != 200:
                return None
            
            # Determine file extension from content-type or URL
            content_type = response.headers.get('content-type', '')
            if 'jpeg' in content_type or 'jpg' in content_type:
                ext = '.jpg'
            elif 'png' in content_type:
                ext = '.png'
            elif 'webp' in content_type:
                ext = '.webp'
            elif 'svg' in content_type:
                ext = '.svg'
            else:
                # Try to get from URL
                ext = os.path.splitext(image_url)[1] or '.png'
            
            # Save file
            safe_filename = sanitize_filename(filename) + ext
            file_path = CLUBS_IMG_DIR / safe_filename
            
            if not self.dry_run:
                with open(file_path, 'wb') as f:
                    f.write(response.content)
            
            # Return relative path from frontend root
            relative_path = f"assets/clubs/{safe_filename}"
            self.stats['images_downloaded'] += 1
            return relative_path
            
        except Exception as e:
            print(f"  ⚠ Error downloading image: {e}")
            return None
    
    def search_club(self, club_name: str) -> Optional[str]:
        """
        Search for a club by name in Transfermarkt API with fallback strategies.
        
        Args:
            club_name: The club's name to search for
            
        Returns:
            Club ID if found, None otherwise
        """
        # Try multiple search strategies
        search_names = [club_name]
        
        # Try normalized name (without accents)
        normalized = normalize_name(club_name)
        if normalized != club_name.lower():
            search_names.append(normalized)
        
        # Try common abbreviations/variations
        # e.g., "São Paulo" -> "Sao Paulo"
        if 'são' in club_name.lower():
            search_names.append(club_name.replace('São', 'Sao').replace('são', 'sao'))
        
        for search_name in search_names:
            self.rate_limiter.wait()
            
            try:
                url = f"{TRANSFERMARKT_API_BASE}/clubs/search/{search_name}"
                response = requests.get(url, timeout=10)
                
                if response.status_code != 200:
                    continue
                
                data = response.json()
                results = data.get('results', [])
                
                if not results:
                    continue
                
                # Return the first result's ID
                return results[0].get('id')
                
            except requests.exceptions.Timeout:
                continue
            except Exception as e:
                continue
        
        return None
    
    def get_club_profile(self, club_id: str) -> Optional[Dict[str, Any]]:
        """
        Get detailed club profile from Transfermarkt API.
        
        Args:
            club_id: The Transfermarkt club ID
            
        Returns:
            Club profile data if successful, None otherwise
        """
        self.rate_limiter.wait()
        
        try:
            url = f"{TRANSFERMARKT_API_BASE}/clubs/{club_id}/profile"
            response = requests.get(url, timeout=10)
            
            if response.status_code != 200:
                print(f"  ⚠ Profile API returned status {response.status_code}")
                return None
            
            return response.json()
            
        except requests.exceptions.Timeout:
            print(f"  ⚠ Timeout fetching profile for ID {club_id}")
            return None
        except Exception as e:
            print(f"  ✗ Error fetching profile for ID {club_id}: {e}")
            return None
    
    def update_club_in_db(self, club_id: int, image_url: Optional[str] = None, 
                          stadium_name: Optional[str] = None, founded: Optional[int] = None):
        """
        Update club's information in the database.
        
        Args:
            club_id: Database club ID
            image_url: Relative path to club logo (or None)
            stadium_name: Stadium name (or None)
            founded: Year founded (or None)
        """
        if self.dry_run:
            print(f"  [DRY RUN] Would update club {club_id} with image: {image_url}, stadium: {stadium_name}")
            return
        
        try:
            # Check which columns exist
            self.cur.execute("""
                SELECT column_name 
                FROM information_schema.columns 
                WHERE table_name='clubes' AND column_name IN ('logo_url', 'image_url', 'ano_fundacao')
            """)
            existing_columns = {row[0] for row in self.cur.fetchall()}
            
            # Build update query dynamically
            updates = []
            params = []
            
            if image_url and ('logo_url' in existing_columns or 'image_url' in existing_columns):
                col_name = 'logo_url' if 'logo_url' in existing_columns else 'image_url'
                updates.append(f"{col_name} = %s")
                params.append(image_url)
            
            if founded and 'ano_fundacao' in existing_columns:
                updates.append("ano_fundacao = %s")
                params.append(founded)
            
            if updates:
                params.append(club_id)
                query = f"UPDATE clubes SET {', '.join(updates)} WHERE id = %s"
                self.cur.execute(query, params)
                self.conn.commit()
            
        except Exception as e:
            print(f"  ✗ Error updating database: {e}")
            self.conn.rollback()
            raise
    
    def fetch_clubs_from_db(self, limit: Optional[int] = None) -> List[Dict]:
        """
        Fetch clubs from the database.
        
        Args:
            limit: Maximum number of clubs to fetch
            
        Returns:
            List of club dictionaries
        """
        query = """
            SELECT id, nome, sigla, cidade, pais
            FROM clubes
        """
        
        if limit:
            query += f" LIMIT {limit}"
        
        self.cur.execute(query)
        
        clubs = []
        for row in self.cur.fetchall():
            clubs.append({
                'id': row[0],
                'nome': row[1],
                'sigla': row[2],
                'cidade': row[3],
                'pais': row[4]
            })
        
        return clubs
    
    def enrich_club(self, club: Dict) -> bool:
        """
        Enrich a single club's data.
        
        Args:
            club: Club dictionary from database
            
        Returns:
            True if club was updated, False otherwise
        """
        club_name = club['nome']
        print(f"\n[{self.stats['total'] + 1}] Processing: {club_name}")
        
        if club['cidade']:
            print(f"  City: {club['cidade']}")
        
        # Search for club
        club_id = self.search_club(club_name)
        
        if not club_id:
            print(f"  ✗ Not found in Transfermarkt")
            self.stats['not_found'] += 1
            return False
        
        print(f"  ✓ Found in Transfermarkt (ID: {club_id})")
        
        # Get profile
        profile = self.get_club_profile(club_id)
        
        if not profile:
            print(f"  ✗ Could not fetch profile")
            self.stats['errors'] += 1
            return False
        
        # Extract data
        stadium_name = profile.get('stadiumName')
        founded_str = profile.get('foundedOn')
        founded_year = None
        
        if founded_str:
            try:
                founded_year = int(founded_str.split('-')[0])
                print(f"  ✓ Founded: {founded_year}")
            except:
                pass
        
        if stadium_name:
            print(f"  ✓ Stadium: {stadium_name}")
        
        # Download club logo
        image_url_db = None
        image_url_api = profile.get('image')
        if image_url_api:
            print(f"  ⬇ Downloading logo...")
            image_url_db = self.download_image(image_url_api, club['nome'])
            if image_url_db:
                print(f"  ✓ Logo saved: {image_url_db}")
            else:
                print(f"  ⚠ Failed to download logo")
        
        # Update database
        try:
            self.update_club_in_db(club['id'], image_url_db, stadium_name, founded_year)
            print(f"  ✓ Updated in database")
            self.stats['updated'] += 1
            return True
        except Exception as e:
            print(f"  ✗ Failed to update: {e}")
            self.stats['errors'] += 1
            return False
    
    def run(self, limit: Optional[int] = None):
        """
        Main execution method.
        
        Args:
            limit: Maximum number of clubs to process
        """
        print("\n" + "="*60)
        print("Transfermarkt Club Data Enrichment")
        print("="*60)
        
        if self.dry_run:
            print("\n⚠ DRY RUN MODE - No changes will be made to the database\n")
        
        # Fetch clubs
        print("\nFetching clubs from database...")
        clubs = self.fetch_clubs_from_db(limit)
        
        if not clubs:
            print("No clubs found to process.")
            return
        
        print(f"Found {len(clubs)} club(s) to process")
        
        # Process each club
        for club in clubs:
            self.stats['total'] += 1
            
            try:
                self.enrich_club(club)
            except KeyboardInterrupt:
                print("\n\n⚠ Interrupted by user")
                break
            except Exception as e:
                print(f"  ✗ Unexpected error: {e}")
                self.stats['errors'] += 1
        
        # Print summary
        self.print_summary()
    
    def print_summary(self):
        """Print execution summary."""
        print("\n" + "="*60)
        print("Summary")
        print("="*60)
        print(f"Total clubs processed:   {self.stats['total']}")
        print(f"Successfully updated:    {self.stats['updated']}")
        print(f"Logos downloaded:        {self.stats['images_downloaded']}")
        print(f"Not found:               {self.stats['not_found']}")
        print(f"Errors:                  {self.stats['errors']}")
        print("="*60 + "\n")


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(
        description='Enrich club data from Transfermarkt API'
    )
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Preview changes without updating the database'
    )
    parser.add_argument(
        '--limit',
        type=int,
        help='Maximum number of clubs to process'
    )
    
    args = parser.parse_args()
    
    # Check database password
    if not DB_PASSWORD:
        print("Error: FUTIME_DB_PASSWORD_DEPLOY environment variable not set")
        sys.exit(1)
    
    # Run enrichment
    enricher = ClubEnricher(dry_run=args.dry_run)
    enricher.run(limit=args.limit)


if __name__ == '__main__':
    main()
