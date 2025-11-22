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

// =======================
// UTILITÁRIOS
// =======================

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
    if (!dateString) return '—';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR').format(date);
}

function formatCurrency(value) {
    if (value === null || value === undefined) return '—';
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
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
        userName.textContent = currentUser.nome;
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
        document.getElementById('loginError').textContent = error.message;
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
        document.getElementById('registerError').textContent = error.message;
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
            favoriteTeamContent.innerHTML = `
                <div class="favorite-team-card clickable" onclick="navigateTo('clube', ${perfil.clubeFavorito.id})">
                    <h4>${perfil.clubeFavorito.nome}</h4>
                    <p>Cidade: ${perfil.clubeFavorito.cidade || '—'}</p>
                    <p>Estádio: ${perfil.clubeFavorito.estadio?.nome || '—'}</p>
                </div>
            `;
        } else {
            favoriteTeamContent.innerHTML = '<p class="no-favorite">Você ainda não selecionou seu time do coração.</p>';
        }

        // Atualizar jogadores favoritos
        const favoritePlayersContent = document.getElementById('favoritePlayersContent');
        if (perfil.jogadoresObservados && perfil.jogadoresObservados.length > 0) {
            favoritePlayersContent.innerHTML = perfil.jogadoresObservados.map(jogador => `
                <div class="favorite-player-card clickable" onclick="navigateTo('jogador', ${jogador.id})">
                    <h4>${jogador.apelido || jogador.nomeCompleto}</h4>
                    <p>Time: ${jogador.clube?.nome || '—'}</p>
                    <p>Posição: ${jogador.posicao || '—'}</p>
                    <p>Gols: ${jogador.golsTotais ?? 0} | Assistências: ${jogador.assistenciasTotais ?? 0}</p>
                    <button class="btn-remove-favorite" onclick="event.stopPropagation(); removerJogadorFavorito(${jogador.id})">Remover</button>
                </div>
            `).join('');
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
        alert('Time do coração definido com sucesso!');
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
        alert('Jogador adicionado aos favoritos!');
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
            results.push({ type: 'clube', name: c.nome, id: c.id, label: 'Time' });
        }
    });

    // Buscar Jogadores
    allJogadores.forEach(j => {
        if ((j.nomeCompleto && j.nomeCompleto.toLowerCase().includes(lowerQuery)) ||
            (j.apelido && j.apelido.toLowerCase().includes(lowerQuery))) {
            results.push({ type: 'jogador', name: j.apelido || j.nomeCompleto, id: j.id, label: 'Jogador' });
        }
    });

    // Buscar Estádios
    allEstadios.forEach(e => {
        if (e.nome.toLowerCase().includes(lowerQuery)) {
            results.push({ type: 'estadio', name: e.nome, id: e.id, label: 'Estádio' });
        }
    });

    // Buscar Competições
    allCompetitions.forEach(c => {
        if (c.nome.toLowerCase().includes(lowerQuery)) {
            results.push({ type: 'competicao', name: `${c.nome} ${c.temporada}`, id: c.id, label: 'Competição' });
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
        div.innerHTML = `
            <span class="search-item-name">${res.name}</span>
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

    // Competições que o time disputa (baseado nas partidas ou na associação da competição)
    // Vamos usar allCompetitions e ver onde o clube está
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
                        <span class="match-date">${formatDate(p.dataHora)}</span>
                        <span class="match-opponent">vs ${adversario.nome}</span>
                    </div>
                    <div class="match-score">${golsPro} - ${golsContra}</div>
                </div>
            `;
        }).join('');
    } else {
        partidasHtml = '<p>Nenhuma partida registrada.</p>';
    }

    let jogadoresHtml = jogadores.map(j => `
        <li class="clickable" onclick="navigateTo('jogador', ${j.id})">
            ${j.apelido || j.nomeCompleto} ${j.posicao ? `<small>(${j.posicao})</small>` : ''}
        </li>
    `).join('');

    let competicoesHtml = competicoes.map(c => `
        <li class="clickable" onclick="navigateTo('competicao', ${c.id})">
            ${c.nome} ${c.temporada}
        </li>
    `).join('');

    detailContent.innerHTML = `
        <h2>${clube.nome}</h2>
        <p class="details-meta">
            ${clube.cidade ? `Cidade: ${clube.cidade} &bull;` : ''}
            Estádio: <span class="clickable" onclick="navigateTo('estadio', ${clube.estadio?.id})">${clube.estadio?.nome || '—'}</span>
        </p>
        ${currentUser ? `<button class="btn-favorite" onclick="definirTimeCoracao(${clube.id})">❤️ Definir como Time do Coração</button>` : ''}

        <div class="details-grid">
            <div class="details-block">
                <h4>Últimas Partidas</h4>
                ${partidasHtml}
            </div>
            <div class="details-block">
                <h4>Elenco</h4>
                <ul>${jogadoresHtml || '<li>Sem jogadores cadastrados</li>'}</ul>
            </div>
            <div class="details-block">
                <h4>Competições</h4>
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
            ${e.gols} gols, ${e.assistencias} assistências
        </li>
    `).join('');

    detailContent.innerHTML = `
        <h2>${jogador.nomeCompleto}</h2>
        <p class="details-meta">
            Apelido: <strong>${jogador.apelido || '—'}</strong> &bull;
            Posição: <strong>${jogador.posicao || '—'}</strong> &bull;
            Time: <span class="clickable" onclick="navigateTo('clube', ${jogador.clube?.id})"><strong>${jogador.clube?.nome || '—'}</strong></span>
        </p>
        ${currentUser ? `<button class="btn-favorite" onclick="adicionarJogadorFavorito(${jogador.id})">⭐ Adicionar aos Favoritos</button>` : ''}

        <div class="details-grid">
            <div class="details-block">
                <h4>Estatísticas Totais</h4>
                <ul>
                    <li>Gols: ${jogador.golsTotais ?? 0}</li>
                    <li>Assistências: ${jogador.assistenciasTotais ?? 0}</li>
                    <li>Valor de Mercado: ${formatCurrency(jogador.valorDeMercado)}</li>
                </ul>
            </div>
            <div class="details-block">
                <h4>Por Competição</h4>
                <ul>${estatHtml || '<li>Sem estatísticas registradas</li>'}</ul>
            </div>
        </div>
    `;
}

