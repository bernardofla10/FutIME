import os
import requests
import psycopg2
from datetime import datetime
import time
from datetime import datetime, timedelta

class RateLimiter:
    def __init__(self, requests_per_minute=10):
        self.delay = 60.0 / requests_per_minute
        self.last_request_time = 0

    def wait(self):
        now = time.time()
        elapsed = now - self.last_request_time
        if elapsed < self.delay:
            sleep_time = self.delay - elapsed
            print(f"Rate limit: sleeping for {sleep_time:.2f}s...")
            time.sleep(sleep_time)
        self.last_request_time = time.time()

rate_limiter = RateLimiter(requests_per_minute=10)

# Configuration
API_KEY = "6cf4c32a1a9ea183d8244becb47b7030"
API_BASE_URL = "https://v3.football.api-sports.io"
LEAGUE_ID = 71  # Brasileirão Série A
SEASONS = [2023]
DB_HOST = "aws-1-sa-east-1.pooler.supabase.com"
DB_PORT = "5432"
DB_NAME = "futime_dbDEPLOY"
DB_USER = "postgres.oeyzjfldgaiaeotwcemn"
DB_PASSWORD = os.environ.get("FUTIME_DB_PASSWORD_DEPLOY", "juSQiPZNCRY10409")

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

def make_request(endpoint, params=None):
    headers = {
        "x-rapidapi-host": "v3.football.api-sports.io",
        "x-rapidapi-key": API_KEY
    }
    url = f"{API_BASE_URL}/{endpoint}"
    url = f"{API_BASE_URL}/{endpoint}"
    rate_limiter.wait()
    try:
        response = requests.get(url, headers=headers, params=params)
        if response.status_code == 200:
            data = response.json().get("response", [])
            if not data:
                print(f"Debug: Request to {url} with params {params} returned empty list. Full response: {response.json()}")
            return data
        else:
            print(f"Error fetching {endpoint}: {response.status_code} - {response.text}")
            return []
    except Exception as e:
        print(f"Exception during request to {endpoint}: {e}")
        return []

def get_or_create_competicao(season):
    # Check if exists
    cur.execute("SELECT id FROM competicoes WHERE nome = %s AND temporada = %s", ('Brasileirão Série A', str(season)))
    result = cur.fetchone()
    if result:
        return result[0]
    
    print(f"Creating competition: Brasileirão Série A - {season}")
    cur.execute("""
        INSERT INTO competicoes (nome, pais, continente, tipo_competicao, temporada, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, NOW(), NOW()) RETURNING id
    """, ('Brasileirão Série A', 'Brazil', 'South America', 'PONTOS_CORRIDOS', str(season)))
    comp_id = cur.fetchone()[0]
    conn.commit()
    return comp_id

def get_or_create_stadium(venue_data):
    if not venue_data or not venue_data.get("name"):
        return None
    
    name = venue_data["name"]
    city = venue_data.get("city")
    
    cur.execute("SELECT id FROM estadios WHERE nome = %s", (name,))
    result = cur.fetchone()
    if result:
        return result[0]
    
    print(f"Creating stadium: {name}")
    cur.execute(
        "INSERT INTO estadios (nome, cidade, pais) VALUES (%s, %s, %s) RETURNING id",
        (name, city, "BRA")
    )
    return cur.fetchone()[0]

def get_or_create_club(team_data, stadium_id):
    name = team_data["name"]
    
    cur.execute("SELECT id FROM clubes WHERE nome = %s", (name,))
    result = cur.fetchone()
    if result:
        return result[0]
    
    print(f"Creating club: {name}")
    try:
        cur.execute(
            "INSERT INTO clubes (nome, sigla, cidade, pais, estadio_id) VALUES (%s, %s, %s, %s, %s) RETURNING id",
            (name, team_data.get("code", name[:3].upper()), None, "Brasil", stadium_id)
        )
    except psycopg2.errors.UniqueViolation:
        conn.rollback()
        # If stadium is already taken, insert without stadium
        print(f"Stadium {stadium_id} already assigned to another club. Creating {name} without stadium.")
        cur.execute(
            "INSERT INTO clubes (nome, sigla, cidade, pais, estadio_id) VALUES (%s, %s, %s, %s, NULL) RETURNING id",
            (name, team_data.get("code", name[:3].upper()), None, "Brasil")
        )
    return cur.fetchone()[0]

