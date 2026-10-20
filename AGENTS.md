# AGENTS.md — ChatBoot Helder

Guia operacional do repositório **ChatBoot Helder**: [HelderAbud/ChatBoot-Helder](https://github.com/HelderAbud/ChatBoot-Helder). O nome DevDocs AI fica só nos documentos de planejamento. O pacote e o artefato continuam `com.chatboot` e `chatboot-helder`.

Nada entra em código sem pedido explícito. Cada decisão em aberto é apresentada com explicação e só vale depois da aprovação.

## Fontes

| Arquivo | Papel |
|---------|--------|
| `docs/contexto-devdocs-ai.md` | Visão, escopo do MVP, arquitetura proposta, segurança e critérios de aceite |
| `docs/analise-devdocs-ai.md` | Correções e riscos sobre esse plano (14/10/2026) |
| `AGENTS.md` | Decisões já aprovadas e como o agente trabalha neste repo |

Quando este arquivo registrar uma decisão fechada, ela prevalece sobre o trecho correspondente do contexto.

## O que o produto é

Assistente de documentação técnica para equipes. A pessoa pergunta em linguagem natural e recebe resposta apoiada em trechos dos documentos do próprio workspace, com citação verificável. Se a documentação não sustentar a resposta, o sistema diz isso.

Objetivos juntos: portfólio (Java, Spring Boot, REST, PostgreSQL, segurança, testes, RAG) e uma base que possa virar produto depois.

## Decisão fechada — ambiente (14/10/2026)

Aprovado: opção 1.

- **Ubuntu (WSL):** PostgreSQL, extensão pgvector e testes de integração.
- **Windows:** Cursor, JDK e Maven. Java confirmado no contexto: 21.0.10. Maven: 3.9.12.
- **Docker:** não há Docker Desktop no Windows. Se o projeto precisar de container, ele roda dentro do Ubuntu.
- Testcontainers, se for adotado, roda no Ubuntu. No Windows, testes que exigem container não são o caminho padrão.

O contexto (seção 10) descrevia PostgreSQL nativo no Windows e Docker só numa etapa posterior. Essa descrição fica substituída por esta decisão.

## Propostas do contexto — ainda não são licença para implementar

Estas linhas vêm do documento de contexto. Continuam como direção, até cada item em aberto ser fechado:

- Monólito modular em Spring Boot, não microserviços.
- API REST com documentação OpenAPI da própria API.
- PostgreSQL, pgvector, Flyway, Spring Security, Spring AI como implementação por trás de portas próprias (`EmbeddingPort`, `ChatPort`), se essa separação for aprovada.
- Modelos locais via Ollama no início. Sem API paga sem autorização.
- Documentos importados são conteúdo não confiável. Isolamento por workspace, citação verificável, abstenção sem evidência e testes são requisitos centrais.
- Formatos previstos no MVP completo: PDF com texto selecionável, Markdown, OpenAPI JSON e OpenAPI YAML. Importar OpenAPI indexa a documentação; não executa os endpoints.
- Fora do desenho inicial: microserviços, agentes que agem em sistemas externos, treino de modelo próprio, GitHub/GitLab/Jira, pagamento, OCR, chamada a API de terceiros a partir do OpenAPI.

## Decisão fechada — primeira fatia (14/10/2026)

Aprovado: opção 1, fatia fina.

Entra nesta entrega:

- Login simples.
- Um workspace.
- Somente Markdown.
- Upload, divisão em trechos e embedding.
- Uma pergunta com citação verificável, ou a resposta explícita de que não há evidência suficiente.

Fica para depois: PDF, OpenAPI, versionamento completo, três papéis e a tela React.

## Decisão fechada — forma da primeira entrega (14/10/2026)

Aprovado: só a API. Login, upload de Markdown, pergunta e resposta com citação ou abstenção saem por REST, Swagger e testes. A tela React entra numa fatia seguinte.

## Decisão fechada — arquivos Markdown (14/10/2026)

Aprovado: pasta local na máquina onde a API roda.

- O caminho vem da variável de ambiente `STORAGE_DIR`.
- O nome no disco é um UUID. O nome original fica só no banco, para a citação.
- A pasta não entra no Git.

## Decisão fechada — autenticação da primeira fatia (14/10/2026)

Aprovado: JWT de acesso com expiração de 30 minutos.

- A senha é armazenada com hash BCrypt.
- Não há refresh token.
- Não há endpoint de recuperação de senha.
- Token expirado exige novo login.

## Decisão fechada — versões (14/10/2026)

Aprovado: Java 21, Spring Boot 3.5.x, Spring AI 1.1.x, PostgreSQL 17 no Ubuntu e pgvector pelo pacote dessa mesma versão (`postgresql-17-pgvector`).

A patch exata de cada componente é a última estável dessa linha no dia em que o projeto for gerado.

## Decisão fechada — portas (14/10/2026)

Aprovado: API em **8087** e PostgreSQL publicado no host em **5436**.

- Dentro do Ubuntu o Postgres escuta na 5432. No PC a porta é 5436, para não colidir com o PostgreSQL 18 do Windows nem com 5433–5435 dos outros projetos.
- Swagger previsto: `http://localhost:8087/swagger-ui.html`.
- Esta fatia não reserva porta de tela.

## Decisão fechada — upload (14/10/2026)

Aprovado: até **2 MB** por arquivo Markdown, um arquivo por requisição.

- Só entra `.md`.
- Arquivo com byte nulo é recusado.
- Acima do limite, com outro tipo ou com byte nulo, a API explica a falha.

## Decisão fechada — evidência (14/10/2026)

Aprovado: duas portas.

- Sem trecho recuperado, ou com o melhor trecho abaixo de um mínimo em configuração, a API responde que não há evidência e não chama o modelo de geração.
- O número desse mínimo não fica fixo agora. Ele entra na configuração e só é ajustado com documento real.
- Quando há evidência, cada citação devolvida pelo modelo é conferida no servidor. Só vale trecho que estava na busca daquela pergunta.

## Decisão fechada — modelos (14/10/2026)

Aprovado em 14/10/2026, depois da medição:

- Geração: `qwen3:8b`, com thinking desligado.
- Embedding: `bge-m3`.
- Dimensão da coluna: `vector(1024)`.
- A medição está em `docs/acervo/farol-software/medicao-ollama.md`.
- Trocar o modelo de embedding exige reindexar.

## Decisão fechada — primeiro usuário (14/10/2026)

Aprovado: cadastro na API.

- Um `POST` de cadastro cria o usuário e o workspace dele.
- O login é outra chamada, depois do cadastro.
- Nesta fatia não há convite nem segundo workspace.

## Decisão fechada — acervo da primeira fatia (14/10/2026)

Além da API, esta fatia inclui documentos em Markdown para abastecer o chat. As categorias aprovadas são:

- manuais
- políticas internas
- catálogos
- procedimentos
- documentos de atendimento

Nesta fatia o formato continua sendo só `.md`. PDF e os outros formatos ficam para depois. Dados reais de cliente, telefone, documento ou conversa particular não entram no Git.

O ramo aprovado é a empresa fictícia **Farol Software**. Os cinco arquivos e as dez perguntas estão em `docs/acervo/farol-software/`.

## Decisões ainda em aberto

Nenhuma escolha de ramo pendente. O mínimo de evidência continua aberto.

A spec está em `docs/superpowers/specs/2026-10-14-fatia-1-api-markdown-design.md`.
O plano já executado está em `docs/superpowers/plans/2026-10-14-fatia-1-api-markdown.md`.
O plano da busca está em `docs/superpowers/plans/2026-10-14-fatia-1-busca.md`.

## Como trabalhar

- Triar antes de executar: Hotfix, Complex, Normal ou Simple.
- Tarefa relevante: plano curto, aprovação, fatia pequena, verificação, parada para o próximo sim.
- Não inventar contrato público (API, porta, papéis, schema) fora do que estiver escrito e aprovado.
- Não commitar, não dar push e não abrir PR sem pedido explícito.
- Não versionar `.env`, senha, token ou chave. Não pedir senha no chat.
- Autor dos commits, quando houver pedido: Helder Abud, sem `Co-authored-by: Cursor`.

## Estado do repositório

A `main` de referência desta linha é `b224223`. Nela já existem o acervo, o JWT, o upload com embedding, a busca no workspace e o `POST /api/v1/questions`. O mínimo de evidência continua `1.01`.

## Próximo passo

A medição está em `docs/acervo/farol-software/minimo-evidencia.md`. O mínimo continua `1.01` até o Helder escolher o número.
