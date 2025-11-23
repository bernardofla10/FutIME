// =======================
// CONFIGURAÇÃO DA API
// =======================
const API_BASE = 'http://localhost:8081';

// =======================
// ESTADO DA APLICAÇÃO
// =======================
let selectedCompetitionName = null;
let selectedCompetition = null;
let selectedSeason = null;
let selectedCategory = null;

let allCompetitions = [];
let allClubes = [];
let allJogadores = [];
let allEstadios = [];
let allPartidas = [];

let isLoadingData = true; // Estado de carregamento

// =======================
// ELEMENTOS DOM
// =======================
const resultsTitleEl = document.getElementById('resultsTitle');
const resultsSubtitleEl = document.getElementById('resultsSubtitle');
const cardsContainerEl = document.getElementById('cardsContainer');
const selectionHintEl = document.getElementById('selectionHint');

const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResults');
const detailView = document.getElementById('detailView');
const detailContent = document.getElementById('detailContent');
const homeView = document.getElementById('homeView');
const btnBack = document.getElementById('btnBack');
const btnHome = document.getElementById('btnHome');

// ======================
// UTILITÁRIOS
// ======================

function normalizeFileName(name) {
    if (!name) return '';
    return name.toLowerCase()
        .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
        .replace(/\s+/g, '_')
        .replace(/[^a-z0-9_]/g, '');
}

function getClubImgUrl(club) {
    if (!club) return 'assets/placeholder_club.png';
    if (club.imageUrl) return club.imageUrl;
    const normalized = normalizeFileName(club.nome);
    return `assets/clubs/${normalized}.png`;
}

function getPlayerImgUrl(player) {
    const name = player.apelido || player.nomeCompleto;
    if (!name) return 'assets/placeholder_player.png';
    const normalized = normalizeFileName(name);
    return `assets/players/${normalized}.jpg`;
}

function getPlayerImgTag(player) {
    const normalized = normalizeFileName(player.apelido || player.nomeCompleto);
    // Usa imageUrl da API se existir, senão tenta local
    const src = player.imageUrl || `assets/players/${normalized}.jpg`;
    return `<img src="${src}" 
            class="card-image player-img" 
            alt="${player.apelido || player.nomeCompleto}"
            onerror="this.onerror=null; this.src='assets/players/${normalized}.png';">`;
}

async function fetchData(endpoint) {
    const response = await fetch(`${API_BASE}${endpoint}`);
    if (!response.ok) throw new Error(`Erro na API: ${response.status}`);
    return await response.json();
}

async function fetchDataAuth(endpoint, options = {}) {
    const user = getCurrentUser();
    if (!user) throw new Error('Usuário não autenticado');

    const headers = {
        'Authorization': 'Basic ' + btoa(user.email + ':' + user.password),
        'Content-Type': 'application/json',
        ...options.headers
    };

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (!response.ok) throw new Error(`Erro na API: ${response.status}`);
    return await response.json();
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR').format(date);
}

function formatCurrency(value) {
    if (value === null || value === undefined) return '-';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'EUR' }).format(value); // Mantido EUR conforme original, altere para BRL se necessário
}

// =======================
// CARREGAMENTO DE DADOS
// =======================

async function loadCompeticoes() {
    try {
        allCompetitions = await fetchData('/competicoes');
        renderCompetitionButtons();
    } catch (error) {
        console.error('Erro ao carregar competições:', error);
        throw error;
    }
}

async function loadAllData() {
    isLoadingData = true;
    try {
        const [clubes, jogadores, estadios, partidas] = await Promise.all([
            fetchData('/clubes'),
            fetchData('/jogadores'),
            fetchData('/estadios'),
            fetchData('/partidas')
        ]);

        allClubes = clubes;
        allJogadores = jogadores;
        allEstadios = estadios;
        allPartidas = partidas;
    } catch (error) {
        console.error('Erro ao carregar dados gerais:', error);
        throw error;
    } finally {
        isLoadingData = false;
        // Se o usuário já estiver em uma tela que precisa de dados, re-renderiza
        if (selectedCategory && selectedCompetition && selectedSeason) {
            render();
        }
    }
}

// =======================
// AUTENTICAÇÃO DE USUÁRIO
// =======================

let currentUser = null;

// Elementos DOM de autenticação
const btnLogin = document.getElementById('btnLogin');
const btnLogout = document.getElementById('btnLogout');
const userProfile = document.getElementById('userProfile');
const userName = document.getElementById('userName');
const authModal = document.getElementById('authModal');
const closeModal = document.querySelector('.close-modal');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const authTabs = document.querySelectorAll('.auth-tab');
const userFavorites = document.getElementById('userFavorites');

function getCurrentUser() {
    if (!currentUser) {
        const stored = localStorage.getItem('futimeUser');
        if (stored) {
            currentUser = JSON.parse(stored);
        }
    }
    return currentUser;
}

function saveUser(user) {
    currentUser = user;
    localStorage.setItem('futimeUser', JSON.stringify(user));
    updateUserUI();
}

function clearUser() {
    currentUser = null;
    localStorage.removeItem('futimeUser');
    updateUserUI();
}

function updateUserUI() {
    if (currentUser) {
        btnLogin.classList.add('hidden');
        userProfile.classList.remove('hidden');
        userName.textContent = `👤 ${currentUser.nome}`;
        userFavorites.classList.remove('hidden');
        loadUserFavorites();
    } else {
        btnLogin.classList.remove('hidden');
        userProfile.classList.add('hidden');
        userFavorites.classList.add('hidden');
    }
}

async function handleLogin(email, senha) {
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, senha })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Erro ao fazer login');
        }

        const userData = await response.json();
        saveUser({ ...userData, email, password: senha });
        authModal.classList.add('hidden');
        document.getElementById('loginError').classList.add('hidden');
        loginForm.reset();
        return true;
    } catch (error) {
        document.getElementById('loginError').textContent = `⚠️ ${error.message}`;
        document.getElementById('loginError').classList.remove('hidden');
        return false;
    }
}

async function handleRegister(nome, email, senha) {
    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nome, email, senha })
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Erro ao registrar');
        }

        const userData = await response.json();
        saveUser({ ...userData, email, password: senha });
        authModal.classList.add('hidden');
        document.getElementById('registerError').classList.add('hidden');
        registerForm.reset();
        return true;
    } catch (error) {
        document.getElementById('registerError').textContent = `⚠️ ${error.message}`;
        document.getElementById('registerError').classList.remove('hidden');
        return false;
    }
}

function handleLogout() {
    clearUser();
}

// Event Listeners de Autenticação
btnLogin.addEventListener('click', () => {
    authModal.classList.remove('hidden');
});

closeModal.addEventListener('click', () => {
    authModal.classList.add('hidden');
});

authModal.addEventListener('click', (e) => {
    if (e.target === authModal) {
        authModal.classList.add('hidden');
    }
});

authTabs.forEach(tab => {
    tab.addEventListener('click', () => {
        const targetTab = tab.dataset.tab;
        authTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        if (targetTab === 'login') {
            loginForm.classList.remove('hidden');
            registerForm.classList.add('hidden');
        } else {
            loginForm.classList.add('hidden');
            registerForm.classList.remove('hidden');
        }
    });
});

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const senha = document.getElementById('loginPassword').value;
    await handleLogin(email, senha);
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const nome = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;
    const senha = document.getElementById('registerPassword').value;
    await handleRegister(nome, email, senha);
});

