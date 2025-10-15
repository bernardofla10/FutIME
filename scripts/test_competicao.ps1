param(
    [string]$BaseUrl = $env:FUTIME_API_BASE_URL
)

if (-not $BaseUrl -or $BaseUrl.Trim() -eq "") { $BaseUrl = "http://localhost:8081" }

function Post-Json($Url, $BodyObj) {
    $json = $BodyObj | ConvertTo-Json -Depth 6
    try {
        return Invoke-RestMethod -Method Post -Uri $Url -ContentType 'application/json; charset=utf-8' -Body $json -ErrorAction Stop
    } catch {
        $resp = $_.Exception.Response
        $errMsg = $null
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) { $errMsg = $_.ErrorDetails.Message }
        if ($resp) {
            $httpResp = [System.Net.HttpWebResponse]$resp
            $stream = $httpResp.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $body = $reader.ReadToEnd()
            Write-Host "ERRO POST $Url" -ForegroundColor Red
            Write-Host ("Status: {0} {1}" -f [int]$httpResp.StatusCode, $httpResp.StatusDescription) -ForegroundColor Red
            if ($errMsg) { Write-Host "ErrorDetails: $errMsg" -ForegroundColor Yellow }
            if ($body) { Write-Host "Body: $body" -ForegroundColor Yellow }
        } else {
            Write-Host $_ -ForegroundColor Red
        }
        throw
    }
}

function Remove-Diacritics($str) {
    if ($null -eq $str) { return $null }
    $normalized = $str.Normalize([Text.NormalizationForm]::FormD)
    $sb = New-Object System.Text.StringBuilder
    foreach ($ch in $normalized.ToCharArray()) {
        if ([Globalization.CharUnicodeInfo]::GetUnicodeCategory($ch) -ne [Globalization.UnicodeCategory]::NonSpacingMark) {
            [void]$sb.Append($ch)
        }
    }
    return $sb.ToString().Normalize([Text.NormalizationForm]::FormC)
}

function Sanitize-ObjectStrings($obj) {
    if ($null -eq $obj) { return $null }
    if ($obj -is [System.Collections.IDictionary]) {
        foreach ($key in @($obj.Keys)) {
            $obj[$key] = Sanitize-ObjectStrings $obj[$key]
        }
        return $obj
    } elseif ($obj -is [System.Collections.IEnumerable] -and -not ($obj -is [string])) {
        $list = @()
        foreach ($item in $obj) { $list += (Sanitize-ObjectStrings $item) }
        return ,$list
    } elseif ($obj -is [string]) {
        return (Remove-Diacritics $obj)
    } else {
        return $obj
    }
}

function Put-Json($Url, $BodyObj) {
    $json = $BodyObj | ConvertTo-Json -Depth 6
    try {
        return Invoke-RestMethod -Method Put -Uri $Url -ContentType 'application/json; charset=utf-8' -Body $json -ErrorAction Stop
    } catch {
        $resp = $_.Exception.Response
        $errMsg = $null
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) { $errMsg = $_.ErrorDetails.Message }
        if ($resp) {
            $httpResp = [System.Net.HttpWebResponse]$resp
            $stream = $httpResp.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $body = $reader.ReadToEnd()
            Write-Host "ERRO PUT $Url" -ForegroundColor Red
            Write-Host ("Status: {0} {1}" -f [int]$httpResp.StatusCode, $httpResp.StatusDescription) -ForegroundColor Red
            if ($errMsg) { Write-Host "ErrorDetails: $errMsg" -ForegroundColor Yellow }
            if ($body) { Write-Host "Body: $body" -ForegroundColor Yellow }
        } else {
            Write-Host $_ -ForegroundColor Red
        }
        throw
    }
}

Write-Host "Base: $BaseUrl"

