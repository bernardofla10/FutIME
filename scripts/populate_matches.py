import os
import requests
import psycopg2
from datetime import datetime

# Configuration
API_KEY = "6cf4c32a1a9ea183d8244becb47b7030"
API_URL = "https://v3.football.api-sports.io/fixtures"
LEAGUE_ID = 71  # Brasileirão Série A
SEASONS = [2022, 2023, 2024, 2025]
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY")

if not DB_PASSWORD:
    print("Error: Environment variable FUTIME_DB_PASSWORD_DEPLOY is not set.")
    exit(1)

# Database Connection
try:
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )
    cur = conn.cursor()
    print("Connected to the database successfully.")
except Exception as e:
    print(f"Error connecting to database: {e}")
    exit(1)

def get_fixtures(season):
    headers = {
        "x-rapidapi-host": "v3.football.api-sports.io",
        "x-rapidapi-key": API_KEY
    }
    params = {
        "league": LEAGUE_ID,
        "season": season
    }
    response = requests.get(API_URL, headers=headers, params=params)
    if response.status_code == 200:
        return response.json().get("response", [])
    else:
        print(f"Error fetching fixtures for season {season}: {response.status_code} - {response.text}")
        return []

def get_or_create_stadium(venue_data):
    if not venue_data or not venue_data.get("name"):
        return None
    
    name = venue_data["name"]
    city = venue_data.get("city")
    
    # Check if exists
    cur.execute("SELECT id FROM estadios WHERE nome = %s", (name,))
    result = cur.fetchone()
    if result:
        return result[0]
    
    # Create
    print(f"Creating stadium: {name}")
    cur.execute(
        "INSERT INTO estadios (nome, cidade, estado, pais) VALUES (%s, %s, %s, %s) RETURNING id",
        (name, city, None, "Brasil") # Assuming Brasil for Brasileirão
    )
    return cur.fetchone()[0]

def get_or_create_club(team_data, stadium_id):
    name = team_data["name"]
    logo = team_data.get("logo")
    
    # Check if exists
    cur.execute("SELECT id FROM clubes WHERE nome = %s", (name,))
    result = cur.fetchone()
    if result:
        return result[0]
    
    # Create
    print(f"Creating club: {name}")
    # Note: Clube entity has 'sigla', 'cidade', 'pais', 'estadio_id'. 
    # API doesn't give city/country for team directly in fixture response usually, 
    # but we can leave null or infer.
    cur.execute(
        "INSERT INTO clubes (nome, sigla, cidade, pais, estadio_id) VALUES (%s, %s, %s, %s, %s) RETURNING id",
        (name, name[:3].upper(), None, "Brasil", stadium_id)
    )
    return cur.fetchone()[0]

def map_status_to_fase(round_str):
    # Simple mapping, can be improved
    if "Regular Season" in round_str:
        return "PONTOS_CORRIDOS"
    return "PONTOS_CORRIDOS" # Default for Brasileirão

