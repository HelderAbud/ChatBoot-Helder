# Análise do DevDocs AI

Oct 14, 2026 · @Helder

## Avaliação geral

O documento de contexto é sólido: tem escopo claro, trata segurança e isolamento de dados como requisito central e exige abstenção e citações verificáveis. Os pontos abaixo estão ordenados por prioridade: correções, riscos não cobertos, melhorias de arquitetura e RAG, e dicas práticas.

## Correções

Seis pontos do documento precisam de ajuste antes de seguir para a implementação.

1. **Testcontainers exige Docker.** Ele não roda sem um runtime de containers, então a frase "não obriga a usar Docker no dia a dia" não se sustenta para testes de integração locais. Alternativas:
   - Local: banco de teste separado no PostgreSQL instalado (com pgvector).
   - CI: service container do GitHub Actions, que já tem Docker.
2. **Excluir documento × histórico com citações.** A seção 8 manda invalidar ou apagar os chunks, e a seção 3.2 pede histórico consultável com fontes. Se o chunk some, a citação da mensagem antiga quebra. Faça o `AnswerSource` guardar um **snapshot** (trecho citado, nome do documento, versão, localização) e defina a política de retenção.
3. **A ordem das fases atrasa o RAG.** Segurança, ingestão completa e versionamento vêm antes de qualquer pergunta e resposta. Esse é o maior risco do cronograma. Faça uma **fatia vertical fina cedo**: um workspace, só Markdown, upload, chunk, embedding, pergunta e resposta com citação. Depois expanda para PDF, OpenAPI, versionamento e papéis.
4. **O MVP está grande para um júnior.** Versionamento com rollback, três papéis, histórico de conversas e quatro formatos juntos é muito. Começar com "substituir e reindexar" no lugar de versionamento completo ainda cumpre o critério de não misturar conteúdo antigo e novo.
5. **Windows 10 sem suporte.** O suporte regular acabou em out/2025 e o ESU de consumidor encerra por volta de hoje. Vale conferir e considerar o Windows 11 se o hardware permitir.
6. **Critérios de aceite não mensuráveis.** "Ter testes" e "validar arquivos" não dizem quando está pronto. Defina, por exemplo: "tentativa de acesso cruzado entre workspaces retorna 403/404 em 100% dos testes" e "perguntas sem resposta geram abstenção em X de Y casos".

## Riscos não cobertos

O documento não trata seis riscos que afetam diretamente o MVP.

- **SSRF no OpenAPI:** parsers como o swagger-parser podem resolver `$ref` remotos. Desative a resolução remota. Isso se liga ao seu requisito de não chamar endpoints de terceiros.
- **YAML/JSON maliciosos:** limite aliases e profundidade (billion laughs) e use configuração segura do parser.
- **Upload:** limite de tamanho, verificação do tipo real do arquivo (não só da extensão), nome interno por UUID (evita path traversal) e timeout de processamento.
- **PDF sem texto:** detecte quando a extração volta vazia e retorne erro claro, já que não há OCR. Prefira PDFBox (Apache) a bibliotecas AGPL.
- **Processamento assíncrono:** os estados `PENDING/PROCESSING/...` pedem um job em segundo plano. Sem broker, uma tabela de jobs com `FOR UPDATE SKIP LOCKED` resolve. Trate também documentos presos em `PROCESSING` e defina retentativas.
- **LGPD:** se virar produto, e-mails e conversas são dados pessoais.

## Melhorias de arquitetura e RAG

A busca híbrida é o maior ganho de qualidade pelo menor custo; o restante reduz risco e retrabalho.

- **Busca híbrida:** documentação técnica tem muitos identificadores exatos (caminhos, códigos de erro, nomes de parâmetros), onde só vetores falham. Combine pgvector com busca textual do PostgreSQL (`tsvector`) e junte os rankings (RRF).
- **Chunking por formato:**
  - OpenAPI: um chunk por operação, com método e caminho no texto e nos metadados.
  - Markdown: por título/seção, mantendo blocos de código inteiros.
  - PDF: por página e seção.
- **Citações validadas:** peça ao modelo a resposta com IDs dos chunks e valide no servidor que cada ID pertence ao conjunto recuperado. Isso impede fonte inventada.
- **Modelo de embeddings:** guarde `embedding_model` e a dimensão por chunk. A dimensão fixa a coluna `vector(N)` na migration, e trocar de modelo exige reindexar.
- **Qwen3:** desative o modo de raciocínio (thinking) no RAG, senão a latência na CPU dispara. Teste os modelos por tokens/s e tempo até a primeira resposta com documentos reais.
- **Acesso a dados:** vetores com filtro em JPA costumam ser incômodos. Considere `JdbcTemplate` ou queries nativas para a busca, com JPA no restante.
- **Isolamento no banco:** use chaves compostas (`workspace_id` no chunk obrigatoriamente igual ao do documento) para garantir isolamento por constraint, não só por código. Deixe RLS para depois.
- **Desacoplamento:** crie suas próprias interfaces (`EmbeddingPort`, `ChatPort`) e use Spring AI só como implementação. A biblioteca muda rápido.
- **Modelo de dados:**
  - `active_version_id` no documento.
  - `UNIQUE(document_id, version_number)` e `UNIQUE(version_id, chunk_index)`.
  - Na resposta, registre modelo, versão do prompt e latência.
- **Avaliação:** com 30 perguntas, 85% tem margem de erro grande. Separe o conjunto respondível do não respondível, meça acerto top-k e MRR, e a taxa de abstenção correta. Amplie o conjunto ao longo do projeto.

## Dicas práticas

O maior risco de ambiente é o pgvector no Windows; valide-o antes de escrever qualquer código.

- **Pasta no Desktop:** no Windows, o Desktop costuma ser sincronizado pelo OneDrive, o que atrapalha `target/`, `.git` e `node_modules`. Mova para algo como `C:\dev\DevDocsAI`.
- **pgvector com PostgreSQL 18:** o caminho usual no Windows é compilar com Visual Studio Build Tools (C++); confirme a versão do pgvector com suporte ao 18. Se travar, avalie PostgreSQL 17 ou WSL2 antes de abrir mão de ficar sem Docker.
- **Versões:** gere o projeto em start.spring.io e confira no site do Spring AI qual versão é compatível com a versão do Spring Boot escolhida.
- **Contexto no Cursor:** 28 KB carregados a cada conversa gastam contexto à toa. Divida em `PROJECT.md`, `ARCHITECTURE.md` e `DECISIONS.md` (um ADR curto por decisão) e mantenha a seção 17 (estado do ambiente) separada, pois ela envelhece rápido. Depois de fechar as decisões, crie um `AGENTS.md` ou `.cursor/rules/` curto com as regras da seção 14.
- **Autenticação:** para o MVP, JWT de acesso com expiração curta é suficiente. Use BCrypt ou Argon2 e limite tentativas de login. Defina a matriz de permissões (quem pode fazer upload, excluir e gerenciar membros) antes de codar.
- **Observabilidade:** Spring Actuator, Micrometer e um correlation ID por requisição já bastam.
- **Portfólio:** inclua um README com diagrama, decisões e limitações. Para a demonstração, use documentos públicos (como o Petstore do OpenAPI). Mostre a taxa de acerto da avaliação e um caso de abstenção funcionando; isso pesa mais em entrevista do que uma lista de funcionalidades.
