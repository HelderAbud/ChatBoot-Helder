# Fatia 1b — embedding, busca e pergunta

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Gravar o vetor de cada trecho, buscar só no workspace do token e responder uma pergunta com citação conferida ou com a frase fixa de abstenção.

**Architecture:** `EmbeddingPort` e `ChatPort` isolam o Ollama. `DocumentService` pede o vetor e grava em `vector(1024)`. `QuestionService` busca, passa o resultado no `EvidenceGate` e só então chama o chat. Citação fora da busca é descartada pelo `CitationChecker`. O mínimo de similaridade continua configuração; o padrão seguro abstém até um teste com o acervo da Farol.

**Tech Stack:** Java 21, Spring Boot 3.5.16, Spring AI 1.1.4 com Ollama, Flyway, PostgreSQL 17 e pgvector no Ubuntu, porta host 5436. Ollama 0.40.4 em `http://localhost:11434`.

## Global Constraints

- Modelos travados pela medição de 14/10/2026: geração `qwen3:8b` com thinking desligado; embedding `bge-m3`; dimensão **1024**.
- Trocar o modelo de embedding exige reindexar. Não criar outra dimensão nesta fatia.
- API na porta 8087. Postgres no host na porta 5436. Dentro do Ubuntu, 5432.
- Teste unitário no Windows com `.\mvnw.cmd`. Não chamar Ollama nem Postgres nesses testes.
- Frase de abstenção, exatamente: `Não encontrei informação confiável na documentação disponível deste workspace.`
- Sem trecho, ou com o melhor score abaixo do mínimo, `abstained` é `true`, `citations` é vazio e `ChatPort` não é chamado.
- Score igual ao mínimo responde.
- Citação cujo id não estava na busca daquela pergunta não entra na resposta.
- Documento ou pergunta de outro workspace responde 404.
- Ollama indisponível no upload: status `FAILED`, HTTP 503, falha persistida.
- Ollama indisponível na pergunta, depois de haver evidência: HTTP 503, sem texto inventado.
- Upload concluído com embedding responde 201 e status `COMPLETED`.
- `TEXT_STORED` continua válido para trecho ainda sem vetor. Não apagar esse status.
- Mínimo de evidência: propriedade `chatboot.evidence.minimum-score`. Sem valor no ambiente, usar `1.01`, que abstém porque a similaridade de cosseno não passa de 1. Não tratar `1.01` como número medido no acervo.
- Documento importado é dado não confiável. Senha, token e texto integral não entram em log.
- Não executar `git commit`, push ou PR sem pedido explícito do Helder.
- Não há fila, tela, PDF nem segundo turno de conversa.

---

## Antes da tarefa 1

A branch `feat/security-swagger` ainda não está na `main`. Ela contém `SecurityConfig`, o teste de isolamento e `docs/acervo/farol-software/medicao-ollama.md`. Este plano começa numa branch nova a partir da `main` que já tiver esse merge. Não misturar a migration `vector(1024)` nesse PR.

## Mapa de arquivos

| Arquivo | Responsabilidade |
|---------|------------------|
| `AGENTS.md` | Registrar o par medido |
| `V2__chunk_embeddings.sql` | Coluna `vector(1024)`, status `COMPLETED` e `failure_message` |
| `EmbeddingPort` | Pedir um vetor sem conhecer o Ollama |
| `ChunkEmbeddingStore` | Gravar o vetor de um trecho |
| `DocumentService` | Cortar, embedar e devolver `COMPLETED` ou `FAILED` |
| `AuthService` | Persistir usuário, senha BCrypt e o workspace |
| `ChunkSearch` | Buscar trechos do workspace por similaridade |
| `ChatPort` | Gerar resposta só quando há evidência |
| `QuestionService` | Aplicar o portão, filtrar citação e gravar a pergunta |

---

### Task 1: Registrar o par medido

**Files:**
- Modify: `AGENTS.md`
- Modify: `docs/acervo/farol-software/medicao-ollama.md`

**Interfaces:**
- Consumes: `medicao-ollama.md` com `qwen3:8b`, thinking desligado, `bge-m3` e dimensão 1024
- Produces: o contrato escrito que as tarefas seguintes não podem contradizer

- [x] **Step 1: Atualizar a decisão de modelos**

Na seção "Decisão fechada — modelos", substituir o texto de "testar antes de travar" por:

```markdown
Aprovado em 14/10/2026, depois da medição:

- Geração: `qwen3:8b`, com thinking desligado.
- Embedding: `bge-m3`.
- Dimensão da coluna: `vector(1024)`.
- A medição está em `docs/acervo/farol-software/medicao-ollama.md`.
- Trocar o modelo de embedding exige reindexar.
```