btnLogout.addEventListener('click', handleLogout);

// =======================
// FAVORITOS DO USUÁRIO
// =======================

async function loadUserFavorites() {
    if (!currentUser) return;

    try {
        const perfil = await fetchDataAuth('/usuarios/perfil');

        // Atualizar time do coração
        const favoriteTeamContent = document.getElementById('favoriteTeamContent');
        if (perfil.clubeFavorito) {
            const normalizedClub = normalizeFileName(perfil.clubeFavorito.nome);
            favoriteTeamContent.innerHTML = `
                <div class="favorite-team-card clickable" onclick="navigateTo('clube', ${perfil.clubeFavorito.id})">
                    <img src="${getClubImgUrl(perfil.clubeFavorito)}" class="favorite-img" alt="${perfil.clubeFavorito.nome}" onerror="this.onerror=null; this.src='assets/clubs/${normalizedClub}.png';">
                    <div>
                        <h4>❤️ ${perfil.clubeFavorito.nome}</h4>
                        <p>Cidade: ${perfil.clubeFavorito.estadio?.cidade || perfil.clubeFavorito.cidade || '-'}</p>
                    </div>
                </div>
            `;
        } else {
            favoriteTeamContent.innerHTML = '<p class="no-favorite">Você ainda não selecionou seu time do coração 💔.</p>';
        }

        // Atualizar jogadores favoritos
        const favoritePlayersContent = document.getElementById('favoritePlayersContent');
        if (perfil.jogadoresObservados && perfil.jogadoresObservados.length > 0) {
            favoritePlayersContent.innerHTML = perfil.jogadoresObservados.map(jogador => {
                const normalized = normalizeFileName(jogador.apelido || jogador.nomeCompleto);
                return `
                <div class="favorite-player-card clickable" onclick="navigateTo('jogador', ${jogador.id})">
                    <img src="${jogador.imageUrl || `assets/players/${normalized}.jpg`}" class="favorite-img" 
                         onerror="this.onerror=null; this.src='assets/players/${normalized}.png';" 
                         alt="${jogador.apelido}">
                    <div>
                        <h4>⭐ ${jogador.apelido || jogador.nomeCompleto}</h4>
                        <p>Time: ${jogador.clube?.nome || '-'}</p>
                        <button class="btn-remove-favorite" onclick="event.stopPropagation(); removerJogadorFavorito(${jogador.id})">🗑️ Remover</button>
                    </div>
                </div>
            `}).join('');
        } else {
            favoritePlayersContent.innerHTML = '<p class="no-favorite">Você ainda não adicionou jogadores favoritos.</p>';
        }
    } catch (error) {
        console.error('Erro ao carregar favoritos:', error);
    }
}

async function definirTimeCoracao(clubeId) {
    if (!currentUser) {
        alert('Você precisa estar logado para definir seu time do coração!');
        return;
    }

    try {
        await fetchDataAuth(`/usuarios/meu-time/${clubeId}`, { method: 'PUT' });
        alert('Time do coração definido com sucesso! ❤️');
        loadUserFavorites();
    } catch (error) {
        console.error('Erro ao definir time do coração:', error);
        alert('Erro ao definir time do coração');
    }
}

async function adicionarJogadorFavorito(jogadorId) {
    if (!currentUser) {
        alert('Você precisa estar logado para adicionar jogadores favoritos!');
        return;
    }

    try {
        await fetchDataAuth(`/usuarios/olheiro/${jogadorId}`, { method: 'POST' });
        alert('Jogador adicionado aos favoritos! ⭐');
        loadUserFavorites();
    } catch (error) {
        console.error('Erro ao adicionar jogador favorito:', error);
        alert('Erro ao adicionar jogador favorito');
    }
}

async function removerJogadorFavorito(jogadorId) {
    if (!currentUser) return;

    try {
        await fetchDataAuth(`/usuarios/olheiro/${jogadorId}`, { method: 'DELETE' });
        loadUserFavorites();
    } catch (error) {
        console.error('Erro ao remover jogador favorito:', error);
        alert('Erro ao remover jogador favorito');
    }
}

// =======================
// ROTEAMENTO E NAVEGAÇÃO
// =======================

