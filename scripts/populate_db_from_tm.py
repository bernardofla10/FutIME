#!/usr/bin/env python3
"""
Script to populate the database (Competitions, Clubs, Stadiums, Players) 
from Transfermarkt API for seasons 2024 and 2025.

Mapping:
- DB Season 2024 -> TM Season 2023
- DB Season 2025 -> TM Season 2024

Usage:
    python populate_db_from_tm.py [--dry-run] [--limit N]
"""

import os
import sys
import argparse
import requests
import psycopg2
from datetime import datetime
import time
from typing import Optional, Dict, List, Any, Tuple
from pathlib import Path
import re
import unicodedata

# Configuration
TRANSFERMARKT_API_BASE = "http://localhost:8000"
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

# Assets configuration
ASSETS_DIR = Path("frontend/assets")
PLAYERS_IMG_DIR = ASSETS_DIR / "players"
CLUBS_IMG_DIR = ASSETS_DIR / "clubs"

# Rate limiting
REQUEST_DELAY = 1.0

# Season Mapping
SEASONS_MAP = {
    2024: "2023",
    2025: "2024"
}

COMPETITION_TM_ID = "BRA1" # Brasileirão Série A
COMPETITION_NAME = "Brasileirão Série A"

def sanitize_filename(name: str) -> str:
    """Convert a name to a safe filename."""
    name = re.sub(r'[^\w\s-]', '', name)
    name = re.sub(r'[-\s]+', '_', name)
    return name.lower()

class RateLimiter:
    def __init__(self, delay_seconds: float = 1.0):
        self.delay = delay_seconds
        self.last_request_time = 0
    
    def wait(self):
        now = time.time()
        elapsed = now - self.last_request_time
        if elapsed < self.delay:
            time.sleep(self.delay - elapsed)
        self.last_request_time = time.time()

