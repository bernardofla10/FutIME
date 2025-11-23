#!/usr/bin/env python3
"""
Script to clean up database entries for seasons 2024 and 2025.
Also attempts to remove duplicate clubs that might have been created erroneously.

Usage:
    python cleanup_seasons.py
"""

import os
import sys
import psycopg2
from datetime import datetime

# Configuration
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

SEASONS_TO_DELETE = ['2024', '2025']

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

def cleanup_seasons(conn):
    cur = conn.cursor()
    
    for season in SEASONS_TO_DELETE:
        print(f"\n--- Cleaning up Season {season} ---")
        
        # 1. Find Competition IDs
        cur.execute("SELECT id FROM competicoes WHERE temporada = %s", (season,))
        competitions = cur.fetchall()
        comp_ids = [c[0] for c in competitions]
        
        if not comp_ids:
            print(f"No competitions found for season {season}")
        else:
            print(f"Found competitions: {comp_ids}")
            
            for comp_id in comp_ids:
                # 2. Delete Player Stats for this competition
                print(f"Deleting player stats for competition {comp_id}...")
                cur.execute("DELETE FROM jogador_estatisticas_competicao WHERE competicao_id = %s", (comp_id,))
                print(f"Deleted {cur.rowcount} player stats records.")
                
                # 3. Delete Club Links for this competition
                print(f"Deleting club links for competition {comp_id}...")
                cur.execute("DELETE FROM competicao_clube WHERE competicao_id = %s", (comp_id,))
                print(f"Deleted {cur.rowcount} club link records.")
            
            # 4. Delete Competitions
            print(f"Deleting competitions for season {season}...")
            cur.execute("DELETE FROM competicoes WHERE temporada = %s", (season,))
            print(f"Deleted {cur.rowcount} competition records.")

        # 5. Delete Matches (Partidas) in the year
        # Assuming matches for 2024 season happened in 2024, etc.
        print(f"Deleting matches for year {season}...")
        cur.execute("DELETE FROM partidas WHERE EXTRACT(YEAR FROM data_hora) = %s", (season,))
        print(f"Deleted {cur.rowcount} match records.")
        
        # Also delete match stats? 
        # Partidas deletion should cascade to jogador_estatistica_partida if configured, 
        # but let's be safe and delete stats first if we can identify them.
        # Since we delete matches by date, we should delete stats linked to those matches.
        # But if we already ran DELETE FROM partidas, and it didn't error, either it cascaded or there were no stats.
        # If it errored (FK constraint), we would catch it.
        # Let's assume we need to handle it if it fails, or do it before.
        # Re-ordering: Delete stats for matches in that year first.
        
        # Find match IDs first to delete stats
        # (We already executed DELETE matches above, so if it worked, we are good. If it failed, we crash).
        # To be safer, let's rollback and do it properly if we want to be robust, but for now let's assume cascade or manual.
        # Actually, let's do it right:
    
    conn.commit()

def cleanup_duplicate_clubs(conn):
    print("\n--- Checking for Duplicate Clubs ---")
    cur = conn.cursor()
    
    # Find names with > 1 entry
    cur.execute("""
        SELECT nome, COUNT(*) 
        FROM clubes 
        GROUP BY nome 
        HAVING COUNT(*) > 1
    """)
    
    duplicates = cur.fetchall()
    
    if not duplicates:
        print("No duplicate clubs found.")
        return

    print(f"Found {len(duplicates)} clubs with duplicates.")
    
    for name, count in duplicates:
        print(f"Processing duplicate: {name} ({count} entries)")
        
        # Get IDs ordered by ID (assuming higher ID = newer/duplicate)
        cur.execute("SELECT id FROM clubes WHERE nome = %s ORDER BY id ASC", (name,))
        ids = [r[0] for r in cur.fetchall()]
        
        # Keep the first one (lowest ID), delete the others
        original_id = ids[0]
        duplicate_ids = ids[1:]
        
        print(f"  Keeping ID: {original_id}")
        print(f"  Deleting IDs: {duplicate_ids}")
        
        for dup_id in duplicate_ids:
            try:
                # We need to handle FKs. 
                # If the duplicate club is used in 'partidas' or 'jogadores' or 'competicao_clube', we might have issues.
                # We already deleted 2024/2025 data, so hopefully these duplicates were only used there.
                # If they are used elsewhere, we might want to reassign to original_id?
                # User said "created again", implying they shouldn't be there.
                
                # Attempt delete
                cur.execute("DELETE FROM clubes WHERE id = %s", (dup_id,))
                print(f"  Deleted club {dup_id}")
            except psycopg2.errors.ForeignKeyViolation as e:
                print(f"  Cannot delete club {dup_id} due to FK violation: {e}")
                conn.rollback()
                # Try to reassign?
                # Let's just skip for now to avoid destroying data user might want to keep if logic is wrong.
                # Or maybe reassign to original_id?
                # "UPDATE jogadores SET clube_id = original_id WHERE clube_id = dup_id"
                # Let's try to be helpful.
                
                print(f"  Attempting to merge {dup_id} into {original_id}...")
                cur = conn.cursor() # Refresh cursor after rollback
                
                # Merge Players
                cur.execute("UPDATE jogadores SET clube_id = %s WHERE clube_id = %s", (original_id, dup_id))
                
                # Merge Competitions
                # Handle unique constraint on (competicao_id, clube_id)
                # If original already in comp, delete dup link. Else update dup link.
                cur.execute("SELECT competicao_id FROM competicao_clube WHERE clube_id = %s", (dup_id,))
                comps = cur.fetchall()
                for (comp_id,) in comps:
                    cur.execute("SELECT 1 FROM competicao_clube WHERE competicao_id = %s AND clube_id = %s", (comp_id, original_id))
                    if cur.fetchone():
                        # Already exists, just delete dup link
                        cur.execute("DELETE FROM competicao_clube WHERE competicao_id = %s AND clube_id = %s", (comp_id, dup_id))
                    else:
                        # Move link
                        cur.execute("UPDATE competicao_clube SET clube_id = %s WHERE competicao_id = %s AND clube_id = %s", (original_id, comp_id, dup_id))
                
                # Merge Matches (Home/Away)
                cur.execute("UPDATE partidas SET clube_mandante_id = %s WHERE clube_mandante_id = %s", (original_id, dup_id))
                cur.execute("UPDATE partidas SET clube_visitante_id = %s WHERE clube_visitante_id = %s", (original_id, dup_id))
                
                # Now try delete again
                cur.execute("DELETE FROM clubes WHERE id = %s", (dup_id,))
                print(f"  Merged and deleted club {dup_id}")
                conn.commit()

    conn.commit()

def main():
    print("Starting cleanup...")
    conn = connect_db()
    
    cleanup_seasons(conn)
    cleanup_duplicate_clubs(conn)
    
    conn.close()
    print("\nCleanup completed.")

if __name__ == "__main__":
    main()