Em "Decisões ainda em aberto", deixar só o mínimo de evidência. O par de modelos sai dessa lista.

- [x] **Step 2: Fechar a ressalva da medição**

Trocar a última frase de `medicao-ollama.md` por: "Par registrado no AGENTS.md em 14/10/2026. O mínimo de evidência continua em aberto."

- [x] **Step 3: Parar para o Helder conferir o texto**

Não criar migration nesta tarefa.

---

### Task 2: Coluna vetorial

**Files:**
- Create: `src/main/resources/db/migration/V2__chunk_embeddings.sql`

**Interfaces:**
- Consumes: tabelas `documents` e `document_chunks` da V1
- Produces: `document_chunks.embedding vector(1024)` e `documents.failure_message`

- [x] **Step 1: Escrever a migration**

```sql
create extension if not exists vector;

alter table documents
    add column failure_message text;

alter table documents
    drop constraint documents_status_check;

alter table documents
    add constraint documents_status_check
    check (status in ('TEXT_STORED', 'COMPLETED', 'FAILED'));

alter table document_chunks
    add column embedding vector(1024);
```

- [x] **Step 2: Não aplicar daqui se a branch de segurança ainda não foi mergeada**

A V1 já está no banco `chatboot` do Ubuntu. A V2 só roda quando a API subir depois do merge. Não editar a V1.

- [x] **Step 3: Commit, só se o Helder pedir**

```bash
git add src/main/resources/db/migration/V2__chunk_embeddings.sql
git commit -m "feat: add a 1024-dimension embedding column"
```

---

### Task 3: Upload pede embedding e grava COMPLETED ou FAILED

**Files:**
- Create: `src/main/java/com/chatboot/assistant/EmbeddingPort.java`
- Create: `src/main/java/com/chatboot/assistant/EmbeddingUnavailableException.java`
- Create: `src/main/java/com/chatboot/documents/ChunkEmbeddingStore.java`
- Modify: `src/main/java/com/chatboot/documents/DocumentService.java`
- Modify: `src/main/java/com/chatboot/documents/DocumentController.java`
- Modify: `src/main/java/com/chatboot/documents/DocumentEntity.java`
- Test: `src/test/java/com/chatboot/documents/DocumentServiceTest.java`
- Test: `src/test/java/com/chatboot/documents/DocumentControllerTest.java`

**Interfaces:**
- Consumes: `MarkdownChunker.chunk`, `LocalFileStorage.store`
- Produces: `EmbeddingPort.embed(String text)` devolve `float[]` de tamanho 1024 ou lança `EmbeddingUnavailableException`. `ChunkEmbeddingStore.save(UUID chunkId, float[] embedding)`. `DocumentService.store` devolve `StoredDocument` com status `COMPLETED` ou `FAILED`.

- [x] **Step 1: Teste que falha quando o embedding não existe**

```java
@Test
void marksDocumentFailedWhenEmbeddingIsUnavailable() {
    when(storage.store(any())).thenReturn(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
    when(chunker.chunk(any())).thenReturn(List.of(new MarkdownChunk("Plano Farol", "R$ 99")));
    when(embeddings.embed("Plano Farol\nR$ 99")).thenThrow(new EmbeddingUnavailableException());

    StoredDocument stored = service.store(workspaceId, "catalogo-planos.md", "# Plano Farol\nR$ 99".getBytes());

    assertEquals("FAILED", stored.status());
    verify(vectors, never()).save(any(), any());
}
```

O teste seguinte pede `COMPLETED` e uma chamada a `vectors.save` com um vetor de 1024 posições quando `embed` devolve esse vetor.

