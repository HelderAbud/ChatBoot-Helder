# Fatia 1a — acervo, regras e API de cadastro e upload

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Publicar o acervo Markdown da Farol Software e uma API que cadastra o usuário, autentica com JWT e grava um `.md` válido em disco e em trechos de texto.

**Architecture:** Monólito Spring Boot. Regras de upload, corte de Markdown, abstenção e citação são classes puras, testadas sem banco e sem Ollama. Cadastro e upload entram em seguida, com o arquivo em `STORAGE_DIR` e os trechos em PostgreSQL sem coluna vetorial. A busca semântica e a resposta do modelo ficam no plano seguinte, depois da medição do Ollama.

**Tech Stack:** Java 21, Spring Boot 3.5.16, Spring Security, Flyway, PostgreSQL 17 no Ubuntu (porta host 5436), springdoc 2.8.9. Spring AI 1.1.4 entra só no plano da busca.

## Global Constraints

- API na porta 8087. Postgres publicado no host na porta 5436. Dentro do Ubuntu, 5432.
- Docker só no Ubuntu. Teste unitário no Windows com `.\mvnw.cmd`. Teste que sobe Postgres roda no Ubuntu.
- JWT de acesso com 1800 segundos. Senha com BCrypt. Senha de cadastro com no mínimo 8 caracteres. Sem refresh e sem recuperação de senha.
- Um `POST /api/v1/auth/register` cria o usuário e o workspace dele. E-mail repetido responde 409.
- Upload: um `.md` de até 2 MB (2_097_152 bytes). Outro tipo, arquivo vazio ou byte nulo responde 400. Nome no disco é UUID. Nome original só no banco.
- `STORAGE_DIR` vem do ambiente. Essa pasta não entra no Git.
- Segredo JWT vem de `CHATBOOT_JWT_SECRET`. Não versionar `.env`, senha ou token.
- Documento importado é dado não confiável.
- Pacote Java: `com.chatboot`.
- A coluna `vector(N)` não entra neste plano. O spec exige essa migration só depois do modelo de embedding aprovado.
- Não executar `git commit`, push ou PR sem pedido explícito do Helder. O comando fica no passo para quando houver esse pedido.
- Não há tela React, PDF, OpenAPI de terceiros, fila ou três papéis.

---

## Fora deste plano

O plano da pergunta com embedding, abstenção em HTTP e citação persistida começa depois que existir `docs/acervo/farol-software/medicao-ollama.md` com o nome do modelo e a dimensão inteira do vetor. Esse arquivo é produzido na tarefa 8. Até lá, a API não expõe `POST /api/v1/questions`.

## Mapa de arquivos

| Arquivo | Responsabilidade |
|---------|------------------|
| `docs/acervo/farol-software/*.md` | Texto-fonte da Farol Software e as dez perguntas |
| `com.chatboot.documents.MarkdownUploadValidator` | Tamanho, extensão, vazio e byte nulo |
| `com.chatboot.documents.MarkdownChunker` | Corte por título, com bloco de código inteiro |
| `com.chatboot.assistant.EvidenceGate` | Decide abster sem chamar o modelo |
| `com.chatboot.assistant.CitationChecker` | Descarta citação que não estava na busca |
| `com.chatboot.identity` | Cadastro, BCrypt, JWT de 1800 segundos |
| `com.chatboot.documents.LocalFileStorage` | Grava bytes em `STORAGE_DIR` com UUID |
| `db/migration/V1__identity_and_documents.sql` | Usuário, workspace, documento e trecho de texto |

### Task 1: Acervo Farol Software

**Files:**
- Create: `docs/acervo/farol-software/manual-farol-desk.md`
- Create: `docs/acervo/farol-software/politica-interna-acesso.md`
- Create: `docs/acervo/farol-software/catalogo-planos.md`
- Create: `docs/acervo/farol-software/procedimento-suporte-n1.md`
- Create: `docs/acervo/farol-software/atendimento-roteiro.md`
- Create: `docs/acervo/farol-software/avaliacao.md`

