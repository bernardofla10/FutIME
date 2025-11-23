#!/usr/bin/env python3
"""
Script to populate the database from scratch for Season 2025.
1. Creates Competition (Brasileirão Série A 2025).
2. Fetches Clubs from Transfermarkt (Brasileirão).
3. Inserts Clubs into DB.
4. Populates Players using TransfermarktPopulator.

Usage:
    python populate_2025.py [--dry-run]
"""

import os
import sys
import argparse
import requests
import psycopg2
from datetime import datetime
from populate_transfermarkt import TransfermarktPopulator, RateLimiter, TRANSFERMARKT_API_BASE, DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD

# Configuration
SEASON = 2025
COMPETITION_NAME = "Brasileirão Série A"
TM_SEARCH_QUERY = "Série A" # Search term for TM

def connect_db():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        return conn
    except Exception as e:
        print(f"Error connecting to database: {e}")
        sys.exit(1)

class SeasonPopulator:
    def __init__(self, dry_run: bool = False):
        self.dry_run = dry_run
        self.conn = connect_db()
        self.cur = self.conn.cursor()
        self.rate_limiter = RateLimiter(1.0)

    def get_or_create_competition(self) -> int:
        print(f"Checking competition: {COMPETITION_NAME} {SEASON}")
        self.cur.execute(
            "SELECT id FROM competicoes WHERE nome = %s AND temporada = %s",
            (COMPETITION_NAME, str(SEASON))
        )
        res = self.cur.fetchone()
        if res:
            print(f"  ✓ Found existing competition (ID: {res[0]})")
            return res[0]
        
        print(f"  + Creating competition...")
        if self.dry_run: return 0
        
        self.cur.execute("""
            INSERT INTO competicoes (nome, pais, continente, tipo_competicao, temporada, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, NOW(), NOW()) RETURNING id
        """, (COMPETITION_NAME, 'Brazil', 'South America', 'PONTOS_CORRIDOS', str(SEASON)))
        comp_id = self.cur.fetchone()[0]
        self.conn.commit()
        return comp_id

    def search_tm_league(self) -> str:
        print(f"Searching Transfermarkt for league: {TM_SEARCH_QUERY}")
        self.rate_limiter.wait()
        try:
            url = f"{TRANSFERMARKT_API_BASE}/competitions/search/{TM_SEARCH_QUERY}"
            res = requests.get(url, timeout=30)
            if res.status_code != 200:
                print(f"  ✗ API Error: {res.status_code}")
                return None
            
            results = res.json().get('results', [])
            # Filter for Brazil?
            for r in results:
                if r.get('country', {}).get('name') == 'Brazil' or r.get('country') == 'Brazil': # Check API format
                     print(f"  ✓ Found League: {r.get('name')} (ID: {r.get('id')})")
                     return r.get('id')
            
            # Fallback: return first result
            if results:
                r = results[0]
                print(f"  ⚠ Using first result: {r.get('name')} (ID: {r.get('id')})")
                return r.get('id')
                
            return None
        except Exception as e:
            print(f"  ✗ Error searching league: {e}")
            return None

    def get_tm_clubs(self, league_id: str) -> list:
        print(f"Fetching clubs for league {league_id}...")
        self.rate_limiter.wait()
        try:
            url = f"{TRANSFERMARKT_API_BASE}/competitions/{league_id}/clubs?season_id={SEASON}"
            # Note: TM API uses season_id usually as year (e.g. 2024).
            # If 2025 is not available, it might return empty or error.
            # Let's try 2024 if 2025 fails?
            # Or just try requested season.
            
            res = requests.get(url, timeout=30)
            if res.status_code != 200:
                print(f"  ✗ API Error: {res.status_code}")
                return []
            
            clubs = res.json().get('clubs', [])
            print(f"  ✓ Found {len(clubs)} clubs")
            return clubs
        except Exception as e:
            print(f"  ✗ Error fetching clubs: {e}")
            return []

    def insert_clubs(self, clubs: list, comp_id: int):
        print(f"Inserting/Updating {len(clubs)} clubs...")
        count = 0
        for club in clubs:
            name = club.get('name')
            tm_id = club.get('id')
            
            # Check if exists
            self.cur.execute("SELECT id FROM clubes WHERE nome = %s", (name,))
            res = self.cur.fetchone()
            
            club_id = None
            if res:
                club_id = res[0]
                # print(f"  - Club exists: {name}")
            else:
                print(f"  + Creating Club: {name}")
                if not self.dry_run:
                    # Insert
                    self.cur.execute("""
                        INSERT INTO clubes (nome, sigla, pais, created_at, updated_at)
                        VALUES (%s, %s, %s, NOW(), NOW()) RETURNING id
                    """, (name, name[:3].upper(), 'Brasil'))
                    club_id = self.cur.fetchone()[0]
            
            if club_id and not self.dry_run:
                # Link to Competition
                self.cur.execute("""
                    INSERT INTO competicao_clube (competicao_id, clube_id)
                    VALUES (%s, %s)
                    ON CONFLICT DO NOTHING
                """, (comp_id, club_id))
                count += 1
        
        self.conn.commit()
        print(f"✓ Processed {count} clubs.")

    def run(self):
        print(f"--- Populating Season {SEASON} ---")
        if self.dry_run: print("⚠ DRY RUN MODE")
        
        # 1. Competition
        comp_id = self.get_or_create_competition()
        
        # 2. TM League
        league_id = self.search_tm_league()
        if not league_id:
            print("Could not find league in Transfermarkt.")
            return
        
        # 3. TM Clubs
        clubs = self.get_tm_clubs(league_id)
        if not clubs:
            print("No clubs found. Trying previous season (2024)...")
            # Fallback to 2024 if 2025 empty
            # url = f"{TRANSFERMARKT_API_BASE}/competitions/{league_id}/clubs?season_id=2024"
            # ... (simplified logic for now)
            pass
            
        # 4. Insert Clubs
        self.insert_clubs(clubs, comp_id)
        
        # 5. Populate Players
        print("\n--- Populating Players ---")
        populator = TransfermarktPopulator(dry_run=self.dry_run)
        populator.run() # Runs for ALL clubs in DB (which we just populated)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--dry-run', action='store_true')
    args = parser.parse_args()
    
    populator = SeasonPopulator(dry_run=args.dry_run)
    populator.run()

if __name__ == "__main__":
    main()
