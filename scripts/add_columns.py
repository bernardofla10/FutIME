import os
import psycopg2
import sys

DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

def add_columns():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASSWORD
        )
        cur = conn.cursor()
        
        # Add image_url to jogadores
        print("Checking 'jogadores' table...")
        cur.execute("""
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name='jogadores' AND column_name='image_url'
        """)
        if not cur.fetchone():
            print("  Adding 'image_url' column to 'jogadores'...")
            cur.execute("ALTER TABLE jogadores ADD COLUMN image_url TEXT")
            conn.commit()
            print("  ✓ Added.")
        else:
            print("  ✓ 'image_url' already exists.")

        # Add image_url to clubes (optional but requested "tables")
        print("Checking 'clubes' table...")
        cur.execute("""
            SELECT column_name 
            FROM information_schema.columns 
            WHERE table_name='clubes' AND column_name='image_url'
        """)
        if not cur.fetchone():
            print("  Adding 'image_url' column to 'clubes'...")
            cur.execute("ALTER TABLE clubes ADD COLUMN image_url TEXT")
            conn.commit()
            print("  ✓ Added.")
        else:
            print("  ✓ 'image_url' already exists.")
            
        conn.close()
        print("\nDone.")
        
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    add_columns()