**Interfaces:**
- Consumes: tabela de fatos da spec
- Produces: caminhos estáveis citados pela avaliação

- [ ] **Step 1: Gravar os seis arquivos com o texto abaixo**

`manual-farol-desk.md`

```markdown
# Manual do Farol Desk

## Acesso

O login do Farol Desk fica em https://app.farol.example/login.

## Abrir chamado

O caminho no produto é Chamados, depois Novo.

## Prioridades

As prioridades são Baixa, Normal, Alta e Urgente.

## Anexo

O anexo do produto Farol Desk aceita até 10 MB.
```

`politica-interna-acesso.md`

```markdown
# Política interna de acesso

## Senha

A senha interna da Farol Software tem no mínimo 12 caracteres.

## Gravação

O analista N1 não acessa gravação de chamada.

## Outro cliente

Ver chamado de outro cliente exige autorização do gestor.
```

`catalogo-planos.md`

```markdown
# Catálogo de planos

## Plano Luz

O Plano Luz custa R$ 49 por usuário/mês, tem 1 fila e o canal e-mail.

## Plano Farol

O Plano Farol custa R$ 99 por usuário/mês, tem 5 filas e os canais e-mail e chat.

## Plano Porto

O Plano Porto custa R$ 199 por usuário/mês, tem filas ilimitadas e os canais e-mail, chat e telefone.

## Plano gratuito

Não existe plano gratuito.
```

`procedimento-suporte-n1.md`

```markdown
# Procedimento de suporte N1

## Identificação

Confirmar o e-mail do solicitante.

## Classificação

Classificar a prioridade.

## Urgente

Se a prioridade for Urgente, avisar o plantão em até 15 minutos.

## Bug

Não prometer prazo de correção de bug.
```

`atendimento-roteiro.md`

```markdown
# Roteiro de atendimento

## Saudação

A saudação é: Farol Software, meu nome é {nome}, como posso ajudar?

## Senha de terceiro

Não informar senha de outro usuário.

## Encerramento

Encerrar perguntando se o chamado ficou claro.
```

`avaliacao.md`

```markdown
# Avaliação da fatia 1

## Respondíveis por um documento

1. Qual o preço mensal por usuário do Plano Farol? Fonte: catalogo-planos.md. Resposta: R$ 99.
2. Em quanto tempo o N1 avisa o plantão numa prioridade Urgente? Fonte: procedimento-suporte-n1.md. Resposta: 15 minutos.
3. Onde fica o login do Farol Desk? Fonte: manual-farol-desk.md. Resposta: https://app.farol.example/login.
4. Qual é o mínimo de caracteres da senha interna? Fonte: politica-interna-acesso.md. Resposta: 12.

## Dois documentos

5. O Plano Porto inclui telefone e o que o N1 não pode prometer sobre bug? Fontes: catalogo-planos.md e procedimento-suporte-n1.md.
6. Qual o caminho para abrir chamado e qual saudação o atendimento usa? Fontes: manual-farol-desk.md e atendimento-roteiro.md.

## Sem resposta no acervo

7. Qual é o CNPJ da Farol Software?
8. A Farol Software integra com SAP?
9. Qual é o desconto anual dos planos?
10. Qual é o endereço do escritório da Farol Software?
```

- [ ] **Step 2: Conferir os fatos da spec no texto**

Confirmar, lendo os arquivos, cada linha da tabela "O que o texto precisa fixar" da spec. Não há CNPJ, SAP, desconto anual nem endereço.

- [ ] **Step 3: Commit, só se o Helder pedir**

```bash
git add docs/acervo/farol-software
git commit -m "docs: add Farol Software markdown corpus for the first slice"
```

### Task 2: Validador de upload

**Files:**
- Create: `pom.xml`
- Create: `src/test/java/com/chatboot/documents/MarkdownUploadValidatorTest.java`
- Create: `src/main/java/com/chatboot/documents/MarkdownUploadValidator.java`
- Create: `src/main/java/com/chatboot/documents/UploadRejectedException.java`

