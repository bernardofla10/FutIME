#!/usr/bin/env python3
"""
Script to DELETE EVERYTHING from the database using TRUNCATE CASCADE.
This is the nuclear option to wipe all data.

Usage:
    python delete_all_players.py
"""

import os
import sys
import psycopg2

# Configuration
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

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

def delete_everything(conn):
    cur = conn.cursor()
    
    print("\n!!! WARNING: THIS WILL DELETE EVERYTHING (Nuclear Option) !!!")
    print("All tables will be truncated.")
    
    try:
        # Clear user references first to avoid issues
        print("Clearing user favorite teams...")
        try:
             cur.execute("UPDATE usuarios SET time_do_coracao_id = NULL")
             print(f"  Updated {cur.rowcount} users.")
        except Exception as e:
             print(f"  Warning: Could not update usuarios: {e}")
             conn.rollback()
             cur = conn.cursor()

        # Truncate main tables with CASCADE
        print("Truncating tables...")
        tables = [
            "competicoes", 
            "clubes", 
            "estadios", 
            "jogadores", 
            "partidas",
            "competicao_clube",
            "jogador_estatisticas_competicao",
            "jogador_estatistica_partida",
            "usuario_jogadores_observados"
        ]
        
        # Construct TRUNCATE command
        # TRUNCATE table1, table2, ... CASCADE;
        query = f"TRUNCATE {', '.join(tables)} CASCADE"
        cur.execute(query)
        
        conn.commit()
        print("\n✓ Successfully TRUNCATED all database content.")
        
    except Exception as e:
        print(f"\n✗ Error deleting data: {e}")
        conn.rollback()

def main():
    conn = connect_db()
    delete_everything(conn)
    conn.close()

if __name__ == "__main__":
    main()