def link_club_to_competition(competicao_id, clube_id):
    cur.execute("SELECT 1 FROM competicao_clube WHERE competicao_id = %s AND clube_id = %s", (competicao_id, clube_id))
    if not cur.fetchone():
        cur.execute("INSERT INTO competicao_clube (competicao_id, clube_id) VALUES (%s, %s)", (competicao_id, clube_id))

def get_or_create_jogador(player_data, team_id):
    p_info = player_data["player"]
    name = p_info["name"]
    
    # Check by name and team? Or just name? Players move.
    # Ideally we should use API ID, but we don't have it in DB.
    # Let's check by name. If exists, update team?
    # A player might exist in DB but be in a different club now.
    # For this script, let's assume if name matches, it's the same player.
    
    cur.execute("SELECT id FROM jogadores WHERE nome_completo = %s", (name,))
    result = cur.fetchone()
    
    birth_date = p_info["birth"]["date"]
    if not birth_date:
        birth_date = None
        
    if result:
        player_id = result[0]
        # Update club
        cur.execute("UPDATE jogadores SET clube_id = %s WHERE id = %s", (team_id, player_id))
        return player_id
    
    print(f"Creating player: {name}")
    cur.execute("""
        INSERT INTO jogadores (nome_completo, apelido, data_nascimento, posicao, clube_id, valor_de_mercado)
        VALUES (%s, %s, %s, %s, %s, %s) RETURNING id
    """, (
        name, 
        p_info.get("firstname", name), 
        birth_date, 
        p_info.get("position"), # API gives position in statistics usually, but let's check
        team_id,
        None # Market value not in standard response
    ))
    return cur.fetchone()[0]

def update_player_stats(player_id, competicao_id, stats_data):
    # stats_data is one object from the 'statistics' array
    games = stats_data["games"] or {}
    goals = stats_data["goals"] or {}
    
    gols = goals.get("total") or 0
    assists = goals.get("assists") or 0
    appearences = games.get("appearences") or 0
    
    cur.execute("""
        SELECT id FROM jogador_estatisticas_competicao 
        WHERE jogador_id = %s AND competicao_id = %s
    """, (player_id, competicao_id))
    result = cur.fetchone()
    
    if result:
        # Update
        cur.execute("""
            UPDATE jogador_estatisticas_competicao 
            SET gols = %s, assistencias = %s, jogos_disputados = %s
            WHERE id = %s
        """, (gols, assists, appearences, result[0]))
    else:
        # Insert
        cur.execute("""
            INSERT INTO jogador_estatisticas_competicao (jogador_id, competicao_id, gols, assistencias, jogos_disputados)
            VALUES (%s, %s, %s, %s, %s)
        """, (player_id, competicao_id, gols, assists, appearences))

