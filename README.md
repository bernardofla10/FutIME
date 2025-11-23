# Futime API & Frontend

Este projeto consiste em uma API RESTful feita com Spring Boot (Java 21) e um Frontend simples em HTML/JS.

## Pré-requisitos

*   **Java 21 JDK**: Necessário para rodar o backend.
*   **VS Code**: Recomendado para editar e rodar o frontend.
*   **Extensão "Live Server" (VS Code)**: Para servir os arquivos do frontend.

## Como Rodar

### 1. Backend (API)

O backend deve ser iniciado primeiro para que a API esteja disponível.

No terminal (na raiz do projeto), execute:

**Windows:**
```cmd
gradlew.bat bootRun
```

**Linux / macOS:**
```bash
./gradlew bootRun
```

Aguarde até ver a mensagem de inicialização do Spring Boot. Por padrão, a API rodará em `http://localhost:8081` (conforme configurado no `frontend/app.js`).

### 2. Frontend

1.  Abra a pasta do projeto no **VS Code**.
2.  Navegue até a pasta `frontend`.
3.  Clique com o botão direito no arquivo `index.html`.
4.  Selecione a opção **"Open with Live Server"**.

O navegador abrirá automaticamente com a aplicação rodando.

---

**Nota**: Certifique-se de que a porta do backend configurada no `frontend/app.js` (`API_BASE`) corresponde à porta onde o Spring Boot está rodando (padrão 8081 neste projeto).