- [x] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=DocumentServiceTest test`  
Expected: FAIL, `EmbeddingPort` ausente.

- [x] **Step 3: Implementar o mínimo**

`DocumentService.store` continua gravando o arquivo e os trechos. Para cada trecho, chama `embeddings.embed(heading + "\n" + body)`. Se voltar o vetor, chama `vectors.save`. No fim, status `COMPLETED`. Se `EmbeddingUnavailableException`, grava o documento com status `FAILED` e `failure_message` curta `embedding unavailable`, e devolve `FAILED` sem relançar. A transação precisa commitar o `FAILED`.

O controller, no sucesso com `COMPLETED`, responde 201. Se o status devolvido for `FAILED`, responde 503. `GET /api/v1/documents/{id}` passa a devolver `documentId`, `originalName`, `status` e `failureMessage`. Documento de outro workspace continua 404.

- [x] **Step 4: Rodar**

Run: `.\mvnw.cmd "-Dtest=DocumentServiceTest,DocumentControllerTest" test`  
Expected: PASS. O teste antigo do PDF continua 400 e não chama o serviço.

- [x] **Step 5: Commit, só se o Helder pedir**

```bash
git commit -m "feat: mark an upload failed when embedding is unavailable"
```

---

### Task 4: Cadastro persiste usuário e workspace

**Files:**
- Create: `src/main/java/com/chatboot/identity/UserAccount.java`
- Create: `src/main/java/com/chatboot/identity/WorkspaceEntity.java`
- Create: `src/main/java/com/chatboot/identity/UserAccountRepository.java`
- Create: `src/main/java/com/chatboot/identity/WorkspaceRepository.java`
- Modify: `src/main/java/com/chatboot/identity/AuthService.java`
- Test: `src/test/java/com/chatboot/identity/AuthServiceTest.java`

**Interfaces:**
- Consumes: tabelas `users` e `workspaces` da V1. `PasswordPolicy` já rejeita senha curta no controller.
- Produces: `AuthService.register(email, password, workspaceName)` grava os dois e devolve `RegisteredUser`. `login` devolve `IssuedToken` ou lança credencial inválida. Senha persistida é hash BCrypt, nunca o texto.

- [x] **Step 1: Teste com repositórios falsos**

E-mail novo: `register` chama `save` do usuário e do workspace, e o hash não é igual à senha `senha-ok`. E-mail já existente: lança `DuplicateEmailException` e não salva workspace. Login com senha errada não emite token.

- [x] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=AuthServiceTest test`  
Expected: FAIL, porque `register` ainda lança `UnsupportedOperationException`.

- [x] **Step 3: Implementar**

`BCryptPasswordEncoder` com strength 10. `existsByEmail` antes do insert. O id do workspace gerado no Java entra no JWT que o `JwtService` já emite. Login usa `encoder.matches`. Não logar a senha.

- [x] **Step 4: Rodar**

Run: `.\mvnw.cmd "-Dtest=AuthServiceTest,AuthControllerTest" test`  
Expected: PASS. O controller segue com o serviço mockado; senha curta continua 400 sem chamar o serviço.

- [x] **Step 5: Commit, só se o Helder pedir**

```bash
git commit -m "feat: persist the user and workspace on registration"
```

---

### Task 5: Pergunta abstem ou cita só trecho recuperado

**Files:**
- Create: `src/main/java/com/chatboot/assistant/ScoredChunk.java`
- Create: `src/main/java/com/chatboot/assistant/ChunkSearch.java`
- Create: `src/main/java/com/chatboot/assistant/ChatPort.java`
- Create: `src/main/java/com/chatboot/assistant/ChatDraft.java`
- Create: `src/main/java/com/chatboot/assistant/QuestionService.java`
- Create: `src/main/java/com/chatboot/assistant/QuestionController.java`
- Test: `src/test/java/com/chatboot/assistant/QuestionServiceTest.java`

**Interfaces:**
- Consumes: `EvidenceGate.decide`, `CitationChecker.keep`, `EmbeddingPort.embed`
- Produces: `POST /api/v1/questions` com corpo `{ "question": "..." }`. Resposta 200: `answer`, `abstained`, `citations`. Cada citação tem `chunkId`, `documentName` e `heading`. `ScoredChunk` é `record ScoredChunk(String id, String documentName, String heading, String body, double score)`.

- [x] **Step 1: Teste de abstenção**

Busca vazia. O serviço responde a frase fixa, `abstained` true, citações vazias, e `verify(chat, never()).answer(any(), any())`.

Segundo teste: um trecho com score igual ao mínimo `0.50`. O chat devolve o id desse trecho e mais um id `fora-da-busca`. A resposta fica `abstained` false e a lista de citações contém só o id recuperado, com o nome `catalogo-planos.md` e o título `Plano Farol`.

Terceiro teste: a busca tem score `0.49` e o mínimo é `0.50`. Abstem e não chama o chat.