function renderStadiumDetails(estadio) {
    // Encontrar dono do estádio (clube que tem este estádio)
    const dono = allClubes.find(c => c.estadio && c.estadio.id === estadio.id);

    detailContent.innerHTML = `
        <h2>${estadio.nome}</h2>
        <p class="details-meta">
            Cidade: <strong>${estadio.cidade}</strong> &bull;
            País: <strong>${estadio.pais}</strong>
        </p>

        <div class="details-grid">
            <div class="details-block">
                <h4>Time Mandante</h4>
                ${dono ? `
                    <p class="clickable" onclick="navigateTo('clube', ${dono.id})">
                        <strong>${dono.nome}</strong>
                    </p>
                ` : '<p>Nenhum time vinculado como mandante.</p>'}
            </div>
        </div>
    `;
}

function renderCompetitionDetails(competicao) {
    // Times da competição
    const times = competicao.clubes || [];

    // Partidas da competição
    // Assumindo que podemos filtrar partidas por competição se tivermos essa info no DTO de partida
    // Se não tiver, teremos que confiar que allPartidas tem tudo e tentar cruzar dados
    // Por enquanto, vamos listar os times

    let timesHtml = times.map(t => `
        <li class="clickable" onclick="navigateTo('clube', ${t.id})">
            ${t.nome}
        </li>
    `).join('');

    detailContent.innerHTML = `
        <h2>${competicao.nome} (${competicao.temporada})</h2>
        
        <div class="details-grid">
            <div class="details-block">
                <h4>Times Participantes</h4>
                <ul>${timesHtml || '<li>Sem times registrados</li>'}</ul>
            </div>
        </div>
    `;
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
        let icon = '🏆';
        if (name.toLowerCase().includes('brasileirão')) icon = '🇧🇷';
        else if (name.toLowerCase().includes('libertadores')) icon = '🌎';

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
        btn.textContent = season;
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
    if (!selectedCompetitionName) selectionHintEl.textContent = 'Selecione a competição para começar.';
    else if (!selectedSeason) selectionHintEl.textContent = 'Agora escolha a temporada.';
    else if (!selectedCategory) selectionHintEl.textContent = 'Perfeito! Agora escolha se quer ver times, jogadores ou estádios.';
    else selectionHintEl.textContent = `Explorando ${selectedCompetitionName} ${selectedSeason} — categoria: ${selectedCategory}.`;
}

