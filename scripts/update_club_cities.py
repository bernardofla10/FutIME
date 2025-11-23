import os
import psycopg2

# Database Configuration
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

try:
    # Connect to database
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )
    cur = conn.cursor()
    print("Connected to database successfully.")
    
    # Update clubs to have the same city as their stadium
    print("Updating club cities...")
    cur.execute("""
        UPDATE clubes c
        SET cidade = e.cidade
        FROM estadios e
        WHERE c.estadio_id = e.id
          AND (c.cidade IS NULL OR c.cidade != e.cidade)
    """)
    
    rows_affected = cur.rowcount
    conn.commit()
    
    print(f"Successfully updated {rows_affected} club(s) with their stadium cities.")
    
    # Show the updated clubs
    cur.execute("""
        SELECT c.nome, c.cidade, e.nome as estadio, e.cidade as cidade_estadio
        FROM clubes c
        LEFT JOIN estadios e ON c.estadio_id = e.id
        ORDER BY c.nome
    """)
    
    print("\nClubs and their cities:")
    print("-" * 80)
    for row in cur.fetchall():
        print(f"{row[0]:25} | Cidade: {row[1] or 'N/A':30} | Estádio: {row[2] or 'N/A'}")
    
    cur.close()
    conn.close()
    print("\nDone!")
    
except Exception as e:
    print(f"Error: {e}")