- [x] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=QuestionServiceTest test`  
Expected: FAIL, classe ausente.

- [x] **Step 3: Implementar**

`QuestionService.ask(UUID userId, UUID workspaceId, String question, double minimumScore)` embeda a pergunta, busca com limite 5, monta `RetrievedChunk` sem o corpo e chama `EvidenceGate`. No `ABSTAIN`, devolve a frase fixa. No `ANSWER`, chama `ChatPort.answer(question, chunks)`. `CitationChecker.keep` filtra os ids. A citação publicada copia `documentName` e `heading` do `ScoredChunk` recuperado.

O controller lê `sub` e `workspaceId` do JWT. A busca filtra por esse workspace. A pergunta e a resposta são gravadas com `user_id` e `workspace_id`, junto das citações aceitas. Não há endpoint que leia pergunta de outro workspace. Id citado fora da busca simplesmente some.

Se `EmbeddingPort` ou `ChatPort` lançar `EmbeddingUnavailableException` depois que o portão decidiu `ANSWER`, o controller responde 503 e não grava resposta inventada. Na abstenção, Ollama de geração não é chamado mesmo que esteja fora.

- [x] **Step 4: Rodar**

Run: `.\mvnw.cmd "-Dtest=QuestionServiceTest,EvidenceGateTest,CitationCheckerTest" test`  
Expected: PASS.

- [x] **Step 5: Commit, só se o Helder pedir**

```bash
git commit -m "feat: abstain or keep only citations from the retrieved set"
```

---

### Task 6: Adaptadores Ollama e busca no pgvector

**Files:**
- Create: `src/main/java/com/chatboot/assistant/OllamaEmbeddingAdapter.java`
- Create: `src/main/java/com/chatboot/assistant/OllamaChatAdapter.java`
- Create: `src/main/java/com/chatboot/documents/JdbcChunkEmbeddingStore.java`
- Create: `src/main/java/com/chatboot/assistant/JdbcChunkSearch.java`
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yml`

**Interfaces:**
- Consumes: as portas das tarefas 3 e 5
- Produces: beans que fecham as portas. Busca SQL restrita a `workspace_id`. Chat com `think` desligado.

- [ ] **Step 1: Dependência**

No `pom.xml`, importar o BOM `org.springframework.ai:spring-ai-bom:1.1.4` e adicionar `spring-ai-starter-model-ollama`. Não adicionar o vector store pronto do Spring AI. A busca é SQL deste projeto.

- [ ] **Step 2: Configuração**

```yaml
chatboot:
  ollama:
    base-url: ${CHATBOOT_OLLAMA_URL:http://localhost:11434}
    embedding-model: bge-m3
    chat-model: qwen3:8b
  evidence:
    minimum-score: ${CHATBOOT_EVIDENCE_MINIMUM:1.01}
```

- [ ] **Step 3: Gravação e busca**

`JdbcChunkEmbeddingStore.save` executa `update document_chunks set embedding = cast(:embedding as vector) where id = :id`. O texto do vetor é a lista de floats entre colchetes.

`JdbcChunkSearch.search` executa apenas trechos do workspace, com vetor não nulo:

```sql
select c.id, d.original_name, c.heading, c.body,
       1 - (c.embedding <=> cast(:embedding as vector)) as score
from document_chunks c
join documents d on d.id = c.document_id
where c.workspace_id = :workspaceId
  and c.embedding is not null
order by c.embedding <=> cast(:embedding as vector)
limit :limit
```

- [ ] **Step 4: Chat**

`OllamaChatAdapter` chama o modelo `qwen3:8b` com thinking desligado. O prompt pede somente um JSON `{"answer":"...","citedChunkIds":["..."]}` e inclui id, título e corpo de cada trecho recuperado. JSON inválido vira resposta sem citações, não uma frase inventada no lugar da abstenção. Falha de rede lança `EmbeddingUnavailableException`, que o controller da tarefa 5 já traduz em 503 quando a decisão era `ANSWER`.

- [ ] **Step 5: Compilar no Windows**

Run: `.\mvnw.cmd -DskipTests package`  
Expected: BUILD SUCCESS. Não subir a pergunta contra o acervo nesta tarefa. O mínimo `1.01` ainda abstém de propósito.

- [ ] **Step 6: Commit, só se o Helder pedir**

```bash
git commit -m "feat: embed chunks with bge-m3 and search inside the workspace"
```

---

### Task 7: Medir o mínimo no acervo e parar

**Files:**
- Create: `docs/acervo/farol-software/minimo-evidencia.md`

**Interfaces:**
- Consumes: os cinco Markdown, `avaliacao.md` e um workspace real criado pelo cadastro
- Produces: o melhor score de cada uma das dez perguntas. Não altera `1.01` neste arquivo sem um sim separado.

- [ ] **Step 1: Subir a API no Windows apontando para o Postgres 5436 e o Ollama 11434**

Cadastrar um usuário, enviar os cinco Markdown e esperar `COMPLETED` em cada um.

- [ ] **Step 2: Para cada pergunta de `avaliacao.md`, anotar o melhor score da busca antes do portão**

Não chamar o modelo de geração nas quatro perguntas sem resposta no acervo. O arquivo guarda pergunta, melhor score e se o trecho esperado apareceu entre os cinco.

- [ ] **Step 3: Parar**

Não gravar `CHATBOOT_EVIDENCE_MINIMUM` no Git. O Helder escolhe o número depois de ler `minimo-evidencia.md`. Não criar coluna de outra dimensão e não marcar o upload antigo como `COMPLETED` sem o vetor.