**Interfaces:**
- Consumes: nenhuma
- Produces: `MarkdownUploadValidator.validate(String filename, byte[] content)` lança `UploadRejectedException` com `reason` em `EMPTY`, `TOO_LARGE`, `NOT_MARKDOWN`, `NULL_BYTE`

- [ ] **Step 1: Criar o `pom.xml` mínimo e o teste que falha**

Parent `spring-boot-starter-parent` `3.5.16`, Java 21. Dependência de teste: `spring-boot-starter-test`.

```java
package com.chatboot.documents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MarkdownUploadValidatorTest {

    private final MarkdownUploadValidator validator = new MarkdownUploadValidator();

    @Test
    void acceptsMarkdownWithinTwoMegabytes() {
        validator.validate("manual.md", "# Titulo".getBytes());
    }

    @Test
    void rejectsEmptyFile() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.md", new byte[0]));
        assertEquals(UploadRejectedException.Reason.EMPTY, error.reason());
    }

    @Test
    void rejectsFileLargerThanTwoMegabytes() {
        byte[] content = new byte[2_097_153];
        content[0] = '#';
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.md", content));
        assertEquals(UploadRejectedException.Reason.TOO_LARGE, error.reason());
    }

    @Test
    void rejectsNonMarkdownExtension() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.pdf", "# Titulo".getBytes()));
        assertEquals(UploadRejectedException.Reason.NOT_MARKDOWN, error.reason());
    }

    @Test
    void rejectsNullByte() {
        UploadRejectedException error = assertThrows(UploadRejectedException.class,
                () -> validator.validate("manual.md", new byte[] {'#', 0}));
        assertEquals(UploadRejectedException.Reason.NULL_BYTE, error.reason());
    }
}
```

- [ ] **Step 2: Rodar o teste e ver a falha de compilação**

Run: `.\mvnw.cmd -Dtest=MarkdownUploadValidatorTest test`  
Expected: FAIL porque `MarkdownUploadValidator` ainda não existe.

- [ ] **Step 3: Implementação mínima**

```java
package com.chatboot.documents;

public class UploadRejectedException extends RuntimeException {
    public enum Reason { EMPTY, TOO_LARGE, NOT_MARKDOWN, NULL_BYTE }
    private final Reason reason;
    public UploadRejectedException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }
    public Reason reason() { return reason; }
}
```

```java
package com.chatboot.documents;

public class MarkdownUploadValidator {
    static final int MAX_BYTES = 2_097_152;

    public void validate(String filename, byte[] content) {
        if (content == null || content.length == 0) {
            throw new UploadRejectedException(UploadRejectedException.Reason.EMPTY);
        }
        if (content.length > MAX_BYTES) {
            throw new UploadRejectedException(UploadRejectedException.Reason.TOO_LARGE);
        }
        if (filename == null || !filename.toLowerCase().endsWith(".md")) {
            throw new UploadRejectedException(UploadRejectedException.Reason.NOT_MARKDOWN);
        }
        for (byte value : content) {
            if (value == 0) {
                throw new UploadRejectedException(UploadRejectedException.Reason.NULL_BYTE);
            }
        }
    }
}
```

A ordem das regras é: vazio, tamanho, extensão, byte nulo. Um PDF vazio responde `EMPTY`.

- [ ] **Step 4: Rodar o teste**

Run: `.\mvnw.cmd -Dtest=MarkdownUploadValidatorTest test`  
Expected: PASS

- [ ] **Step 5: Commit, só se o Helder pedir**

```bash
git add pom.xml src/main/java/com/chatboot/documents src/test/java/com/chatboot/documents/MarkdownUploadValidatorTest.java
git commit -m "test: reject markdown uploads that break the first-slice rules"
```

### Task 3: Corte do Markdown por seção

**Files:**
- Create: `src/test/java/com/chatboot/documents/MarkdownChunkerTest.java`
- Create: `src/main/java/com/chatboot/documents/MarkdownChunk.java`
- Create: `src/main/java/com/chatboot/documents/MarkdownChunker.java`

