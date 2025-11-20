// =======================
// CONFIGURAÇÃO DA API
// =======================
const API_BASE = 'http://localhost:8081';

// =======================
// ESTADO DA APLICAÇÃO
// =======================
let selectedCompetitionName = null; // Nome da competição selecionada (ex: "Brasileirão")
let selectedCompetition = null;     // ID da competição específica (Nome + Temporada)
let selectedSeason = null;
let selectedCategory = null;

let allCompetitions = [];
let allClubes = [];
let allJogadores = [];
let allEstadios = [];

const resultsTitleEl = document.getElementById('resultsTitle');
const resultsSubtitleEl = document.getElementById('resultsSubtitle');
const cardsContainerEl = document.getElementById('cardsContainer');
const detailsPanelEl = document.getElementById('detailsPanel');
const selectionHintEl = document.getElementById('selectionHint');

// =======================
// UTILITÁRIOS
// =======================

async function fetchData(endpoint) {
    const response = await fetch(`${API_BASE}${endpoint}`);
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
    try {
        const [clubes, jogadores, estadios] = await Promise.all([
            fetchData('/clubes'),
            fetchData('/jogadores'),
            fetchData('/estadios')
        ]);

        allClubes = clubes;
        allJogadores = jogadores;
        allEstadios = estadios;
    } catch (error) {
        console.error('Erro ao carregar dados gerais:', error);
        throw error;
    }
}

// =======================
// RENDERIZAÇÃO
// =======================

function renderCompetitionButtons() {
    const container = document.getElementById('competitionRow');
    container.innerHTML = '';

    if (allCompetitions.length === 0) {
        container.innerHTML = '<p style="color: #ff6b6b;">Nenhuma competição encontrada</p>';
        return;
    }

    // Agrupar competições por nome para não repetir botões
    const uniqueNames = [...new Set(allCompetitions.map(c => c.nome))];

    uniqueNames.forEach(name => {
        const btn = document.createElement('button');
        btn.className = 'pill';
        btn.dataset.competitionName = name;

        // Ícone baseado no nome da competição
        let icon = '🏆';
        if (name.toLowerCase().includes('brasileirão')) {
            icon = '🇧🇷';
        } else if (name.toLowerCase().includes('libertadores')) {
            icon = '🌎';
        }

        btn.textContent = `${icon} ${name}`;
        btn.addEventListener('click', () => selectCompetitionByName(name));
        container.appendChild(btn);
    });
}