class DatabasePopulator:
    def __init__(self, dry_run: bool = False):
        self.dry_run = dry_run
        self.rate_limiter = RateLimiter(REQUEST_DELAY)
        self.stats = {
            'clubs_created': 0,
            'players_created': 0,
            'players_updated': 0,
            'images_downloaded': 0,
            'errors': 0
        }
        self.has_image_column = False
        
        self._create_assets_dirs()
        
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
            
            # Check for image_url column
            self.cur.execute("""
                SELECT column_name 
                FROM information_schema.columns 
                WHERE table_name='jogadores' AND column_name='image_url'
            """)
            self.has_image_column = self.cur.fetchone() is not None
            if not self.has_image_column:
                print("⚠ 'image_url' column missing in 'jogadores' table. Images will not be saved to DB.")
                
        except Exception as e:
            print(f"✗ Error connecting to database: {e}")
            sys.exit(1)

    def __del__(self):
        if hasattr(self, 'conn'):
            self.conn.close()

    def _create_assets_dirs(self):
        PLAYERS_IMG_DIR.mkdir(parents=True, exist_ok=True)
        CLUBS_IMG_DIR.mkdir(parents=True, exist_ok=True)

    def download_image(self, image_url: str, filename: str, subfolder: Path) -> Optional[str]:
        if not image_url: return None
        try:
            response = requests.get(image_url, timeout=10)
            if response.status_code != 200: return None
            
            ext = os.path.splitext(image_url)[1] or '.jpg'
            if '?' in ext: ext = ext.split('?')[0]
            
            safe_filename = sanitize_filename(filename) + ext
            file_path = subfolder / safe_filename
            
            if not self.dry_run:
                with open(file_path, 'wb') as f:
                    f.write(response.content)
            
            self.stats['images_downloaded'] += 1
            return f"assets/{subfolder.name}/{safe_filename}"
        except Exception as e:
            print(f"  ⚠ Error downloading image: {e}")
            return None

    def get_or_create_competition(self, db_season: int) -> int:
        try:
            self.cur.execute(
                "SELECT id FROM competicoes WHERE nome = %s AND temporada = %s", 
                (COMPETITION_NAME, str(db_season))
            )
            result = self.cur.fetchone()
            if result:
                return result[0]
            
            if self.dry_run:
                print(f"  [DRY RUN] Would create competition: {COMPETITION_NAME} ({db_season})")
                return 0

            print(f"  Creating competition: {COMPETITION_NAME} ({db_season})")
            self.cur.execute("""
                INSERT INTO competicoes (nome, pais, continente, tipo_competicao, temporada, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW()) RETURNING id
            """, (COMPETITION_NAME, 'BRA', 'South America', 'PONTOS_CORRIDOS', str(db_season)))
            comp_id = self.cur.fetchone()[0]
            self.conn.commit()
            return comp_id
        except Exception as e:
            print(f"  ✗ Error creating competition: {e}")
            self.conn.rollback()
            return 0

    def get_or_create_stadium(self, stadium_name: str) -> Optional[int]:
        if not stadium_name: return None
        
        try:
            self.cur.execute("SELECT id FROM estadios WHERE nome = %s", (stadium_name,))
            result = self.cur.fetchone()
            if result:
                return result[0]
                
            if self.dry_run:
                print(f"    [DRY RUN] Would create stadium: {stadium_name}")
                return 0
                
            print(f"    Creating stadium: {stadium_name}")
            # Changed 'Brazil' to 'BRA' to fit varchar(3)
            self.cur.execute(
                "INSERT INTO estadios (nome, cidade, pais) VALUES (%s, %s, %s) RETURNING id",
                (stadium_name, "Unknown", "BRA")
            )
            stadium_id = self.cur.fetchone()[0]
            self.conn.commit()
            return stadium_id
        except Exception as e:
            print(f"    ✗ Error creating stadium: {e}")
            self.conn.rollback()
            return None

    def get_or_create_club(self, tm_club: Dict, stadium_id: Optional[int]) -> int:
        name = tm_club['name']
        
        try:
            self.cur.execute("SELECT id FROM clubes WHERE nome = %s", (name,))
            result = self.cur.fetchone()
            if result:
                return result[0]
                
            if self.dry_run:
                print(f"    [DRY RUN] Would create club: {name}")
                return 0
                
            print(f"    Creating club: {name}")
            
            # Ensure sigla is 3 chars
            sigla = name[:3].upper()
            
            try:
                self.cur.execute(
                    "INSERT INTO clubes (nome, sigla, cidade, pais, estadio_id) VALUES (%s, %s, %s, %s, %s) RETURNING id",
                    (name, sigla, None, "BRA", stadium_id)
                )
                club_id = self.cur.fetchone()[0]
                self.conn.commit()
                self.stats['clubs_created'] += 1
                return club_id
            except psycopg2.errors.UniqueViolation:
                self.conn.rollback()
                print(f"    ⚠ Stadium {stadium_id} already assigned. Creating {name} without stadium.")
                self.cur.execute(
                    "INSERT INTO clubes (nome, sigla, cidade, pais, estadio_id) VALUES (%s, %s, %s, %s, NULL) RETURNING id",
                    (name, sigla, None, "BRA")
                )
                club_id = self.cur.fetchone()[0]
                self.conn.commit()
                self.stats['clubs_created'] += 1
                return club_id
                
        except Exception as e:
            print(f"    ✗ Error creating club {name}: {e}")
            self.conn.rollback()
            return 0

    def link_club_to_competition(self, comp_id: int, club_id: int):
        if self.dry_run or comp_id == 0 or club_id == 0: return
        
        try:
            self.cur.execute(
                "SELECT 1 FROM competicao_clube WHERE competicao_id = %s AND clube_id = %s", 
                (comp_id, club_id)
            )
            if not self.cur.fetchone():
                self.cur.execute(
                    "INSERT INTO competicao_clube (competicao_id, clube_id) VALUES (%s, %s)", 
                    (comp_id, club_id)
                )
                self.conn.commit()
        except Exception as e:
            print(f"    ✗ Error linking club to competition: {e}")
            self.conn.rollback()

    def get_or_create_player(self, tm_player: Dict, club_id: int) -> int:
        name = tm_player['name']
        
        try:
            self.cur.execute("SELECT id FROM jogadores WHERE nome_completo = %s", (name,))
            result = self.cur.fetchone()
            
            if result:
                if not self.dry_run:
                    self.cur.execute("UPDATE jogadores SET clube_id = %s WHERE id = %s", (club_id, result[0]))
                    self.conn.commit()
                return result[0]
                
            if self.dry_run:
                print(f"      [DRY RUN] Would create player: {name}")
                return 0
                
            self.cur.execute("""
                INSERT INTO jogadores (nome_completo, apelido, posicao, clube_id, valor_de_mercado)
                VALUES (%s, %s, %s, %s, %s) RETURNING id
            """, (
                name,
                name.split()[0],
                tm_player.get('position'),
                club_id,
                tm_player.get('marketValue')
            ))
            player_id = self.cur.fetchone()[0]
            self.conn.commit()
            self.stats['players_created'] += 1
            return player_id
        except Exception as e:
            print(f"      ✗ Error creating player {name}: {e}")
            self.conn.rollback()
            return 0

    def update_player_details(self, player_id: int, tm_player_id: str, current_mv: Optional[int]):
        if self.dry_run or player_id == 0: return
        
        self.rate_limiter.wait()
        try:
            url = f"{TRANSFERMARKT_API_BASE}/players/{tm_player_id}/profile"
            resp = requests.get(url, timeout=10)
            if resp.status_code != 200: return
            
            profile = resp.json()
            image_url_api = profile.get('imageUrl')
            
            image_path = None
            if image_url_api:
                image_path = self.download_image(
                    image_url_api, 
                    profile.get('name', 'player'), 
                    PLAYERS_IMG_DIR
                )
            
            # Update DB
            if self.has_image_column:
                self.cur.execute("""
                    UPDATE jogadores 
                    SET image_url = COALESCE(%s, image_url),
                        valor_de_mercado = COALESCE(%s, valor_de_mercado),
                        data_nascimento = %s
                    WHERE id = %s
                """, (
                    image_path, 
                    current_mv, 
                    profile.get('dateOfBirth'), 
                    player_id
                ))
            else:
                self.cur.execute("""
                    UPDATE jogadores 
                    SET valor_de_mercado = COALESCE(%s, valor_de_mercado),
                        data_nascimento = %s
                    WHERE id = %s
                """, (
                    current_mv, 
                    profile.get('dateOfBirth'), 
                    player_id
                ))
            self.conn.commit()
            self.stats['players_updated'] += 1
            
        except Exception as e:
            print(f"      ⚠ Error updating player details: {e}")
            self.conn.rollback()

    def process_season(self, db_season: int, tm_season: str, limit: Optional[int]):
        print(f"\n--- Processing Season {db_season} (TM: {tm_season}) ---")
        
        comp_id = self.get_or_create_competition(db_season)
        if not self.dry_run and comp_id == 0:
            print("  ✗ Could not get/create competition. Skipping season.")
            return
        
        # Fetch Clubs
        self.rate_limiter.wait()
        url = f"{TRANSFERMARKT_API_BASE}/competitions/{COMPETITION_TM_ID}/clubs?season_id={tm_season}"
        try:
            resp = requests.get(url, timeout=20) # Increased timeout
            if resp.status_code != 200:
                print(f"✗ Error fetching clubs: {resp.status_code}")
                return
            
            clubs_data = resp.json().get('clubs', [])
        except Exception as e:
            print(f"✗ Exception fetching clubs: {e}")
            return

        if limit:
            clubs_data = clubs_data[:limit]

        print(f"Found {len(clubs_data)} clubs.")
        
        for i, club_item in enumerate(clubs_data):
            print(f"\n[{i+1}/{len(clubs_data)}] Club: {club_item['name']}")
            
            # Get Club Profile for Stadium
            stadium_id = None
            self.rate_limiter.wait()
            try:
                p_url = f"{TRANSFERMARKT_API_BASE}/clubs/{club_item['id']}/profile"
                p_resp = requests.get(p_url, timeout=10)
                if p_resp.status_code == 200:
                    profile = p_resp.json()
                    stadium_name = profile.get('stadiumName')
                    stadium_id = self.get_or_create_stadium(stadium_name)
            except Exception as e:
                print(f"  ⚠ Error fetching club profile: {e}")

            # Create Club
            club_id = self.get_or_create_club(club_item, stadium_id)
            
            if not self.dry_run and club_id == 0:
                print(f"  ⚠ Skipping squad for {club_item['name']} (Club creation failed)")
                continue
                
            self.link_club_to_competition(comp_id, club_id)
            
            # Fetch Squad
            self.rate_limiter.wait()
            try:
                s_url = f"{TRANSFERMARKT_API_BASE}/clubs/{club_item['id']}/players?season_id={tm_season}"
                s_resp = requests.get(s_url, timeout=10)
                if s_resp.status_code != 200:
                    print(f"  ⚠ Error fetching squad: {s_resp.status_code}")
                    continue
                    
                squad = s_resp.json().get('players', [])
                print(f"  Squad size: {len(squad)}")
                
                for p_item in squad:
                    player_id = self.get_or_create_player(p_item, club_id)
                    
                    # Update details (Image, etc)
                    if not self.dry_run and player_id:
                        self.update_player_details(player_id, p_item['id'], p_item.get('marketValue'))
                        
            except Exception as e:
                print(f"  ✗ Error processing squad: {e}")
    def run(self, limit: Optional[int] = None):
        print("="*60)
        print("Full DB Population from Transfermarkt")
        print("="*60)
        if self.dry_run:
            print("⚠ DRY RUN MODE")
            
        for db_season, tm_season in SEASONS_MAP.items():
            self.process_season(db_season, tm_season, limit)
            
        print("\n" + "="*60)
        print("Summary")
        print(f"Clubs Created: {self.stats['clubs_created']}")
        print(f"Players Created: {self.stats['players_created']}")
        print(f"Players Updated: {self.stats['players_updated']}")
        print(f"Images Downloaded: {self.stats['images_downloaded']}")
        print("="*60)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--limit', type=int, help='Limit clubs per season')
    args = parser.parse_args()
    
    if not DB_PASSWORD:
        print("Error: DB Password not set")
        sys.exit(1)
        
    populator = DatabasePopulator(dry_run=args.dry_run)
    populator.run(limit=args.limit)

if __name__ == '__main__':
    main()