def process_fixture_details(fixture_id, partida_id):
    # Fetch details
    data = make_request("fixtures", {"id": fixture_id})
    if not data:
        return
    
    fixture_data = data[0]
    # players fixture data is in response[0]['players'] -> list of 2 teams
    players_teams = fixture_data.get("players", [])
    
    for team_stats in players_teams:
        # team_stats has 'team' and 'players' (list)
        for p_stat in team_stats["players"]:
            p_info = p_stat["player"]
            stats = p_stat["statistics"][0] # usually one per game
            
            player_name = p_info["name"]
            # Find player in DB
            cur.execute("SELECT id FROM jogadores WHERE nome_completo = %s", (player_name,))
            res = cur.fetchone()
            if not res:
                # Player not found (maybe didn't play enough to be in top lists or name mismatch)
                # We could create him here, but let's skip for safety/simplicity or try to create?
                # Let's skip to avoid creating duplicates if name varies slightly.
                continue
            
            player_id = res[0]
            
            # Extract stats
            games = stats["games"] or {}
            goals = stats["goals"] or {}
            cards = stats["cards"] or {}
            shots = stats["shots"] or {}
            tackles = stats["tackles"] or {}
            
            minutes = games.get("minutes") or 0
            yellow = (cards.get("yellow") or 0) > 0
            red = (cards.get("red") or 0) > 0
            titular = (games.get("position") != "S") # 'S' usually substitute? Or check 'games.captain'? 
            # Actually 'games.substitutes.in' > 0 means sub. 'games.lineups' boolean?
            # API docs: games.substitutes.in : Number of times the player has been substituted in.
            # If minutes > 0 and substitute.in == 0, likely titular.
            # Let's assume titular if he started. API doesn't have explicit 'starter' bool in this endpoint easily.
            # Wait, 'grid' field exists in lineups, but here in players stats?
            # Let's use a heuristic: if substitute is false?
            # For now, default to false if unsure.
            
            gols_match = goals.get("total") or 0
            assists_match = goals.get("assists") or 0
            saves = goals.get("saves") or 0 # stored in 'defesa' column?
            total_shots = shots.get("total") or 0
            on_goal = shots.get("on") or 0
            total_tackles = tackles.get("total") or 0
            
            # Insert JogadorEstatisticaPartida
            # Check duplicate
            cur.execute("SELECT id FROM jogador_estatistica_partida WHERE partida_id = %s AND jogador_id = %s", (partida_id, player_id))
            if cur.fetchone():
                continue
                
            cur.execute("""
                INSERT INTO jogador_estatistica_partida (
                    partida_id, jogador_id, minutos_jogados, cartao_amarelo, cartao_vermelho, 
                    titular, gols, assistencias, defesa, finalizacoes, chutes_a_gol, desarmes, 
                    created_at, updated_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
            """, (
                partida_id, player_id, minutes, yellow, red, 
                titular, gols_match, assists_match, saves, total_shots, on_goal, total_tackles
            ))