def process_fixture(fixture):
    fixture_data = fixture["fixture"]
    league_data = fixture["league"]
    teams_data = fixture["teams"]
    goals_data = fixture["goals"]
    score_data = fixture["score"]
    
    # Stadium
    stadium_id = get_or_create_stadium(fixture_data.get("venue"))
    if not stadium_id:
        # Fallback or skip? Let's skip if no stadium, or maybe create a dummy?
        # For now, let's try to proceed only if we have stadium, or maybe allow null if DB allows (DB schema said nullable=false for estadio_id in Partida)
        # Wait, Partida.java says @JoinColumn(name = "estadio_id", nullable = false)
        # So we MUST have a stadium.
        if fixture_data.get("venue") and fixture_data["venue"].get("id"):
             # If API has ID but no name? Unlikely.
             pass
        else:
             # If no venue info, we can't insert Partida safely without violating constraint.
             # Let's check if we can find a default stadium or just skip.
             # print(f"Skipping fixture {fixture_data['id']} due to missing venue.")
             # return
             pass

    # If stadium_id is still None (e.g. venue name was null), we might need a placeholder.
    # Let's assume for now most have venues.
    if not stadium_id:
        # Create a "Unknown" stadium
        cur.execute("SELECT id FROM estadios WHERE nome = 'Desconhecido'")
        res = cur.fetchone()
        if res:
            stadium_id = res[0]
        else:
            cur.execute("INSERT INTO estadios (nome, cidade, estado, pais) VALUES ('Desconhecido', 'Desconhecido', NULL, 'Brasil') RETURNING id")
            stadium_id = cur.fetchone()[0]

    # Clubs
    # We need stadium for club creation too (as per Clube.java, though it is OneToOne, maybe nullable? 
    # Clube.java: @JoinColumn(name = "estadio_id", referencedColumnName = "id") -> doesn't say nullable=false explicitly in annotation but usually it is nullable unless specified.
    # Let's check DB schema if possible, but assuming nullable for Club's stadium is safer.
    # Wait, Clube.java has `private Estadio estadio;`
    # Let's pass None for club's stadium if we don't know it specifically belongs to them (fixture venue might be neutral).
    # Actually, `get_or_create_club` takes stadium_id. Let's pass None.
    
    home_club_id = get_or_create_club(teams_data["home"], None)
    away_club_id = get_or_create_club(teams_data["away"], None)
    
    # Date
    date_str = fixture_data["date"]
    # ISO format: 2020-02-06T14:00:00+00:00
    # Python 3.7+ handles this with fromisoformat if we remove the last colon in timezone or use dateutil.
    # Let's use a simple replace for the timezone if needed or just parsed.
    # Actually, psycopg2 handles datetime objects.
    try:
        match_date = datetime.fromisoformat(date_str.replace("Z", "+00:00"))
    except ValueError:
        # Handle +00:00 manually if needed or use dateutil
        match_date = date_str # Let postgres handle string?
    
    # Goals
    home_goals = goals_data["home"] if goals_data["home"] is not None else 0
    away_goals = goals_data["away"] if goals_data["away"] is not None else 0
    
    # Fase
    fase = map_status_to_fase(league_data["round"])
    
    # Insert Partida
    # Check if exists (by date and teams maybe? or API ID if we stored it. We didn't add API ID to Partida entity)
    # Let's check by teams and date approximately? Or just insert blindly?
    # User asked to "populate", implying potential duplicates if run multiple times?
    # "quero um script de execução unica" -> Single execution script.
    # So I should try to avoid duplicates if I can, but without API ID in DB, it's hard.
    # Let's check if a match between these two teams exists on this date.
    
    cur.execute("""
        SELECT id FROM partidas 
        WHERE clube_mandante_id = %s AND clube_visitante_id = %s AND data_hora = %s
    """, (home_club_id, away_club_id, match_date))
    
    if cur.fetchone():
        print(f"Match {teams_data['home']['name']} vs {teams_data['away']['name']} already exists.")
        return

    print(f"Inserting match: {teams_data['home']['name']} vs {teams_data['away']['name']}")
    cur.execute("""
        INSERT INTO partidas (
            clube_mandante_id, clube_visitante_id, estadio_id, fase, 
            gols_mandante, gols_visitante, data_hora, created_at, updated_at
        ) VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
    """, (
        home_club_id, away_club_id, stadium_id, fase, 
        home_goals, away_goals, match_date
    ))

def main():
    for season in SEASONS:
        print(f"Processing season {season}...")
        fixtures = get_fixtures(season)
        print(f"Found {len(fixtures)} fixtures.")
        for fixture in fixtures:
            process_fixture(fixture)
            conn.commit() # Commit after each or batch? Each is safer for debugging.
    
    conn.close()
    print("Done.")

if __name__ == "__main__":
    main()