function showHome() {
    detailView.classList.add('hidden');
    homeView.classList.remove('hidden');
    searchResults.classList.add('hidden');
    searchInput.value = '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function showDetailView() {
    homeView.classList.add('hidden');
    detailView.classList.remove('hidden');
    searchResults.classList.add('hidden');
    searchInput.value = '';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function navigateTo(type, id) {
    showDetailView();
    switch (type) {
        case 'clube':
            const clube = allClubes.find(c => c.id === id);
            if (clube) renderTeamDetails(clube);
            break;
        case 'jogador':
            const jogador = allJogadores.find(j => j.id === id);
            if (jogador) renderPlayerDetails(jogador);
            break;
        case 'estadio':
            const estadio = allEstadios.find(e => e.id === id);
            if (estadio) renderStadiumDetails(estadio);
            break;
        case 'competicao':
            const competicao = allCompetitions.find(c => c.id === id);
            if (competicao) renderCompetitionDetails(competicao);
            break;
    }
}

// =======================
// BUSCA
// =======================

searchInput.addEventListener('input', (e) => handleSearch(e.target.value));

function handleSearch(query) {
    if (!query || query.length < 2) {
        searchResults.classList.add('hidden');
        return;
    }

    const lowerQuery = query.toLowerCase();
    const results = [];

    // Buscar Clubes
    allClubes.forEach(c => {
        if (c.nome.toLowerCase().includes(lowerQuery)) {
            results.push({ type: 'clube', name: c.nome, id: c.id, label: '🛡️ Time', img: getClubImgUrl(c) });
        }
    });

    // Buscar Jogadores
    allJogadores.forEach(j => {
        if ((j.nomeCompleto && j.nomeCompleto.toLowerCase().includes(lowerQuery)) ||
            (j.apelido && j.apelido.toLowerCase().includes(lowerQuery))) {
            const normalized = normalizeFileName(j.apelido || j.nomeCompleto);
            results.push({
                type: 'jogador',
                name: j.apelido || j.nomeCompleto,
                id: j.id,
                label: '🏃 Jogador',
                img: `assets/players/${normalized}.jpg`,
                isPlayer: true
            });
        }
    });

    // Buscar Estádios
    allEstadios.forEach(e => {
        if (e.nome.toLowerCase().includes(lowerQuery)) {
            results.push({ type: 'estadio', name: e.nome, id: e.id, label: '🏟️ Estádio' });
        }
    });

    // Buscar Competições
    allCompetitions.forEach(c => {
        if (c.nome.toLowerCase().includes(lowerQuery)) {
            results.push({ type: 'competicao', name: `${c.nome} ${c.temporada}`, id: c.id, label: '🏆 Competição' });
        }
    });

    renderSearchResults(results);
}

function renderSearchResults(results) {
    searchResults.innerHTML = '';
    if (results.length === 0) {
        searchResults.classList.add('hidden');
        return;
    }

    results.slice(0, 10).forEach(res => {
        const div = document.createElement('div');
        div.className = 'search-item';

        let imgHtml = '';
        if (res.img || res.imageUrl) {
            const src = res.imageUrl || res.img;
            if (res.isPlayer) {
                imgHtml = `<img src="${src}" class="search-item-img" onerror="this.onerror=null; this.src='assets/players/${normalizeFileName(res.name)}.png';">`;
            } else {
                // Para clubes e outros
                imgHtml = `<img src="${src}" class="search-item-img" onerror="this.onerror=null; this.src='assets/clubs/${normalizeFileName(res.name)}.png';">`;
            }
        }

        div.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px;">
                ${imgHtml}
                <span class="search-item-name">${res.name}</span>
            </div>
            <span class="search-item-type">${res.label}</span>
        `;
        div.addEventListener('click', () => {
            navigateTo(res.type, res.id);
        });
        searchResults.appendChild(div);
    });

    searchResults.classList.remove('hidden');
}

// =======================
// RENDERIZAÇÃO DETALHADA
// =======================

function renderTeamDetails(clube) {
    // Jogadores do time
    const jogadores = allJogadores.filter(j => j.clube && j.clube.id === clube.id);

    // Partidas do time
    const partidas = allPartidas.filter(p =>
        (p.mandante && p.mandante.id === clube.id) ||
        (p.visitante && p.visitante.id === clube.id)
    );

    // Competições que o time disputa
    const competicoes = allCompetitions.filter(c => c.clubes && c.clubes.some(cl => cl.id === clube.id));

    let partidasHtml = '';
    if (partidas.length > 0) {
        partidasHtml = partidas.map(p => {
            const isMandante = p.mandante.id === clube.id;
            const adversario = isMandante ? p.visitante : p.mandante;
            const golsPro = isMandante ? p.golsMandante : p.golsVisitante;
            const golsContra = isMandante ? p.golsVisitante : p.golsMandante;

            let statusClass = 'draw';
            if (golsPro > golsContra) statusClass = 'win';
            else if (golsPro < golsContra) statusClass = 'loss';

            return `
                <div class="match-card ${statusClass}">
                    <div class="match-info">
                        <span class="match-date">📅 ${formatDate(p.dataHora)}</span>
                        <span class="match-opponent">vs ${adversario.nome}</span>
                    </div>
                    <div class="match-score">${golsPro} - ${golsContra}</div>
                </div>
            `;
        }).join('');
    } else {
        partidasHtml = '<p>Nenhuma partida registrada.</p>';
    }

    let jogadoresHtml = jogadores.map(j => {
        const normalized = normalizeFileName(j.apelido || j.nomeCompleto);
        return `
        <li class="clickable player-list-item" onclick="navigateTo('jogador', ${j.id})">
            <img src="${j.imageUrl || `assets/players/${normalized}.jpg`}" class="mini-avatar" onerror="this.onerror=null; this.src='assets/players/${normalized}.png';">
            <span>${j.apelido || j.nomeCompleto} ${j.posicao ? `<small>(${j.posicao})</small>` : ''}</span>
        </li>
    `}).join('');

    let competicoesHtml = competicoes.map(c => `
        <li>
            🏆 ${c.nome} <span class="badge">${c.temporada}</span>
        </li>
    `).join('');

    detailContent.innerHTML = `
        <div class="detail-header-content">
            <img src="${getClubImgUrl(clube)}" class="detail-logo" alt="${clube.nome}" onerror="this.onerror=null; this.src='assets/clubs/${normalizeFileName(clube.nome)}.png';">
            <div>
                <h2>${clube.nome}</h2>
                <p class="details-meta">
                    ${clube.estadio?.cidade ? `📍 Cidade: ${clube.estadio.cidade} &bull;` : (clube.cidade ? `📍 Cidade: ${clube.cidade} &bull;` : '')}
                    Estádio: <span class="clickable" onclick="navigateTo('estadio', ${clube.estadio?.id})">🏟️ ${clube.estadio?.nome || '-'}</span>
                </p>
                ${currentUser ? `<button class="btn-favorite" onclick="definirTimeCoracao(${clube.id})">❤️ Definir como Time do Coração</button>` : ''}
            </div>
        </div>

        <div class="details-grid">
            <div class="details-block">
                <h4>⚽ Últimas Partidas</h4>
                ${partidasHtml}
            </div>
            <div class="details-block">
                <h4>👕 Elenco</h4>
                <ul class="player-list">${jogadoresHtml || '<li>Sem jogadores cadastrados</li>'}</ul>
            </div>
            <div class="details-block">
                <h4>🏆 Competições</h4>
                <ul>${competicoesHtml || '<li>Não disputa competições registradas</li>'}</ul>
            </div>
        </div>
    `;
}

function renderPlayerDetails(jogador) {
    const estatisticas = jogador.estatisticasPorCompeticao || [];

    let estatHtml = estatisticas.map(e => `
        <li>
            <strong>${e.nomeCompeticao}</strong>: 
            ⚽ ${e.gols} gols, 👟 ${e.assistencias} assistências
        </li>
    `).join('');

    const normalized = normalizeFileName(jogador.apelido || jogador.nomeCompleto);

    detailContent.innerHTML = `
        <div class="detail-header-content">
            <img src="${jogador.imageUrl || `assets/players/${normalized}.jpg`}" class="detail-player-img" 
                 onerror="this.onerror=null; this.src='assets/players/${normalized}.png';" 
                 alt="${jogador.apelido || jogador.nomeCompleto}">
            <div>
                <h2>${jogador.nomeCompleto}</h2>
                <p class="details-meta">
                    Apelido: <strong>${jogador.apelido || '-'}</strong> &bull;
                    Time: <span class="clickable" onclick="navigateTo('clube', ${jogador.clube?.id})"><strong>🛡️ ${jogador.clube?.nome || '-'}</strong></span>
                </p>
                ${currentUser ? `<button class="btn-favorite" onclick="adicionarJogadorFavorito(${jogador.id})">⭐ Adicionar aos Favoritos</button>` : ''}
            </div>
        </div>

        <div class="details-grid">
            <div class="details-block">
                <h4>📊 Estatísticas Totais</h4>
                <ul>
                    <li>⚽ Gols: ${jogador.golsTotais ?? 0}</li>
                    <li>👟 Assistências: ${jogador.assistenciasTotais ?? 0}</li>
                    <li>💰 Valor de Mercado: <span class="market-value">${formatCurrency(jogador.valorDeMercado)}</span></li>
                </ul>
            </div>
            <div class="details-block">
                <h4>🏅 Por Competição</h4>
                <ul>${estatHtml || '<li>Sem estatísticas registradas</li>'}</ul>
            </div>
        </div>
    `;
}

function renderStadiumDetails(estadio) {
    // Encontrar dono do estádio (clube que tem este estádio)
    const dono = allClubes.find(c => c.estadio && c.estadio.id === estadio.id);

    detailContent.innerHTML = `
        <h2>🏟️ ${estadio.nome}</h2>
        <p class="details-meta">
            📍 Cidade: <strong>${estadio.cidade}</strong> &bull;
            País: <strong>${estadio.pais}</strong>
        </p>

        <div class="details-grid">
            <div class="details-block">
                <h4>🏠 Time Mandante</h4>
                ${dono ? `
                    <div class="clickable" onclick="navigateTo('clube', ${dono.id})" style="display: flex; align-items: center; gap: 10px;">
                        <img src="${getClubImgUrl(dono)}" class="mini-avatar">
                        <strong>${dono.nome}</strong>
                    </div>
                ` : '<p>Nenhum time vinculado como mandante.</p>'}
            </div>
        </div>
    `;
}

function calculateStandings(clubes, competicaoId) {
    // Get all matches for this competition
    const competicao = allCompetitions.find(c => c.id === competicaoId);
    if (!competicao) return [];

    // Filter matches that belong to this competition's clubs
    const clubeIds = clubes.map(c => c.id);
    const matches = allPartidas.filter(p =>
        clubeIds.includes(p.mandante?.id) && clubeIds.includes(p.visitante?.id)
    );

    // Initialize standings
    const standings = clubes.map(clube => ({
        clube: clube,
        jogos: 0,
        vitorias: 0,
        empates: 0,
        derrotas: 0,
        golsMarcados: 0,
        golsSofridos: 0,
        saldoGols: 0,
        pontos: 0
    }));

    // Calculate standings from matches
    matches.forEach(match => {
        const homeTeam = standings.find(s => s.clube.id === match.mandante.id);
        const awayTeam = standings.find(s => s.clube.id === match.visitante.id);

        if (!homeTeam || !awayTeam) return;

        const homeGoals = match.golsMandante || 0;
        const awayGoals = match.golsVisitante || 0;

        // Update games played
        homeTeam.jogos++;
        awayTeam.jogos++;

        // Update goals
        homeTeam.golsMarcados += homeGoals;
        homeTeam.golsSofridos += awayGoals;
        awayTeam.golsMarcados += awayGoals;
        awayTeam.golsSofridos += homeGoals;

        // Update results
        if (homeGoals > awayGoals) {
            // Home win
            homeTeam.vitorias++;
            homeTeam.pontos += 3;
            awayTeam.derrotas++;
        } else if (awayGoals > homeGoals) {
            // Away win
            awayTeam.vitorias++;
            awayTeam.pontos += 3;
            homeTeam.derrotas++;
        } else {
            // Draw
            homeTeam.empates++;
            awayTeam.empates++;
            homeTeam.pontos += 1;
            awayTeam.pontos += 1;
        }
    });

    // Calculate goal difference
    standings.forEach(s => {
        s.saldoGols = s.golsMarcados - s.golsSofridos;
    });

    // Sort standings: points desc, wins desc, goal difference desc, goals scored desc
    standings.sort((a, b) => {
        if (b.pontos !== a.pontos) return b.pontos - a.pontos;
        if (b.vitorias !== a.vitorias) return b.vitorias - a.vitorias;
        if (b.saldoGols !== a.saldoGols) return b.saldoGols - a.saldoGols;
        return b.golsMarcados - a.golsMarcados;
    });

    return standings;
}

function renderCompetitionDetails(competicao) {
    // Times da competição
    const times = competicao.clubes || [];

    let content = `<h2>🏆 ${competicao.nome} (${competicao.temporada})</h2>`;

    // Standings table removed as per user request

    let timesHtml = times.map(t => `
        <li class="clickable player-list-item" onclick="navigateTo('clube', ${t.id})">
            <img src="${getClubImgUrl(t)}" class="mini-avatar">
            <span>${t.nome}</span>
        </li>
    `).join('');

    content += `
        <div class="details-grid">
            <div class="details-block">
                <h4>🛡️ Times Participantes</h4>
                <ul class="player-list">${timesHtml || '<li>Sem times registrados</li>'}</ul>
            </div>
        </div>
    `;

    detailContent.innerHTML = content;
}

// =======================
// RENDERIZAÇÃO HOME (Mantida e adaptada)
// =======================

function renderCompetitionButtons() {
    const container = document.getElementById('competitionRow');
    container.innerHTML = '';

    if (allCompetitions.length === 0) {
        container.innerHTML = '<p style="color: #ff6b6b;">Nenhuma competição encontrada</p>';
        return;
    }

    const uniqueNames = [...new Set(allCompetitions.map(c => c.nome))];

    uniqueNames.forEach(name => {
        const btn = document.createElement('button');
        btn.className = 'pill';
        btn.dataset.competitionName = name;
        let icon = '⚽';
        if (name.toLowerCase().includes('brasileirão')) icon = '🇧🇷';
        else if (name.toLowerCase().includes('libertadores')) icon = '🏆';

        btn.textContent = `${icon} ${name}`;
        btn.addEventListener('click', () => selectCompetitionByName(name));
        container.appendChild(btn);
    });
}

function selectCompetitionByName(name) {
    selectedCompetitionName = name;
    document.querySelectorAll('#competitionRow .pill').forEach(b => b.classList.remove('active'));
    const activeBtn = document.querySelector(`[data-competition-name="${name}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    const variations = allCompetitions.filter(c => c.nome === name);
    renderSeasonButtons(variations);

    if (variations.length > 0) {
        variations.sort((a, b) => b.temporada.localeCompare(a.temporada));
        selectSeason(variations[0].temporada);
    }
}

function renderSeasonButtons(variations) {
    const container = document.getElementById('seasonRow');
    container.innerHTML = '';
    const seasons = variations.map(v => v.temporada).sort();

    seasons.forEach(season => {
        const btn = document.createElement('button');
        btn.className = 'pill';
        btn.dataset.season = season;
        btn.textContent = `📅 ${season}`;
        btn.addEventListener('click', () => selectSeason(season));
        container.appendChild(btn);
    });
}

function selectSeason(season) {
    selectedSeason = season;
    document.querySelectorAll('#seasonRow .pill').forEach(b => b.classList.remove('active'));
    const activeBtn = document.querySelector(`[data-season="${season}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    if (selectedCompetitionName) {
        const targetComp = allCompetitions.find(c => c.nome === selectedCompetitionName && c.temporada === season);
        if (targetComp) selectedCompetition = targetComp.id;
    }
    updateHint();
    render();
}

function updateHint() {
    if (!selectedCompetitionName) selectionHintEl.textContent = '💡 Selecione a competição para começar.';
    else if (!selectedSeason) selectionHintEl.textContent = '💡 Agora escolha a temporada.';
    else if (!selectedCategory) selectionHintEl.textContent = '✨ Perfeito! Agora escolha se quer ver times, jogadores ou estádios.';
    else selectionHintEl.textContent = `✅ Explorando ${selectedCompetitionName} ${selectedSeason} — categoria: ${selectedCategory}.`;
}

function clearResults() {
    cardsContainerEl.innerHTML = '';
}

function showLoading() {
    cardsContainerEl.innerHTML = '<div class="loading">⏳ Carregando dados...</div>';
}

function showError(message) {
    cardsContainerEl.innerHTML = `<div class="error-message">⚠️ ${message}</div>`;
}

function render() {
    clearResults();
    if (!selectedCompetition || !selectedSeason || !selectedCategory) {
        resultsTitleEl.textContent = '👋 Selecione competição, temporada e categoria.';
        resultsSubtitleEl.textContent = 'Use as opções acima para filtrar o que você quer ver.';
        return;
    }

    const comp = allCompetitions.find(c => c.id === selectedCompetition);
    if (!comp) return;

    const catLabel = {
        clubes: '🛡️ Times participantes',
        jogadores: '🏃 Todos os jogadores cadastrados',
        estadios: '🏟️ Estádios'
    }[selectedCategory];

    resultsTitleEl.textContent = `${comp.nome} • ${selectedSeason}`;
    resultsSubtitleEl.textContent = catLabel;

    switch (selectedCategory) {
        case 'clubes': renderClubes(comp); break;
        case 'jogadores': renderJogadores(comp); break;
        case 'estadios': renderEstadios(); break;
    }
}

function renderClubes(comp) {
    if (isLoadingData) {
        cardsContainerEl.innerHTML = '<div class="loading">⏳ Carregando times...</div>';
        return;
    }
    const clubesFiltered = comp.clubes || [];
    if (!clubesFiltered.length) {
        cardsContainerEl.innerHTML = '<p>Não há times cadastrados.</p>';
        return;
    }

    const html = clubesFiltered.map(clube => `
        <article class="card clickable" onclick="navigateTo('clube', ${clube.id})">
            <div class="card-header">
                <div style="display: flex; align-items: center; gap: 15px;">
                    <img src="${getClubImgUrl(clube)}" class="card-image" alt="${clube.nome}">
                    <div>
                        <div class="card-title">${clube.nome}</div>
                        <div class="results-subtitle">📍 ${clube.estadio?.cidade || '-'}</div>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <p><span>País:</span> ${clube.pais}</p>
                <p><span>🏟️ Estádio:</span> ${clube.estadio?.nome || '-'}</p>
            </div>
        </article>
    `).join('');
    cardsContainerEl.innerHTML = html;
}

function renderJogadores(comp) {
    if (isLoadingData) {
        cardsContainerEl.innerHTML = '<div class="loading">⏳ Carregando jogadores...</div>';
        return;
    }
    const clubeIds = (comp.clubes || []).map(c => c.id);
    const jogadoresFiltered = allJogadores.filter(j => j.clube && clubeIds.includes(j.clube.id));

    if (!jogadoresFiltered.length) {
        cardsContainerEl.innerHTML = '<p>Não há jogadores cadastrados.</p>';
        return;
    }

    const html = jogadoresFiltered.map(jogador => `
        <article class="card clickable" onclick="navigateTo('jogador', ${jogador.id})">
            <div class="card-header">
                <div style="display: flex; align-items: center; gap: 15px;">
                    ${getPlayerImgTag(jogador)}
                    <div>
                        <div class="card-title">${jogador.apelido || jogador.nomeCompleto}</div>
                        <span class="badge">${jogador.posicao || '-'}</span>
                    </div>
                </div>
            </div>
            <div class="card-body">
                <p><span>🛡️ Time:</span> ${jogador.clube?.nome || '-'}</p>
                <p><span>⚽ Gols:</span> ${jogador.golsTotais ?? 0}</p>
                <p><span>💰 Valor:</span> ${formatCurrency(jogador.valorDeMercado)}</p>
            </div>
        </article>
    `).join('');
    cardsContainerEl.innerHTML = html;
}

// =======================
// NAVEGAÇÃO E SCROLL SPY
// =======================

function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a');
    const sections = [
        { id: 'homeView', navId: 'btnHome' }, // Hero/Home section
        { id: 'explorar', navId: null }, // Explorar section (link href="#explorar")
        { id: 'sobre', navId: null } // Sobre section (link href="#sobre")
    ];

    // Smooth Scroll com Offset
    navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            const href = link.getAttribute('href');
            if (href.startsWith('#')) {
                e.preventDefault();
                const targetId = href.substring(1);

                // Se for link para Início (href="#"), rolar para o topo
                if (targetId === '') {
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                    return;
                }

                const targetSection = document.getElementById(targetId);
                if (targetSection) {
                    const headerOffset = 80;
                    const elementPosition = targetSection.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.scrollY - headerOffset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: 'smooth'
                    });
                }
            }
        });
    });
    // Scroll Spy
    window.addEventListener('scroll', () => {
        let current = '';
        const scrollPosition = window.scrollY + 100; // Offset para detecção antecipada
        const bottomOfPage = window.innerHeight + window.scrollY >= document.body.offsetHeight - 10;

        // Mapeamento manual das seções para verificar
        const homeSection = document.getElementById('homeView'); // Usando homeView como topo
        const exploreSection = document.getElementById('explorar');
        const aboutSection = document.getElementById('sobre');

        if (homeSection && scrollPosition >= homeSection.offsetTop) {
            current = 'home'; // Default
        }

        if (exploreSection && scrollPosition >= exploreSection.offsetTop) {
            current = 'explorar';
        }

        if (aboutSection && scrollPosition >= aboutSection.offsetTop) {
            current = 'sobre';
        }

        // Se chegou no fim da página, força 'sobre'
        if (bottomOfPage) {
            current = 'sobre';
        }

        // Se estivermos no topo absoluto, força home
        if (window.scrollY < 50) {
            current = 'home';
        }

        navLinks.forEach(link => {
            link.classList.remove('active');
            const href = link.getAttribute('href');

            if (current === 'home' && href === '#') {
                link.classList.add('active');
            } else if (current === 'explorar' && href === '#explorar') {
                link.classList.add('active');
            } else if (current === 'sobre' && href === '#sobre') {
                link.classList.add('active');
            }
        });
    });
}

function renderEstadios() {
    if (isLoadingData) {
        cardsContainerEl.innerHTML = '<div class="loading">⏳ Carregando estádios...</div>';
        return;
    }
    if (!allEstadios.length) {
        cardsContainerEl.innerHTML = '<p>Não há estádios cadastrados.</p>';
        return;
    }

    const html = allEstadios.map(estadio => `
        <article class="card clickable" onclick="navigateTo('estadio', ${estadio.id})">
            <div class="card-header">
                <div class="card-title">${estadio.nome}</div>
                <span class="badge">🏟️ Estádio</span>
            </div>
            <div class="card-body">
                <p><span>📍 Cidade:</span> ${estadio.cidade}</p>
                <p><span>🗺️ País:</span> ${estadio.pais}</p>
            </div>
        </article>
    `).join('');
    cardsContainerEl.innerHTML = html;
}

// =======================
// PERFIL DO USUÁRIO
// =======================

const profileView = document.getElementById('profileView');
const btnCloseProfile = document.getElementById('btnCloseProfile');
const profileName = document.getElementById('profileName');
const profileEmail = document.getElementById('profileEmail');
const profilePassword = document.getElementById('profilePassword');
const btnTogglePassword = document.getElementById('btnTogglePassword');

// Navegação para o Perfil
userName.addEventListener('click', () => {
    if (currentUser) {
        showProfileView();
    }
});

btnCloseProfile.addEventListener('click', () => {
    profileView.classList.add('hidden');
    homeView.classList.remove('hidden');
});

function showProfileView() {
    homeView.classList.add('hidden');
    detailView.classList.add('hidden');
    searchResults.classList.add('hidden');
    profileView.classList.remove('hidden');

    renderUserProfile();
}

async function renderUserProfile() {
    if (!currentUser) return;

    profileName.textContent = currentUser.nome;
    profileEmail.textContent = currentUser.email;

    // Senha (recuperada do cache local da sessão)
    profilePassword.value = currentUser.password || '';

    // Carregar favoritos
    try {
        const perfil = await fetchDataAuth('/usuarios/perfil');
        renderProfileFavorites(perfil);
    } catch (error) {
        console.error('Erro ao carregar perfil:', error);
    }
}

// Toggle Senha
btnTogglePassword.addEventListener('click', () => {
    const type = profilePassword.getAttribute('type') === 'password' ? 'text' : 'password';
    profilePassword.setAttribute('type', type);
    btnTogglePassword.textContent = type === 'password' ? '👁️' : '🙈';
});

function renderProfileFavorites(perfil) {
    // Time do Coração
    const profileTeamContent = document.getElementById('profileTeamContent');
    if (perfil.clubeFavorito) {
        profileTeamContent.innerHTML = `
            <div class="favorite-team-card clickable" onclick="navigateTo('clube', ${perfil.clubeFavorito.id})">
                <h4>❤️ ${perfil.clubeFavorito.nome}</h4>
                <p>Cidade: ${perfil.clubeFavorito.estadio?.cidade || perfil.clubeFavorito.cidade || '-'}</p>
                <p>Estádio: ${perfil.clubeFavorito.estadio?.nome || '-'}</p>
            </div>
        `;
    } else {
        profileTeamContent.innerHTML = '<p class="no-favorite">Você ainda não selecionou seu time do coração.</p>';
    }

    // Jogadores Favoritos
    const profilePlayersContent = document.getElementById('profilePlayersContent');
    if (perfil.jogadoresObservados && perfil.jogadoresObservados.length > 0) {
        profilePlayersContent.innerHTML = perfil.jogadoresObservados.map(jogador => `
            <div class="favorite-player-card clickable" onclick="navigateTo('jogador', ${jogador.id})">
                <h4>⭐ ${jogador.apelido || jogador.nomeCompleto}</h4>
                <p>Time: ${jogador.clube?.nome || '-'}</p>
                <p>Posição: ${jogador.posicao || '-'}</p>
                <p>⚽ ${jogador.golsTotais ?? 0} | 👟 ${jogador.assistenciasTotais ?? 0}</p>
                <button class="btn-remove-favorite" onclick="event.stopPropagation(); removerJogadorFavorito(${jogador.id})">🗑️ Remover</button>
            </div>
        `).join('');
    } else {
        profilePlayersContent.innerHTML = '<p class="no-favorite">Você ainda não adicionou jogadores favoritos.</p>';
    }
}

// =======================
// INICIALIZAÇÃO
// =======================

// Event Listeners Globais
btnHome.addEventListener('click', (e) => {
    e.preventDefault();
    showHome();
});

btnBack.addEventListener('click', showHome);

document.querySelectorAll('.pill').forEach(pill => {
    pill.addEventListener('click', function () {
        // Lógica de seleção de categoria
        if (this.parentElement.id === 'categoryRow') {
            document.querySelectorAll('#categoryRow .pill').forEach(p => p.classList.remove('active'));
            this.classList.add('active');
            selectedCategory = this.dataset.category;
            updateHint();
            render();
        }
    });
});

// Inicializar
(async function init() {
    try {
        updateUserUI();
        setupNavigation();
        await loadCompeticoes();
        await loadAllData();
    } catch (error) {
        console.error('Erro na inicialização:', error);
        showError('Falha ao carregar dados iniciais. Verifique se a API está rodando.');
    }
})();
// =======================
// DASHBOARD E GRÁFICOS
// =======================

let charts = {}; // Armazena instâncias dos gráficos

function renderDashboard() {
    const dashboardSection = document.getElementById('dashboardSection');

    // Só mostra o dashboard se tiver competição e temporada selecionadas
    if (!selectedCompetition || !selectedSeason) {
        dashboardSection.classList.add('hidden');
        return;
    }

    dashboardSection.classList.remove('hidden');

    // Filtrar jogadores com estatísticas na competição/temporada selecionada
    const playersWithStats = allJogadores.filter(j =>
        j.estatisticasPorCompeticao &&
        j.estatisticasPorCompeticao.some(e =>
            e.nomeCompeticao === selectedCompetitionName &&
            e.temporada === selectedSeason
        )
    ).map(j => {
        const stat = j.estatisticasPorCompeticao.find(e =>
            e.nomeCompeticao === selectedCompetitionName &&
            e.temporada === selectedSeason
        );
        return {
            ...j,
            golsNaCompeticao: stat ? stat.gols : 0,
            assistenciasNaCompeticao: stat ? stat.assistencias : 0
        };
    });

    renderTopScorersChart(playersWithStats);
    renderTopAssistersChart(playersWithStats);
    renderMarketValueChart(playersWithStats);
}

function renderTopScorersChart(players) {
    const ctx = document.getElementById('topScorersChart').getContext('2d');

    // Top 10 Artilheiros
    const topScorers = players
        .sort((a, b) => b.golsNaCompeticao - a.golsNaCompeticao)
        .slice(0, 10)
        .filter(p => p.golsNaCompeticao > 0);

    const labels = topScorers.map(p => p.apelido || p.nomeCompleto);
    const data = topScorers.map(p => p.golsNaCompeticao);

    if (charts.topScorers) charts.topScorers.destroy();

    charts.topScorers = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Gols ⚽',
                data: data,
                backgroundColor: 'rgba(0, 255, 170, 0.6)',
                borderColor: '#00ffaa',
                borderWidth: 1
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: { beginAtZero: true, grid: { color: '#333' } },
                y: { grid: { display: false } }
            }
        }
    });
}

function renderTopAssistersChart(players) {
    const ctx = document.getElementById('topAssistersChart').getContext('2d');

    // Top 10 Assistentes
    const topAssisters = players
        .sort((a, b) => b.assistenciasNaCompeticao - a.assistenciasNaCompeticao)
        .slice(0, 10)
        .filter(p => p.assistenciasNaCompeticao > 0);

    const labels = topAssisters.map(p => p.apelido || p.nomeCompleto);
    const data = topAssisters.map(p => p.assistenciasNaCompeticao);

    if (charts.topAssisters) charts.topAssisters.destroy();

    charts.topAssisters = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Assistências 👟',
                data: data,
                backgroundColor: 'rgba(74, 158, 255, 0.6)',
                borderColor: '#4a9eff',
                borderWidth: 1
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: { beginAtZero: true, grid: { color: '#333' } },
                y: { grid: { display: false } }
            }
        }
    });
}

function renderMarketValueChart(players) {
    const ctx = document.getElementById('marketValueChart').getContext('2d');

    // Top 15 Jogadores Mais Valiosos (independente de stats na competição, mas filtrados por estarem nela?)
    // Se quisermos mostrar os mais valiosos DA COMPETIÇÃO, usamos a lista filtrada.
    const topValuable = players
        .sort((a, b) => (b.valorDeMercado || 0) - (a.valorDeMercado || 0))
        .slice(0, 15)
        .filter(p => p.valorDeMercado > 0);

    const labels = topValuable.map(p => p.apelido || p.nomeCompleto);
    const data = topValuable.map(p => p.valorDeMercado);

    if (charts.marketValue) charts.marketValue.destroy();

    charts.marketValue = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Valor de Mercado (R$) 💰',
                data: data,
                backgroundColor: 'rgba(168, 85, 247, 0.6)',
                borderColor: '#a855f7',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return formatCurrency(context.raw);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#333' },
                    ticks: {
                        callback: function (value) {
                            return 'R$ ' + (value / 1000000).toFixed(0) + 'M';
                        }
                    }
                },
                x: { grid: { display: false } }
            }
        }
    });
}

// =======================
// RENDERIZAÇÃO PRINCIPAL
// =======================

function render() {
    if (!selectedCompetition || !selectedSeason) return;

    // Renderiza o Dashboard
    renderDashboard();

    if (!selectedCategory) return;

    cardsContainerEl.innerHTML = '';
    resultsTitleEl.textContent = `${selectedCategory.charAt(0).toUpperCase() + selectedCategory.slice(1)} - ${selectedCompetitionName} ${selectedSeason}`;
    resultsSubtitleEl.textContent = `Exibindo resultados para ${selectedCategory}`;

    if (isLoadingData) {
        showLoading();
        return;
    }

    switch (selectedCategory) {
        case 'clubes':
            renderClubes();
            break;
        case 'jogadores':
            renderJogadores();
            break;
        case 'estadios':
            renderEstadios();
            break;
    }
}

function renderClubes() {
    // Filtrar clubes que participam desta competição/temporada
    // A API de competições retorna a lista de clubes nela.
    const comp = allCompetitions.find(c => c.id === selectedCompetition);
    if (!comp || !comp.clubes) {
        cardsContainerEl.innerHTML = '<p>Nenhum clube encontrado nesta competição.</p>';
        return;
    }

    comp.clubes.forEach(clube => {
        const card = document.createElement('div');
        card.className = 'card clickable';
        card.onclick = () => navigateTo('clube', clube.id);

        const imgUrl = getClubImgUrl(clube);
        const normalizedName = normalizeFileName(clube.nome);

        card.innerHTML = `
            <div class="card-header">
                <span class="badge">🛡️ Série A</span>
                <span class="badge">${clube.sigla || '---'}</span>
            </div>
            <div style="text-align: center; margin: 1rem 0;">
                <img src="${imgUrl}" style="width: 80px; height: 80px; object-fit: contain;" onerror="this.onerror=null; this.src='assets/clubs/${normalizedName}.png';">
            </div>
            <div class="card-title" style="text-align: center; margin-bottom: 1rem;">${clube.nome}</div>
            <div class="card-body">
                <p><span>📍 Cidade:</span> ${clube.estadio?.cidade || clube.cidade || '-'}</p>
                <p><span>🏟️ Estádio:</span> ${clube.estadio ? clube.estadio.nome : '-'}</p>
            </div>
        `;
        cardsContainerEl.appendChild(card);
    });
}

function renderJogadores() {
    // Filtrar jogadores que têm estatísticas nesta competição e temporada
    const jogadoresFiltrados = allJogadores.filter(j =>
        j.estatisticasPorCompeticao &&
        j.estatisticasPorCompeticao.some(e =>
            e.nomeCompeticao === selectedCompetitionName &&
            e.temporada === selectedSeason
        )
    );

    if (jogadoresFiltrados.length === 0) {
        cardsContainerEl.innerHTML = '<p>Nenhum jogador encontrado com estatísticas nesta competição.</p>';
        return;
    }

    // Ordenar por gols (padrão)
    jogadoresFiltrados.sort((a, b) => {
        const statA = a.estatisticasPorCompeticao.find(e => e.nomeCompeticao === selectedCompetitionName && e.temporada === selectedSeason);
        const statB = b.estatisticasPorCompeticao.find(e => e.nomeCompeticao === selectedCompetitionName && e.temporada === selectedSeason);
        return (statB ? statB.gols : 0) - (statA ? statA.gols : 0);
    });

    jogadoresFiltrados.forEach(jogador => {
        const stat = jogador.estatisticasPorCompeticao.find(e => e.nomeCompeticao === selectedCompetitionName && e.temporada === selectedSeason);
        const gols = stat ? stat.gols : 0;
        const assistencias = stat ? stat.assistencias : 0;
        const imgTag = getPlayerImgTag(jogador);

        const card = document.createElement('div');
        card.className = 'card clickable';
        card.onclick = () => navigateTo('jogador', jogador.id);

        card.innerHTML = `
            <div class="card-header">
                <span class="badge">${jogador.posicao || 'Atleta'}</span>
                ${gols > 0 ? `<span class="badge">⚽ ${gols}</span>` : ''}
            </div>
            <div style="text-align: center; margin: 1rem 0;">
                ${imgTag}
            </div>
            <div class="card-title" style="text-align: center; margin-bottom: 0.5rem;">${jogador.apelido || jogador.nomeCompleto}</div>
            <div class="card-body">
                <p><span>🛡️ Time:</span> ${jogador.clube ? jogador.clube.nome : '-'}</p>
                <p><span>⚽ Gols:</span> ${gols}</p>
                <p><span>👟 Assistências:</span> ${assistencias}</p>
            </div>
        `;
        cardsContainerEl.appendChild(card);
    });
}

function renderEstadios() {
    // Mostrar estádios dos times desta competição
    const comp = allCompetitions.find(c => c.id === selectedCompetition);
    if (!comp || !comp.clubes) {
        cardsContainerEl.innerHTML = '<p>Nenhum estádio encontrado.</p>';
        return;
    }

    const estadiosIds = new Set();
    const estadios = [];

    comp.clubes.forEach(c => {
        if (c.estadio && !estadiosIds.has(c.estadio.id)) {
            estadiosIds.add(c.estadio.id);
            estadios.push(c.estadio);
        }
    });

    if (estadios.length === 0) {
        cardsContainerEl.innerHTML = '<p>Nenhum estádio vinculado aos times desta competição.</p>';
        return;
    }

    estadios.forEach(estadio => {
        const card = document.createElement('div');
        card.className = 'card clickable';
        card.onclick = () => navigateTo('estadio', estadio.id);

        card.innerHTML = `
            <div class="card-header">
                <span class="badge">🏟️ Estádio</span>
            </div>
            <div class="card-title" style="margin-bottom: 1rem;">${estadio.nome}</div>
            <div class="card-body">
                <p><span>📍 Cidade:</span> ${estadio.cidade}</p>
                <p><span>🗺️ País:</span> ${estadio.pais}</p>
            </div>
        `;
        cardsContainerEl.appendChild(card);
    });
}

// =======================
// INICIALIZAÇÃO
// =======================

document.addEventListener('DOMContentLoaded', () => {
    loadCompeticoes();
    loadAllData();
    updateUserUI(); // Checa se tem usuário logado

    // Setup dos botões de categoria
    document.querySelectorAll('#categoryRow .pill').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('#categoryRow .pill').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            selectedCategory = btn.dataset.category;
            updateHint();
            render();
        });
    });

    // Botão Voltar
    btnBack.addEventListener('click', showHome);

    // Botão Home
    btnHome.addEventListener('click', showHome);

    // Botão Fechar Perfil
    document.getElementById('btnCloseProfile').addEventListener('click', () => {
        document.getElementById('profileView').classList.add('hidden');
        document.getElementById('homeView').classList.remove('hidden');
    });

    // Botão Abrir Perfil (clique no nome)
    userName.addEventListener('click', () => {
        document.getElementById('homeView').classList.add('hidden');
        document.getElementById('detailView').classList.add('hidden');
        document.getElementById('profileView').classList.remove('hidden');

        // Preencher dados
        if (currentUser) {
            document.getElementById('profileName').textContent = currentUser.nome;
            document.getElementById('profileEmail').textContent = currentUser.email;
            document.getElementById('profilePassword').value = currentUser.password;
        }
    });

    // Toggle Password
    const btnTogglePassword = document.getElementById('btnTogglePassword');
    const profilePassword = document.getElementById('profilePassword');
    btnTogglePassword.addEventListener('click', () => {
        if (profilePassword.type === 'password') {
            profilePassword.type = 'text';
            btnTogglePassword.textContent = '👁️';
        } else {
            profilePassword.type = 'password';
            btnTogglePassword.textContent = '🙈';
        }
    });
});

// =======================
// PLAYER COMPARISON
// =======================

const playerComparisonView = document.getElementById('playerComparisonView');
const btnOpenComparison = document.getElementById('btnOpenComparison');
const btnCloseComparison = document.getElementById('btnCloseComparison');
const player1Select = document.getElementById('player1Select');
const player2Select = document.getElementById('player2Select');
const btnCompare = document.getElementById('btnCompare');
const comparisonResult = document.getElementById('comparisonResult');

// Abrir comparador
if (btnOpenComparison) {
    btnOpenComparison.addEventListener('click', () => {
        showComparisonView();
    });
}

// Fechar comparador
if (btnCloseComparison) {
    btnCloseComparison.addEventListener('click', () => {
        playerComparisonView.classList.add('hidden');
        homeView.classList.remove('hidden');
    });
}

function showComparisonView() {
    homeView.classList.add('hidden');
    detailView.classList.add('hidden');
    profileView.classList.add('hidden');
    playerComparisonView.classList.remove('hidden');

    loadPlayersForComparison();
}

function loadPlayersForComparison() {
    // Limpar seletores
    player1Select.innerHTML = '<option value="">Selecione um jogador</option>';
    player2Select.innerHTML = '<option value="">Selecione um jogador</option>';

    // Filtrar jogadores com estatísticas
    let jogadoresDisponiveis = [];

    if (selectedCompetitionName && selectedSeason) {
        // Filtrar jogadores da competição/temporada selecionada
        jogadoresDisponiveis = allJogadores.filter(j =>
            j.estatisticasPorCompeticao &&
            j.estatisticasPorCompeticao.some(e =>
                e.nomeCompeticao === selectedCompetitionName &&
                e.temporada === selectedSeason
            )
        );
    } else {
        // Se não tem filtro, mostra todos os jogadores com estatísticas
        jogadoresDisponiveis = allJogadores.filter(j =>
            j.estatisticasPorCompeticao && j.estatisticasPorCompeticao.length > 0
        );
    }

    // Ordenar por nome
    jogadoresDisponiveis.sort((a, b) => {
        const nomeA = a.apelido || a.nomeCompleto || '';
        const nomeB = b.apelido || b.nomeCompleto || '';
        return nomeA.localeCompare(nomeB);
    });

    // Popula os seletores
    jogadoresDisponiveis.forEach(jogador => {
        const option1 = document.createElement('option');
        const option2 = document.createElement('option');
        const nome = jogador.apelido || jogador.nomeCompleto;
        const clube = jogador.clube ? ` (${jogador.clube.nome})` : '';

        option1.value = jogador.id;
        option1.textContent = nome + clube;
        option2.value = jogador.id;
        option2.textContent = nome + clube;

        player1Select.appendChild(option1);
        player2Select.appendChild(option2);
    });
}

// Habilitar botão de comparar quando ambos jogadores estiverem selecionados
if (player1Select && player2Select && btnCompare) {
    player1Select.addEventListener('change', checkCompareButton);
    player2Select.addEventListener('change', checkCompareButton);

    btnCompare.addEventListener('click', () => {
        const id1 = parseInt(player1Select.value);
        const id2 = parseInt(player2Select.value);

        if (id1 && id2) {
            comparePlayerStats(id1, id2);
        }
    });
}

function checkCompareButton() {
    if (player1Select.value && player2Select.value) {
        btnCompare.disabled = false;
    } else {
        btnCompare.disabled = true;
    }
}

function comparePlayerStats(jogadorId1, jogadorId2) {
    const jogador1 = allJogadores.find(j => j.id === jogadorId1);
    const jogador2 = allJogadores.find(j => j.id === jogadorId2);

    if (!jogador1 || !jogador2) {
        comparisonResult.innerHTML = '<p class="error-message">Jogadores não encontrados.</p>';
        comparisonResult.classList.remove('hidden');
        return;
    }

    renderComparison(jogador1, jogador2);
}

function renderComparison(jogador1, jogador2) {
    // Buscar estatísticas da competição/temporada selecionada, ou usar totais
    let stats1 = {
        gols: jogador1.golsTotais || 0,
        assistencias: jogador1.assistenciasTotais || 0,
        partidas: jogador1.estatisticasPorPartida ? jogador1.estatisticasPorPartida.length : 0
    };

    let stats2 = {
        gols: jogador2.golsTotais || 0,
        assistencias: jogador2.assistenciasTotais || 0,
        partidas: jogador2.estatisticasPorPartida ? jogador2.estatisticasPorPartida.length : 0
    };

    // Se houver competição/temporada selecionada, usa apenas essas stats
    if (selectedCompetitionName && selectedSeason) {
        const stat1 = jogador1.estatisticasPorCompeticao?.find(e =>
            e.nomeCompeticao === selectedCompetitionName && e.temporada === selectedSeason
        );
        const stat2 = jogador2.estatisticasPorCompeticao?.find(e =>
            e.nomeCompeticao === selectedCompetitionName && e.temporada === selectedSeason
        );

        if (stat1) {
            const partidasFiltradas = jogador1.estatisticasPorPartida ?
                jogador1.estatisticasPorPartida.filter(p =>
                    p.nomeCompeticao === selectedCompetitionName &&
                    p.temporada === selectedSeason
                ) : [];
            stats1 = {
                gols: stat1.gols || 0,
                assistencias: stat1.assistencias || 0,
                partidas: partidasFiltradas.length
            };
        }

        if (stat2) {
            const partidasFiltradas = jogador2.estatisticasPorPartida ?
                jogador2.estatisticasPorPartida.filter(p =>
                    p.nomeCompeticao === selectedCompetitionName &&
                    p.temporada === selectedSeason
                ) : [];
            stats2 = {
                gols: stat2.gols || 0,
                assistencias: stat2.assistencias || 0,
                partidas: partidasFiltradas.length
            };
        }
    }

    const nome1 = jogador1.apelido || jogador1.nomeCompleto;
    const nome2 = jogador2.apelido || jogador2.nomeCompleto;

    const html = `
        <table class="comparison-table">
            <thead>
                <tr>
                    <th class="player-name-header">${nome1}</th>
                    <th class="stat-label">Estatística</th>
                    <th class="player-name-header">${nome2}</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td class="${stats1.gols > stats2.gols ? 'stat-winner' : ''}">${stats1.gols}</td>
                    <td class="stat-label">⚽ Gols</td>
                    <td class="${stats2.gols > stats1.gols ? 'stat-winner' : ''}">${stats2.gols}</td>
                </tr>
                <tr>
                    <td class="${stats1.assistencias > stats2.assistencias ? 'stat-winner' : ''}">${stats1.assistencias}</td>
                    <td class="stat-label">👟 Assistências</td>
                    <td class="${stats2.assistencias > stats1.assistencias ? 'stat-winner' : ''}">${stats2.assistencias}</td>
                </tr>
                <tr>
                    <td class="${stats1.partidas > stats2.partidas ? 'stat-winner' : ''}">${stats1.partidas}</td>
                    <td class="stat-label">📅 Jogos Disputados</td>
                    <td class="${stats2.partidas > stats1.partidas ? 'stat-winner' : ''}">${stats2.partidas}</td>
                </tr>
                <tr>
                    <td class="${(jogador1.valorDeMercado || 0) > (jogador2.valorDeMercado || 0) ? 'stat-winner' : ''}">${formatCurrency(jogador1.valorDeMercado)}</td>
                    <td class="stat-label">💰 Valor de Mercado</td>
                    <td class="${(jogador2.valorDeMercado || 0) > (jogador1.valorDeMercado || 0) ? 'stat-winner' : ''}">${formatCurrency(jogador2.valorDeMercado)}</td>
                </tr>
            </tbody>
        </table>
    `;

    comparisonResult.innerHTML = html;
    comparisonResult.classList.remove('hidden');
}