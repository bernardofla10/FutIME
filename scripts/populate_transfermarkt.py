#!/usr/bin/env python3
"""
Script to populate/update database with Transfermarkt data.
Iterates by PLAYER to update image and market value.

Features:
1. Fetches all players from DB.
2. Searches for each player by name on Transfermarkt.
3. Updates image_url and valor_de_mercado.
4. Downloads Player Images.

Usage:
    python populate_transfermarkt.py [--dry-run] [--limit N]
"""

import os
import sys
import argparse
import requests
import psycopg2
import time
from typing import Optional, Dict, List, Tuple
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

# Assets
ASSETS_DIR = Path("frontend/assets")
PLAYERS_IMG_DIR = ASSETS_DIR / "players"

# Rate limiting
REQUEST_DELAY = 1.0

def sanitize_filename(name: str) -> str:
    name = re.sub(r'[^\w\s-]', '', name)
    name = re.sub(r'[-\s]+', '_', name)
    return name.lower()

def normalize_name(name: str) -> str:
    if not name: return ""
    name = unicodedata.normalize('NFD', name)
    name = ''.join(char for char in name if unicodedata.category(char) != 'Mn')
    return name.lower().strip()

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

class TransfermarktPopulator:
    def __init__(self, dry_run: bool = False):
        self.dry_run = dry_run
        self.rate_limiter = RateLimiter(REQUEST_DELAY)
        self.stats = {
            'players_processed': 0,
            'players_updated': 0,
            'images_downloaded': 0,
            'errors': 0,
            'not_found': 0
        }
        
        self._create_assets_dirs()
        self.conn = self._connect_db()
        self.cur = self.conn.cursor()
        self._ensure_schema()

    def _connect_db(self):
        try:
            return psycopg2.connect(
                host=DB_HOST, port=DB_PORT, dbname=DB_NAME,
                user=DB_USER, password=DB_PASSWORD
            )
        except Exception as e:
            print(f"Error connecting to database: {e}")
            sys.exit(1)

    def _create_assets_dirs(self):
        PLAYERS_IMG_DIR.mkdir(parents=True, exist_ok=True)

    def _ensure_schema(self):
        """Ensure necessary columns exist."""
        try:
            # Check image_url in jogadores
            self.cur.execute("""
                SELECT column_name FROM information_schema.columns 
                WHERE table_name='jogadores' AND column_name='image_url'
            """)
            if not self.cur.fetchone():
                print("Adding 'image_url' column to 'jogadores'...")
                if not self.dry_run:
                    self.cur.execute("ALTER TABLE jogadores ADD COLUMN image_url TEXT")
                    self.conn.commit()
        except Exception as e:
            print(f"Schema check warning: {e}")
            self.conn.rollback()

    def download_image(self, url: str, filename: str, folder: Path) -> Optional[str]:
        if not url: return None
        try:
            res = requests.get(url, timeout=30)
            if res.status_code != 200: return None
            
            ext = '.jpg'
            if 'png' in res.headers.get('content-type', ''): ext = '.png'
            
            safe_name = sanitize_filename(filename) + ext
            path = folder / safe_name
            
            if not self.dry_run:
                with open(path, 'wb') as f:
                    f.write(res.content)
            
            self.stats['images_downloaded'] += 1
            return f"assets/{folder.name}/{safe_name}"
        except Exception as e:
            print(f"Error downloading image {url}: {e}")
            return None

    def search_player_tm(self, name: str) -> Optional[Dict]:
        self.rate_limiter.wait()
        try:
            # Search endpoint usually: /players/search/{query}
            url = f"{TRANSFERMARKT_API_BASE}/players/search/{name}"
            res = requests.get(url, timeout=30)
            if res.status_code != 200: return None
            
            results = res.json().get('results', [])
            if not results: return None
            
            # Return the first result
            return results[0]
        except Exception as e:
            print(f"Error searching player {name}: {e}")
            return None

    def get_all_db_players(self) -> List[Tuple]:
        """Fetch all players."""
        self.cur.execute("SELECT id, nome_completo, image_url, valor_de_mercado FROM jogadores ORDER BY id")
        return self.cur.fetchall()

    def process_player(self, player: Tuple):
        pid, name, current_img, current_val = player
        
        # Skip if already has both (optional optimization, maybe we want to update value?)
        # User said "muda completamente", implies re-running.
        # But let's print what we are doing.
        
        print(f"Processing: {name} (ID: {pid})")
        self.stats['players_processed'] += 1
        
        tm_player = self.search_player_tm(name)
        
        if not tm_player:
            print(f"  ✗ Not found in TM")
            self.stats['not_found'] += 1
            return

        tm_id = tm_player.get('id')
        tm_name = tm_player.get('name')
        market_val = tm_player.get('marketValue')
        
        # Fetch Profile for better image?
        # The search result might have image, but profile is better.
        # Let's check search result first.
        # Usually search result has 'image'.
        
        # Fetch profile to be sure and get high res image if possible
        prof_url = f"{TRANSFERMARKT_API_BASE}/players/{tm_id}/profile"
        self.rate_limiter.wait()
        prof_res = requests.get(prof_url)
        
        img_url = None
        if prof_res.status_code == 200:
            prof = prof_res.json()
            img_url = prof.get('imageUrl')
            # market_val might be better here too
            if not market_val:
                market_val = prof.get('marketValue')
        else:
            img_url = tm_player.get('image')

        # Download Image
        local_img = self.download_image(img_url, name, PLAYERS_IMG_DIR)
        
        # Update DB
        if local_img or market_val:
            print(f"  ✓ Updating {name} -> Val: {market_val}, Img: {local_img is not None}")
            if not self.dry_run:
                self.cur.execute("""
                    UPDATE jogadores 
                    SET valor_de_mercado = %s, image_url = COALESCE(%s, image_url)
                    WHERE id = %s
                """, (market_val, local_img, pid))
                self.stats['players_updated'] += 1
        else:
            print("  - No new data")

    def run(self, limit: Optional[int] = None):
        print("Starting Transfermarkt Population (Player Search Mode)...")
        if self.dry_run: print("⚠ DRY RUN MODE")
        
        players = self.get_all_db_players()
        print(f"Loaded {len(players)} players from DB.")
        
        if limit: players = players[:limit]
        
        for player in players:
            try:
                self.process_player(player)
                self.conn.commit() # Commit frequently
            except Exception as e:
                print(f"Error processing player {player[1]}: {e}")
                self.conn.rollback()
                self.stats['errors'] += 1
        
        print("\nSummary:")
        print(self.stats)
        self.conn.close()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--limit', type=int)
    args = parser.parse_args()
    
    populator = TransfermarktPopulator(dry_run=args.dry_run)
    populator.run(limit=args.limit)

if __name__ == "__main__":
    main()