function clearResults() {
    cardsContainerEl.innerHTML = '';
}

function showLoading() {
    cardsContainerEl.innerHTML = '<div class="loading">Carregando dados</div>';
}

function showError(message) {
    cardsContainerEl.innerHTML = `<div class="error-message">${message}</div>`;
}

function render() {
    clearResults();
    if (!selectedCompetition || !selectedSeason || !selectedCategory) {
        resultsTitleEl.textContent = 'Selecione competição, temporada e categoria.';
        resultsSubtitleEl.textContent = 'Use as opções acima para filtrar o que você quer ver.';
        return;
    }

    const comp = allCompetitions.find(c => c.id === selectedCompetition);
    if (!comp) return;

    const catLabel = {
        clubes: 'Times participantes',
        jogadores: 'Todos os jogadores cadastrados',
        estadios: 'Estádios'
    }[selectedCategory];

    resultsTitleEl.textContent = `${comp.nome} — ${selectedSeason}`;
    resultsSubtitleEl.textContent = catLabel;

    switch (selectedCategory) {
        case 'clubes': renderClubes(comp); break;
        case 'jogadores': renderJogadores(comp); break;
        case 'estadios': renderEstadios(); break;
    }
}

function renderClubes(comp) {
    if (isLoadingData) {
        cardsContainerEl.innerHTML = '<div class="loading">Carregando times...</div>';
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
                <div>
                    <div class="card-title">${clube.nome}</div>
                    <div class="results-subtitle">Cidade: ${clube.estadio?.cidade || '—'}</div>
                </div>
            </div>
            <div class="card-body">
                <p><span>País:</span> ${clube.pais}</p>
                <p><span>Estádio:</span> ${clube.estadio?.nome || '—'}</p>
            </div>
        </article>
    `).join('');
    cardsContainerEl.innerHTML = html;
}

function renderJogadores(comp) {
    if (isLoadingData) {
        cardsContainerEl.innerHTML = '<div class="loading">Carregando jogadores...</div>';
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
                <div class="card-title">${jogador.apelido || jogador.nomeCompleto}</div>
                <span class="badge">${jogador.posicao || '—'}</span>
            </div>
            <div class="card-body">
                <p><span>Time:</span> ${jogador.clube?.nome || '—'}</p>
                <p><span>Gols:</span> ${jogador.golsTotais ?? 0}</p>
                <p><span>Assistências:</span> ${jogador.assistenciasTotais ?? 0}</p>
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
        cardsContainerEl.innerHTML = '<div class="loading">Carregando estádios...</div>';
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
                <p><span>Cidade:</span> ${estadio.cidade}</p>
                <p><span>País:</span> ${estadio.pais}</p>
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
                <h4>${perfil.clubeFavorito.nome}</h4>
                <p>Cidade: ${perfil.clubeFavorito.cidade || '—'}</p>
                <p>Estádio: ${perfil.clubeFavorito.estadio?.nome || '—'}</p>
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
                <h4>${jogador.apelido || jogador.nomeCompleto}</h4>
                <p>Time: ${jogador.clube?.nome || '—'}</p>
                <p>Posição: ${jogador.posicao || '—'}</p>
                <p>Gols: ${jogador.golsTotais ?? 0} | Assistências: ${jogador.assistenciasTotais ?? 0}</p>
                <button class="btn-remove-favorite" onclick="event.stopPropagation(); removerJogadorFavorito(${jogador.id})">Remover</button>
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