$estadiosPayload = @(
    @{ nome = "Allianz Parque";        cidade = "São Paulo";      pais = "BRA" }
    @{ nome = "Maracanã";              cidade = "Rio de Janeiro"; pais = "BRA" }
    @{ nome = "Arena MRV";             cidade = "Belo Horizonte"; pais = "BRA" }
    @{ nome = "Arena do Grêmio";       cidade = "Porto Alegre";   pais = "BRA" }
    @{ nome = "Nilton Santos";         cidade = "Rio de Janeiro"; pais = "BRA" }
    @{ nome = "Nabi Abi Chedid";       cidade = "Bragança Paulista"; pais = "BRA" }
    @{ nome = "Ligga Arena";           cidade = "Curitiba";       pais = "BRA" }
    @{ nome = "Morumbi";               cidade = "São Paulo";      pais = "BRA" }
    @{ nome = "Neo Química Arena";     cidade = "São Paulo";      pais = "BRA" }
    @{ nome = "Arena Fonte Nova";      cidade = "Salvador";       pais = "BRA" }
    @{ nome = "Castelão";              cidade = "Fortaleza";      pais = "BRA" }
    @{ nome = "Mineirão";              cidade = "Belo Horizonte"; pais = "BRA" }
    @{ nome = "São Januário";          cidade = "Rio de Janeiro"; pais = "BRA" }
    @{ nome = "Beira-Rio";             cidade = "Porto Alegre";   pais = "BRA" }
    @{ nome = "Alfredo Jaconi";        cidade = "Caxias do Sul";  pais = "BRA" }
    @{ nome = "Heriberto Hülse";       cidade = "Criciúma";       pais = "BRA" }
    @{ nome = "Arena Pantanal";        cidade = "Cuiabá";         pais = "BRA" }
    @{ nome = "Antônio Accioly";       cidade = "Goiânia";        pais = "BRA" }
    @{ nome = "Barradão";              cidade = "Salvador";       pais = "BRA" }
)

$clubeParaEstadio = @{
    "Palmeiras"            = "Allianz Parque"
    "Flamengo"             = "Maracanã"
    "Atlético Mineiro"     = "Arena MRV"
    "Grêmio"               = "Arena do Grêmio"
    "Fluminense"           = "Maracanã"
    "Botafogo"             = "Nilton Santos"
    "Red Bull Bragantino"  = "Nabi Abi Chedid"
    "Athletico Paranaense" = "Ligga Arena"
    "São Paulo"            = "Morumbi"
    "Corinthians"          = "Neo Química Arena"
    "Bahia"                = "Arena Fonte Nova"
    "Fortaleza"            = "Castelão"
    "Cruzeiro"             = "Mineirão"
    "Vasco da Gama"        = "São Januário"
    "Internacional"        = "Beira-Rio"
    "Juventude"            = "Alfredo Jaconi"
    "Criciúma"             = "Heriberto Hülse"
    "Cuiabá"               = "Arena Pantanal"
    "Atlético Goianiense"  = "Antônio Accioly"
    "Vitória"              = "Barradão"
}

Write-Host "Criando estádios..."
$estadioIdPorNome = @{}
foreach ($e in $estadiosPayload) {
    try {
        $payload = Sanitize-ObjectStrings ([Hashtable]$e.Clone())
        $createdE = Post-Json "$BaseUrl/estadios" $payload
        if ($createdE.id -and $createdE.nome) { $estadioIdPorNome[$createdE.nome] = [int]$createdE.id }
    } catch {
        Write-Host "Falha ao criar estádio: $($e.nome)" -ForegroundColor Red
        throw
    }
}

