# ChatBoot Helder

Assistente de documentação para equipes. A pessoa pergunta em linguagem natural e recebe uma resposta apoiada nos documentos do próprio workspace, com citação verificável. Se a documentação não sustentar a resposta, o sistema diz isso.

O acervo de demonstração é da empresa fictícia **Farol Software**, produto **Farol Desk**. Repositório: [HelderAbud/ChatBoot-Helder](https://github.com/HelderAbud/ChatBoot-Helder).

## Estado atual

Já está na `main`:

- Cinco documentos Markdown da Farol (manual, política interna, catálogo, procedimento e roteiro de atendimento) e dez perguntas de avaliação.
- API Spring Boot 3.5.16 em Java 21.
- Validação de upload: só `.md`, até 2 MB, sem byte nulo.
- Corte do Markdown por título, mantendo bloco de código dentro da seção.
- Regra de abstenção e conferência de citação, testadas sem banco e sem modelo.
- Cadastro e login com JWT de 30 minutos. A senha fica com hash BCrypt. Não há refresh nem recuperação de senha.
- Upload que grava o Markdown em disco, com nome UUID, e os trechos de texto no PostgreSQL. O status dessa etapa é `TEXT_STORED`.

Ainda não está pronto:

- Embedding, busca no workspace e a pergunta respondida pelo modelo.
- Proteção Bearer das rotas e o teste de isolamento entre dois workspaces. Isso está na branch local, ainda sem merge.
- Tela, PDF e OpenAPI de terceiros.

O desenho da fatia está em `docs/superpowers/specs/2026-10-14-fatia-1-api-markdown-design.md`. As decisões fechadas estão em `AGENTS.md`.

## Como rodar os testes

No Windows, na raiz do repositório. Se o Maven reclamar de certificado ao baixar dependência, use o repositório de certificados do sistema na mesma janela:

```powershell
$env:MAVEN_OPTS='-Djavax.net.ssl.trustStoreType=Windows-ROOT'
.\mvnw.cmd test
```

Os testes atuais não sobem banco nem modelo. Integração com PostgreSQL fica no Ubuntu, quando existir.

## Ambiente

| Peça | Onde |
|------|------|
| Cursor, JDK 21 e Maven | Windows |
| API | porta **8087** |
| PostgreSQL 17 e pgvector | Ubuntu (WSL), publicado na porta **5436** |
| Docker, se entrar | só dentro do Ubuntu |

Não há Docker Desktop no Windows. Segredos ficam fora do Git: `CHATBOOT_JWT_SECRET` e a pasta `STORAGE_DIR`.