**Interfaces:**
- Consumes: nenhuma
- Produces: `List<MarkdownChunk> MarkdownChunker.chunk(String markdown)` com `heading` e `body`. Um bloco cercado por crases permanece no trecho da seção em que começou.

- [ ] **Step 1: Teste que falha**

```java
@Test
void splitsOnHeadingsAndKeepsCodeFenceIntact() {
    String markdown = """
            # Manual
            intro
            ## Acesso
            login
            ```
            # isto nao e secao
            ```
            fim
            """;
    List<MarkdownChunk> chunks = new MarkdownChunker().chunk(markdown);
    assertEquals(2, chunks.size());
    assertEquals("Manual", chunks.get(0).heading());
    assertEquals("Acesso", chunks.get(1).heading());
    assertTrue(chunks.get(1).body().contains("# isto nao e secao"));
}
```

- [ ] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=MarkdownChunkerTest test`  
Expected: FAIL, classe ausente.

- [ ] **Step 3: Implementação**

```java
package com.chatboot.documents;

public record MarkdownChunk(String heading, String body) {}
```

```java
package com.chatboot.documents;

import java.util.ArrayList;
import java.util.List;

public class MarkdownChunker {
    public List<MarkdownChunk> chunk(String markdown) {
        List<MarkdownChunk> chunks = new ArrayList<>();
        String heading = null;
        StringBuilder body = new StringBuilder();
        boolean insideFence = false;
        for (String line : markdown.split("\\R", -1)) {
            if (line.startsWith("```")) {
                insideFence = !insideFence;
                appendLine(body, line);
                continue;
            }
            if (!insideFence && line.startsWith("#")) {
                flush(chunks, heading, body);
                heading = line.replaceFirst("^#+\\s*", "");
                continue;
            }
            appendLine(body, line);
        }
        flush(chunks, heading, body);
        return chunks;
    }

    private static void appendLine(StringBuilder body, String line) {
        if (!body.isEmpty()) {
            body.append('\n');
        }
        body.append(line);
    }