$clubesPayload = @(
    @{ nome = "Palmeiras";           sigla = "PAL"; cidade = "São Paulo";      pais = "Brasil"; estadioId = $null }
    @{ nome = "Flamengo";            sigla = "FLA"; cidade = "Rio de Janeiro"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Atlético Mineiro";    sigla = "CAM"; cidade = "Belo Horizonte"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Grêmio";              sigla = "GRE"; cidade = "Porto Alegre";   pais = "Brasil"; estadioId = $null }
    @{ nome = "Fluminense";          sigla = "FLU"; cidade = "Rio de Janeiro"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Botafogo";            sigla = "BOT"; cidade = "Rio de Janeiro"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Red Bull Bragantino"; sigla = "RBB"; cidade = "Bragança Paulista"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Athletico Paranaense";sigla = "CAP"; cidade = "Curitiba";       pais = "Brasil"; estadioId = $null }
    @{ nome = "São Paulo";           sigla = "SAO"; cidade = "São Paulo";      pais = "Brasil"; estadioId = $null }
    @{ nome = "Corinthians";         sigla = "COR"; cidade = "São Paulo";      pais = "Brasil"; estadioId = $null }
    @{ nome = "Bahia";               sigla = "BAH"; cidade = "Salvador";       pais = "Brasil"; estadioId = $null }
    @{ nome = "Fortaleza";           sigla = "FOR"; cidade = "Fortaleza";      pais = "Brasil"; estadioId = $null }
    @{ nome = "Cruzeiro";            sigla = "CRU"; cidade = "Belo Horizonte"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Vasco da Gama";       sigla = "VAS"; cidade = "Rio de Janeiro"; pais = "Brasil"; estadioId = $null }
    @{ nome = "Internacional";       sigla = "INT"; cidade = "Porto Alegre";   pais = "Brasil"; estadioId = $null }
    @{ nome = "Juventude";           sigla = "JUV"; cidade = "Caxias do Sul";  pais = "Brasil"; estadioId = $null }
    @{ nome = "Criciúma";            sigla = "CRI"; cidade = "Criciúma";       pais = "Brasil"; estadioId = $null }
    @{ nome = "Cuiabá";              sigla = "CUI"; cidade = "Cuiabá";         pais = "Brasil"; estadioId = $null }
    @{ nome = "Atlético Goianiense"; sigla = "ACG"; cidade = "Goiânia";        pais = "Brasil"; estadioId = $null }
    @{ nome = "Vitória";             sigla = "VIT"; cidade = "Salvador";       pais = "Brasil"; estadioId = $null }
)

$clubIds = @()
Write-Host "Criando clubes..."
foreach ($c in $clubesPayload) {
    try {
        $nomeEstadio = $clubeParaEstadio[$c.nome]
        if (-not $nomeEstadio) { throw "Sem mapeamento de estádio para o clube '$($c.nome)'" }
        $idEstadio = $estadioIdPorNome[$nomeEstadio]
        if (-not $idEstadio) { throw "Estádio '$nomeEstadio' não foi criado" }
        $c.estadioId = [int]$idEstadio
        $cPayload = Sanitize-ObjectStrings ([Hashtable]$c.Clone())
        $created = Post-Json "$BaseUrl/clubes" $cPayload
        if ($created.id) { $clubIds += [int]$created.id }
    } catch {
        Write-Host "Falha ao criar clube: $($c.nome)" -ForegroundColor Red
        throw
    }
}
Write-Host "Clubes criados: $($clubIds.Count)"

Write-Host "Criando competição..."
$competicaoBody = @{
    nome = "Campeonato Brasileiro Série A"
    pais = "Brasil"
    continente = "América do Sul"
    tipoCompeticao = "PONTOS_CORRIDOS"
    temporada = "2025"
    clubeIds = $clubIds
}

$competicaoCreated = Post-Json "$BaseUrl/competicoes" $competicaoBody
$competicaoId = [int]$competicaoCreated.id
Write-Host "Competição criada: ID $competicaoId"

Write-Host "Listando competições..."
$competicoes = Invoke-RestMethod -Method Get -Uri "$BaseUrl/competicoes" -ErrorAction Stop
Write-Host ("Total: {0}" -f ($competicoes | Measure-Object | Select-Object -ExpandProperty Count))

Write-Host "Buscando competição por ID..."
$byId = Invoke-RestMethod -Method Get -Uri "$BaseUrl/competicoes/$competicaoId" -ErrorAction Stop
Write-Host "OK: $($byId.nome)"

Write-Host "Atualizando competição..."
$updateBody = @{
    nome = "Campeonato Brasileiro Série A - Atualizado"
    pais = $competicaoBody.pais
    continente = $competicaoBody.continente
    tipoCompeticao = $competicaoBody.tipoCompeticao
    temporada = $competicaoBody.temporada
    clubeIds = $competicaoBody.clubeIds
}
$updated = Put-Json "$BaseUrl/competicoes/$competicaoId" $updateBody
Write-Host "Atualizada: $($updated.nome)"

Write-Host "Testando id inexistente (GET e DELETE)..."
$inexistente = 999999
try {
    Invoke-RestMethod -Method Get -Uri "$BaseUrl/competicoes/$inexistente" -ErrorAction Stop | Out-Null
    Write-Host "GET inexistente retornou 200" -ForegroundColor Yellow
} catch {
    Write-Host "GET inexistente falhou como esperado" -ForegroundColor Green
}

Write-Host "Excluindo competição criada..."
$delResp = Invoke-WebRequest -Method Delete -Uri "$BaseUrl/competicoes/$competicaoId" -ErrorAction SilentlyContinue
if ($delResp -and $delResp.StatusCode) {
    Write-Host "DELETE status: $($delResp.StatusCode)"
} else {
    Write-Host "DELETE concluído"
}

Write-Host "Tentando excluir novamente (deve falhar)..."
$delAgain = Invoke-WebRequest -Method Delete -Uri "$BaseUrl/competicoes/$competicaoId" -ErrorAction SilentlyContinue
if ($delAgain -and $delAgain.StatusCode) {
    Write-Host "DELETE repetido status: $($delAgain.StatusCode)"
} else {
    Write-Host "DELETE repetido sem status"
}

Write-Host "Fim"


