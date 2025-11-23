import os
import psycopg2

# Database Configuration
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

try:
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )
    cur = conn.cursor()
    
    # Check Brasileirão teams
    cur.execute("""
        SELECT c.nome, c.cidade, e.nome, e.cidade
        FROM clubes c
        LEFT JOIN estadios e ON c.estadio_id = e.id
        WHERE c.nome IN ('Bahia', 'Internacional', 'Botafogo', 'Palmeiras', 
                         'Flamengo', 'São Paulo', 'Corinthians', 'Atlético Mineiro')
        ORDER BY c.nome
    """)
    
    for row in cur.fetchall():
        clube = row[0]
        cidade_clube = row[1] or "SEM CIDADE"
        estadio = row[2] or "SEM ESTADIO"
        cidade_estadio = row[3] or "SEM CIDADE"
        print(f"{clube:20} | Clube: {cidade_clube:30} | Estadio: {estadio:30} ({cidade_estadio})")
    
    cur.close()
    conn.close()
    
except Exception as e:
    print(f"Error: {e}")