    private static void flush(List<MarkdownChunk> chunks, String heading, StringBuilder body) {
        if (heading == null) {
            return;
        }
        chunks.add(new MarkdownChunk(heading, body.toString().trim()));
        body.setLength(0);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `.\mvnw.cmd -Dtest=MarkdownChunkerTest test`  
Expected: PASS

- [ ] **Step 5: Commit, só se o Helder pedir**

```bash
git add src/main/java/com/chatboot/documents/MarkdownChunk.java src/main/java/com/chatboot/documents/MarkdownChunker.java src/test/java/com/chatboot/documents/MarkdownChunkerTest.java
git commit -m "test: split markdown on headings without breaking code fences"
```

### Task 4: Abstenção e citação

**Files:**
- Create: `src/main/java/com/chatboot/assistant/RetrievedChunk.java`
- Create: `src/main/java/com/chatboot/assistant/EvidenceGate.java`
- Create: `src/main/java/com/chatboot/assistant/CitationChecker.java`
- Create: `src/test/java/com/chatboot/assistant/EvidenceGateTest.java`
- Create: `src/test/java/com/chatboot/assistant/CitationCheckerTest.java`

**Interfaces:**
- Consumes: nenhuma
- Produces:
  - `record RetrievedChunk(String id, String documentName, String heading, double score)`
  - `EvidenceGate.decide(List<RetrievedChunk> chunks, double minimumScore)` devolve `ABSTAIN` quando a lista é vazia ou quando o maior `score` é menor que `minimumScore`. Score igual ao mínimo devolve `ANSWER`.
  - `CitationChecker.keep(Set<String> retrievedIds, List<String> citedIds)` devolve só ids presentes em `retrievedIds`, na ordem citada, sem duplicar.

A frase HTTP da abstenção não é montada aqui. Quem chamar `ABSTAIN` usa exatamente: `Não encontrei informação confiável na documentação disponível deste workspace.`

- [ ] **Step 1: Testes**

`EvidenceGateTest` cobre lista vazia, score 0.49 com mínimo 0.50 e score 0.50 com mínimo 0.50. O terceiro caso devolve `ANSWER`.

`CitationCheckerTest`: retrieved `{a, b}`, cited `[b, c, b, a]` devolve `[b, a]`.

- [ ] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=EvidenceGateTest,CitationCheckerTest test`  
Expected: FAIL, classes ausentes.

- [ ] **Step 3: Implementação mínima dos dois tipos e das duas classes**

```java
package com.chatboot.assistant;

public record RetrievedChunk(String id, String documentName, String heading, double score) {}
```

```java
package com.chatboot.assistant;

import java.util.List;

public class EvidenceGate {
    public enum Decision { ABSTAIN, ANSWER }

    public Decision decide(List<RetrievedChunk> chunks, double minimumScore) {
        if (chunks == null || chunks.isEmpty()) {
            return Decision.ABSTAIN;
        }
        double best = chunks.stream().mapToDouble(RetrievedChunk::score).max().orElse(0);
        if (best < minimumScore) {
            return Decision.ABSTAIN;
        }
        return Decision.ANSWER;
    }
}
```

```java
package com.chatboot.assistant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CitationChecker {
    public List<String> keep(Set<String> retrievedIds, List<String> citedIds) {
        LinkedHashSet<String> kept = new LinkedHashSet<>();
        for (String cited : citedIds) {
            if (retrievedIds.contains(cited)) {
                kept.add(cited);
            }
        }
        return new ArrayList<>(kept);
    }
}
```

- [ ] **Step 4: Rodar e ver passar**

Run: `.\mvnw.cmd -Dtest=EvidenceGateTest,CitationCheckerTest test`  
Expected: PASS

- [ ] **Step 5: Commit, só se o Helder pedir**

```bash
git add src/main/java/com/chatboot/assistant src/test/java/com/chatboot/assistant
git commit -m "test: abstain without evidence and drop citations outside the search"
```

### Task 5: Cadastro e JWT

**Files:**
- Create: `src/main/java/com/chatboot/ChatbootApplication.java`
- Create: `src/main/java/com/chatboot/identity/PasswordPolicy.java`
- Create: `src/main/java/com/chatboot/identity/JwtService.java`
- Create: `src/main/java/com/chatboot/identity/AuthController.java`
- Create: `src/main/java/com/chatboot/identity/AuthService.java`
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/com/chatboot/identity/PasswordPolicyTest.java`
- Create: `src/test/java/com/chatboot/identity/JwtServiceTest.java`
- Create: `src/test/java/com/chatboot/identity/AuthControllerTest.java`
- Modify: `pom.xml`

**Interfaces:**
- Consumes: nenhuma regra das tarefas 2 a 4
- Produces:
  - `PasswordPolicy.check(String password)` lança exceção se o tamanho for menor que 8
  - `JwtService.issue(UUID userId, UUID workspaceId)` devolve token com `sub` = userId, claim `workspaceId` e expiração de 1800 segundos
  - `POST /api/v1/auth/register` body `{ "email", "password", "workspaceName" }` responde `201` com `{ "userId", "workspaceId" }`
  - `POST /api/v1/auth/login` responde `200` com `{ "accessToken", "expiresIn": 1800, "tokenType": "Bearer" }`
  - e-mail já existente responde `409`
  - senha curta responde `400`

- [ ] **Step 1: Testes de política e de JWT, sem subir a API**

`PasswordPolicyTest` rejeita `"curta"` e aceita `"senha-ok"`.

`JwtServiceTest` usa o segredo `chatboot-test-secret-must-be-32bytes!`. Decodifica o token com o `JwtDecoder` de teste e afirma `expiresAt` menos `issuedAt` igual a 1800 segundos, e a claim `workspaceId`.

- [ ] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=PasswordPolicyTest,JwtServiceTest test`  
Expected: FAIL, classes ausentes.

- [ ] **Step 3: Implementar política, JwtService e o pom com `spring-boot-starter-security` e `spring-boot-starter-oauth2-resource-server`**

`server.port: 8087` em `application.yml`. O segredo é `${CHATBOOT_JWT_SECRET}`.

- [ ] **Step 4: Teste MockMvc do cadastro**

`AuthControllerTest` com `@WebMvcTest` e `AuthService` mockado. Cadastro válido espera 201. Serviço lançando e-mail duplicado espera 409. Senha com 7 caracteres espera 400 sem chamar o serviço.

- [ ] **Step 5: Rodar os testes de identity**

Run: `.\mvnw.cmd -Dtest=PasswordPolicyTest,JwtServiceTest,AuthControllerTest test`  
Expected: PASS

- [ ] **Step 6: Commit, só se o Helder pedir**

```bash
git add pom.xml src/main/java/com/chatboot/ChatbootApplication.java src/main/java/com/chatboot/identity src/main/resources/application.yml src/test/java/com/chatboot/identity
git commit -m "feat: register a user workspace and issue a thirty-minute JWT"
```

### Task 6: Upload grava arquivo e trechos

**Files:**
- Create: `src/main/java/com/chatboot/documents/LocalFileStorage.java`
- Create: `src/main/java/com/chatboot/documents/DocumentController.java`
- Create: `src/main/java/com/chatboot/documents/DocumentService.java`
- Create: `src/main/resources/db/migration/V1__identity_and_documents.sql`
- Create: `src/test/java/com/chatboot/documents/LocalFileStorageTest.java`
- Create: `src/test/java/com/chatboot/documents/DocumentControllerTest.java`
- Modify: `pom.xml` com `spring-boot-starter-data-jpa`, `flyway-core`, `postgresql` e `springdoc-openapi-starter-webmvc-ui` `2.8.9`

**Interfaces:**
- Consumes: `MarkdownUploadValidator`, `MarkdownChunker`, claim `workspaceId` do JWT
- Produces:
  - `LocalFileStorage.store(byte[] content)` grava em `STORAGE_DIR` com nome UUID e extensão `.md` e devolve o UUID
  - `POST /api/v1/documents` multipart `file` responde `201` com `{ "documentId", "status": "TEXT_STORED" }`
  - arquivo inválido responde `400` e não cria arquivo
  - `GET /api/v1/documents/{id}` de outro workspace responde `404`

`TEXT_STORED` significa texto e trechos gravados, ainda sem embedding. Não usar `COMPLETED` aqui: a spec reserva `COMPLETED` para quando o embedding existir.

- [ ] **Step 1: Teste da pasta local**

`LocalFileStorageTest` usa um diretório temporário. Depois de `store`, o arquivo existe, o nome é UUID mais `.md`, e o conteúdo é o byte enviado.

- [ ] **Step 2: Rodar e ver falhar**

Run: `.\mvnw.cmd -Dtest=LocalFileStorageTest test`  
Expected: FAIL, classe ausente.

- [ ] **Step 3: Implementar storage, migration e controller**

`V1__identity_and_documents.sql` cria `users`, `workspaces`, `documents` e `document_chunks`. `document_chunks` tem `id`, `document_id`, `workspace_id`, `chunk_index`, `heading`, `body`. Sem coluna de embedding. `documents.status` aceita `TEXT_STORED` e `FAILED`. `documents.workspace_id` e `document_chunks.workspace_id` são obrigatórios.

`GET` compara o `workspaceId` do token com o do documento. Diferente responde 404.

- [ ] **Step 4: MockMvc do upload inválido**

Extensão `.pdf` responde 400 e o `DocumentService` não é chamado.

- [ ] **Step 5: Rodar os testes de documents**

Run: `.\mvnw.cmd -Dtest=LocalFileStorageTest,DocumentControllerTest,MarkdownUploadValidatorTest,MarkdownChunkerTest test`  
Expected: PASS

- [ ] **Step 6: Commit, só se o Helder pedir**

```bash
git add pom.xml src/main/java/com/chatboot/documents src/main/resources/db/migration/V1__identity_and_documents.sql src/test/java/com/chatboot/documents
git commit -m "feat: store markdown uploads as text chunks without embeddings"
```

### Task 7: Swagger e isolamento no teste de controller

**Files:**
- Modify: `src/main/java/com/chatboot/identity/SecurityConfig.java`
- Modify: `src/test/java/com/chatboot/documents/DocumentControllerTest.java`

**Interfaces:**
- Consumes: rotas da tarefa 5 e da tarefa 6
- Produces: `/v3/api-docs` autenticado ou liberado só para a documentação. Cadastro e login permanecem públicos. Demais rotas exigem Bearer.

- [ ] **Step 1: Teste de isolamento**

Dois workspaces. `GET /api/v1/documents/{id}` com token do workspace B e documento do workspace A espera 404.

- [ ] **Step 2: Rodar e ver falhar se o controller ainda não filtrar**

Run: `.\mvnw.cmd -Dtest=DocumentControllerTest test`  
Expected: FAIL até o filtro de workspace existir; PASS se a tarefa 6 já cobrir o caso. Se passar de primeira, registrar isso no resultado da tarefa e seguir.

- [ ] **Step 3: Liberar `/v3/api-docs/**`, `/swagger-ui/**`, `/api/v1/auth/**`. Exigir autenticação no resto.**

- [ ] **Step 4: Subir a API no Windows e abrir o Swagger**

Variáveis: `CHATBOOT_JWT_SECRET` com pelo menos 32 caracteres e `STORAGE_DIR` apontando para uma pasta fora do Git. Postgres ainda não é exigido neste passo se o teste for só o contexto web. Se a aplicação exigir datasource para subir, usar o Postgres 5436 do Ubuntu e parar se ele ainda não existir.

Abrir `http://localhost:8087/swagger-ui.html`.

- [ ] **Step 5: Commit, só se o Helder pedir**

```bash
git add src/main/java/com/chatboot/identity/SecurityConfig.java src/test/java/com/chatboot/documents/DocumentControllerTest.java
git commit -m "test: hide documents from another workspace and expose swagger"
```

### Task 8: Medição do Ollama, sem implementar a busca

**Files:**
- Create: `docs/acervo/farol-software/medicao-ollama.md`

**Interfaces:**
- Consumes: Ollama local
- Produces: o arquivo de medição com modelo de geração, modelo de embedding e dimensão inteira. Esse arquivo é a entrada do próximo plano.

- [ ] **Step 1: Instalar no Ollama, na máquina local, `qwen3:8b` e `bge-m3`**

- [ ] **Step 2: Pedir uma resposta curta ao Qwen3 com thinking desligado e anotar segundos até a resposta**

Comando de referência, ajustando o nome exato que o `ollama list` mostrar:

```bash
ollama run qwen3:8b "Responda só: ok"
```

- [ ] **Step 3: Gerar um embedding com BGE-M3 e anotar o tamanho do vetor**

- [ ] **Step 4: Se a CPU não completar o passo 2 em tempo utilizável, repetir com `qwen3:4b` e, se o embedding falhar, com `nomic-embed-text`**

- [ ] **Step 5: Gravar `medicao-ollama.md` com os números medidos e parar**

Não criar migration `vector`, não implementar `POST /api/v1/questions` e não trocar `TEXT_STORED` por `COMPLETED` nesta tarefa. O plano seguinte só começa depois da leitura desse arquivo e de um sim do Helder.

## Verificação deste plano

- `.\mvnw.cmd test` no Windows passa nas tarefas 2 a 7.
- Os seis Markdown existem e batem com a spec.
- Swagger abre em `http://localhost:8087/swagger-ui.html`.
- Upload de `.pdf` ou de arquivo com byte nulo não cria arquivo em `STORAGE_DIR`.
- A busca vetorial, a frase de abstenção em HTTP e as dez perguntas respondidas pelo modelo ficam para o plano seguinte.
