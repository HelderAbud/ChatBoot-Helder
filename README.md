# ChatBoot Helder

Assistente de documentação para equipes. A pessoa pergunta em linguagem natural e recebe uma resposta apoiada nos documentos do próprio workspace, com citação verificável. Se a documentação não sustentar a resposta, o sistema diz isso.

O acervo de demonstração é da empresa fictícia **Farol Software**, produto **Farol Desk**. Repositório: [HelderAbud/ChatBoot-Helder](https://github.com/HelderAbud/ChatBoot-Helder).

## Estado atual

O que a API faz:

- Cinco documentos Markdown da Farol (manual, política interna, catálogo, procedimento e roteiro de atendimento) e dez perguntas de avaliação.
- API Spring Boot 3.5.16 em Java 21, com rotas protegidas por Bearer.
- Cadastro e login com JWT de 30 minutos. A senha fica com hash BCrypt. Não há refresh nem recuperação de senha.
- Upload de um `.md` de até 2 MB. O arquivo no disco usa um UUID. Os trechos vão para o PostgreSQL e o embedding `bge-m3` grava o vetor. O status fica `COMPLETED` só quando o vetor existe; se o modelo falha, fica `FAILED`.
- `POST /api/v1/questions` busca só no workspace e só em documento `COMPLETED`. Sem evidência, ou sem citação aproveitável, a API se abstém e não inventa resposta. A geração usa `qwen3:8b` local.

Ainda não está pronto:

- O mínimo de evidência continua `1.01`. Com esse número a API se abstém, porque o score de cosseno não passa de 1. A medição está em `docs/acervo/farol-software/minimo-evidencia.md`.
- Tela, PDF e OpenAPI de terceiros.
- Teste de isolamento entre dois workspaces contra um PostgreSQL real.

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