def process_season(season):
    print(f"--- Processing Season {season} ---")
    competicao_id = get_or_create_competicao(season)
    
    # 1. Get Teams
    print("Fetching teams...")
    teams_resp = make_request("teams", {"league": LEAGUE_ID, "season": season})
    if not teams_resp:
        print(f"Warning: No teams found for season {season}!")

    for item in teams_resp:
        team = item["team"]
        venue = item["venue"]
        stadium_id = get_or_create_stadium(venue)
        club_id = get_or_create_club(team, stadium_id)
        link_club_to_competition(competicao_id, club_id)
        
        # 2. Get Players for this team
        # Optimization: Check if we already have players for this club
        cur.execute("SELECT COUNT(*) FROM jogadores WHERE clube_id = %s", (club_id,))
        player_count = cur.fetchone()[0]
        
        if player_count > 15: # Arbitrary threshold to assume populated
             print(f"Skipping players for {team['name']} (already has {player_count} players).")
        else:
            print(f"Fetching players for {team['name']}...")
            # Note: This can be heavy (20 teams * 1 call).
            # players_resp = make_request("players", {"league": LEAGUE_ID, "season": season, "team": team["id"]})
            # Pagination handling? API returns paging.
            # Simple loop for pages
            current_page = 1
            total_pages = 1
            
            while current_page <= total_pages:
                # We need to call raw request to get paging info if we use make_request wrapper we lose it.
                # Let's do it inline here for players.
                rate_limiter.wait()
                headers = {"x-rapidapi-host": "v3.football.api-sports.io", "x-rapidapi-key": API_KEY}
                p_url = f"{API_BASE_URL}/players?league={LEAGUE_ID}&season={season}&team={team['id']}&page={current_page}"
                p_res = requests.get(p_url, headers=headers)
                if p_res.status_code != 200:
                    break
                p_json = p_res.json()
                p_list = p_json.get("response", [])
                total_pages = p_json.get("paging", {}).get("total", 1)
                
                for p_item in p_list:
                    p_data = p_item["player"]
                    stats_list = p_item["statistics"]
                    
                    # Create/Update Player
                    player_id = get_or_create_jogador(p_item, club_id)
                    
                    # Create/Update Stats
                    # Find stats for this league
                    for stat in stats_list:
                        if stat["league"]["id"] == LEAGUE_ID:
                            update_player_stats(player_id, competicao_id, stat)
            
                current_page += 1
            # time.sleep(0.2) # Rate limit safety
        
        conn.commit() # Commit after each team's players are processed
    
    conn.commit() # Commit after all teams

             
    # 3. Get Fixtures
    print("Fetching fixtures...")
    fixtures = make_request("fixtures", {"league": LEAGUE_ID, "season": season})
    if not fixtures:
        print(f"Warning: No fixtures found for season {season}!")

    for fixture in fixtures:
        f_data = fixture["fixture"]
        teams = fixture["teams"]
        goals = fixture["goals"]
        league = fixture["league"]
        
        # Ensure clubs exist (should be done in step 1, but safe check)
        # We don't have stadium info for clubs here easily if we didn't fetch teams list.
        # But we did step 1.
        
        # Find club IDs
        cur.execute("SELECT id FROM clubes WHERE nome = %s", (teams["home"]["name"],))
        h_res = cur.fetchone()
        home_id = h_res[0] if h_res else get_or_create_club(teams["home"], None)
        
        cur.execute("SELECT id FROM clubes WHERE nome = %s", (teams["away"]["name"],))
        a_res = cur.fetchone()
        away_id = a_res[0] if a_res else get_or_create_club(teams["away"], None)
        
        # Stadium for match
        stadium_id = get_or_create_stadium(f_data.get("venue"))
        if not stadium_id:
             # Fallback
             cur.execute("SELECT id FROM estadios WHERE nome = 'Desconhecido'")
             res = cur.fetchone()
             if res:
                 stadium_id = res[0]
             else:
                 cur.execute("INSERT INTO estadios (nome, cidade, pais) VALUES ('Desconhecido', 'Desconhecido', 'Brasil') RETURNING id")
                 stadium_id = cur.fetchone()[0]

        # Match Date
        try:
            match_date = datetime.fromisoformat(f_data["date"].replace("Z", "+00:00"))
        except:
            match_date = datetime.now()

        # Insert Partida
        cur.execute("""
            SELECT id FROM partidas 
            WHERE clube_mandante_id = %s AND clube_visitante_id = %s AND data_hora = %s
        """, (home_id, away_id, match_date))
        
        partida_id = None
        res = cur.fetchone()
        if res:
            partida_id = res[0]
        else:
            print(f"Inserting match: {teams['home']['name']} vs {teams['away']['name']}")
            cur.execute("""
                INSERT INTO partidas (
                    clube_mandante_id, clube_visitante_id, estadio_id, fase, 
                    gols_mandante, gols_visitante, data_hora, created_at, updated_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW()) RETURNING id
            """, (
                home_id, away_id, stadium_id, 'PONTOS_CORRIDOS', 
                goals["home"] or 0, goals["away"] or 0, match_date
            ))
            partida_id = cur.fetchone()[0]
        
        # 4. Match Stats (Lineups/Players)
        # Only if match is finished?
        if f_data["status"]["short"] in ["FT", "AET", "PEN"]:
            # Optimization: Check if we already have stats for this match
            cur.execute("SELECT COUNT(*) FROM jogador_estatistica_partida WHERE partida_id = %s", (partida_id,))
            if cur.fetchone()[0] > 0:
                print(f"Skipping stats for match {partida_id} (already populated).")
            else:
                print(f"Processing stats for match {partida_id}...")
                process_fixture_details(f_data["id"], partida_id)
                conn.commit() # Commit per match
    
    conn.commit() # Final commit for the season

def main():
    for season in SEASONS:
        process_season(season)
    
    conn.close()
    print("Full population completed.")

if __name__ == "__main__":
    main()