function selectCompetitionByName(name) {
    selectedCompetitionName = name;

    // Atualiza UI da competição
    document.querySelectorAll('#competitionRow .pill').forEach(b => b.classList.remove('active'));
    const activeBtn = document.querySelector(`[data-competition-name="${name}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    // Encontrar temporadas disponíveis para este nome
    const variations = allCompetitions.filter(c => c.nome === name);

    // Renderizar botões de temporada dinamicamente
    renderSeasonButtons(variations);

    // Auto-selecionar a temporada mais recente
    if (variations.length > 0) {
        // Ordena decrescente (2025, 2024...)
        variations.sort((a, b) => b.temporada.localeCompare(a.temporada));
        selectSeason(variations[0].temporada);
    }
}

function renderSeasonButtons(variations) {
    const container = document.getElementById('seasonRow');
    container.innerHTML = '';

    // Extrair e ordenar temporadas
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

    // Atualiza UI da temporada
    document.querySelectorAll('#seasonRow .pill').forEach(b => b.classList.remove('active'));
    const activeBtn = document.querySelector(`[data-season="${season}"]`);
    if (activeBtn) activeBtn.classList.add('active');

    // Resolve o ID específico da competição (Nome + Temporada)
    if (selectedCompetitionName) {
        const targetComp = allCompetitions.find(c => c.nome === selectedCompetitionName && c.temporada === season);
        if (targetComp) {
            selectedCompetition = targetComp.id;
        }
    }

    updateHint();
    render();
}

function updateHint() {
    if (!selectedCompetitionName) {
        selectionHintEl.textContent = 'Selecione a competição para começar.';
    } else if (!selectedSeason) {
        selectionHintEl.textContent = 'Agora escolha a temporada.';
    } else if (!selectedCategory) {
        selectionHintEl.textContent = 'Perfeito! Agora escolha se quer ver times, jogadores ou estádios.';
    } else {
        selectionHintEl.textContent = `Explorando ${selectedCompetitionName} ${selectedSeason} — categoria: ${selectedCategory}.`;
    }
}

function clearResults() {
    cardsContainerEl.innerHTML = '';
    detailsPanelEl.classList.add('hidden');
    detailsPanelEl.innerHTML = '';
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
    if (!comp) {
        resultsTitleEl.textContent = 'Competição não encontrada';
        resultsSubtitleEl.textContent = '';
        return;
    }

    const catLabel = {
        clubes: 'Times participantes',
        jogadores: 'Todos os jogadores cadastrados',
        estadios: 'Estádios'
    }[selectedCategory];

    resultsTitleEl.textContent = `${comp.nome} — ${selectedSeason}`;
    resultsSubtitleEl.textContent = catLabel;

    switch (selectedCategory) {
        case 'clubes':
            renderClubes(comp);
            break;
        case 'jogadores':
            renderJogadores(comp);
            break;
        case 'estadios':
            renderEstadios();
            break;
    }
}

function renderClubes(comp) {
    // O DTO de competição já traz a lista de clubes, mas vamos garantir
    const clubesFiltered = comp.clubes || [];

    if (!clubesFiltered.length) {
        cardsContainerEl.innerHTML = '<p>Não há times cadastrados para essa temporada nesta competição.</p>';
        return;
    }

    const html = clubesFiltered.map(clube => `
        <article class="card clickable" data-clube-id="${clube.id}">
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

    document.querySelectorAll('[data-clube-id]').forEach(el => {
        el.addEventListener('click', () => {
            const id = parseInt(el.getAttribute('data-clube-id'));
            // Busca nos dados completos para ter certeza que temos tudo
            const clube = allClubes.find(c => c.id === id) || clubesFiltered.find(c => c.id === id);
            if (clube) renderClubeDetails(clube);
        });
    });
}

function renderClubeDetails(clube) {
    detailsPanelEl.classList.remove('hidden');
    detailsPanelEl.innerHTML = `
        <h3>${clube.nome}</h3>
        <p class="details-meta">
            País: <strong>${clube.pais}</strong> &bull;
            Cidade: <strong>${clube.estadio?.cidade || '—'}</strong> &bull;
            Estádio: <strong>${clube.estadio?.nome || '—'}</strong>
        </p>
        <div class="details-grid">
            <div class="details-block">
                <h4>Informações do Estádio</h4>
                <ul>
                    ${clube.estadio ? `
                        <li>Nome: ${clube.estadio.nome}</li>
                        <li>Cidade: ${clube.estadio.cidade}</li>
                        <li>País: ${clube.estadio.pais}</li>
                    ` : '<li>Sem informações de estádio</li>'}
                </ul>
            </div>
        </div>
    `;

    detailsPanelEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function renderJogadores(comp) {
    // Filtrar jogadores que pertencem aos clubes desta competição
    // O DTO de Jogador tem o objeto 'clube' completo
    const clubeIds = (comp.clubes || []).map(c => c.id);

    const jogadoresFiltered = allJogadores.filter(j =>
        j.clube && clubeIds.includes(j.clube.id)
    );

    if (!jogadoresFiltered.length) {
        cardsContainerEl.innerHTML = '<p>Não há jogadores cadastrados para essa temporada.</p>';
        return;
    }

    const html = jogadoresFiltered.map(jogador => `
        <article class="card clickable" data-jogador-id="${jogador.id}">
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

    document.querySelectorAll('[data-jogador-id]').forEach(el => {
        el.addEventListener('click', () => {
            const id = parseInt(el.getAttribute('data-jogador-id'));
            const jogador = jogadoresFiltered.find(j => j.id === id);
            if (jogador) renderJogadorDetails(jogador);
        });
    });
}

function renderJogadorDetails(jogador) {
    detailsPanelEl.classList.remove('hidden');

    const estatisticasPorCompeticao = jogador.estatisticasPorCompeticao || [];
    const estatHtml = estatisticasPorCompeticao.length > 0
        ? estatisticasPorCompeticao.map(e => `
            <li>${e.nomeCompeticao || 'Competição'}: <strong>${e.gols || 0}</strong> gols, <strong>${e.assistencias || 0}</strong> assistências</li>
        `).join('')
        : '<li>Sem estatísticas por competição</li>';

    detailsPanelEl.innerHTML = `
        <h3>${jogador.nomeCompleto}</h3>
        <p class="details-meta">
            Apelido: <strong>${jogador.apelido || '—'}</strong> &bull;
            Posição: <strong>${jogador.posicao || '—'}</strong> &bull;
            Time: <strong>${jogador.clube?.nome || '—'}</strong> &bull;
            Nascimento: <strong>${formatDate(jogador.dataNascimento)}</strong>
        </p>
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
                <h4>Estatísticas por Competição</h4>
                <ul>
                    ${estatHtml}
                </ul>
            </div>
        </div>
    `;

    detailsPanelEl.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function renderEstadios() {
    if (!allEstadios.length) {
        cardsContainerEl.innerHTML = '<p>Não há estádios cadastrados.</p>';
        return;
    }

    const html = allEstadios.map(estadio => `
        <article class="card">
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
// LISTENERS DE INTERFACE
// =======================

// Temporada: Listeners são adicionados dinamicamente em renderSeasonButtons

// Categoria
document.querySelectorAll('#categoryRow .pill').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('#categoryRow .pill').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        selectedCategory = btn.dataset.category;
        updateHint();
        render();
    });
});

// =======================
// INICIALIZAÇÃO
// =======================

async function init() {
    showLoading();
    try {
        await loadCompeticoes();
        await loadAllData();
        clearResults();
        resultsTitleEl.textContent = 'Bem-vindo ao FutIME';
        resultsSubtitleEl.textContent = 'Selecione os filtros acima para começar a explorar.';
    } catch (error) {
        showError('Erro ao carregar dados iniciais. Verifique se o backend está rodando.');
        console.error('Erro na inicialização:', error);
    }
}

// Iniciar quando a página carregar
window.addEventListener('DOMContentLoaded', init);
