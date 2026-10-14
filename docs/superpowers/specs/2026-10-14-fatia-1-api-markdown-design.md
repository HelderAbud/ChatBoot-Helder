# Spec — Fatia 1: API Markdown da Farol Software

Data: 2026-10-14  
Status: aprovada para o plano de implementação  
Fonte operacional: `AGENTS.md`

## Problema

O ChatBoot Helder precisa, nesta primeira entrega, responder perguntas com citação verificável a partir de documentação da própria equipe. Sem documentos de exemplo, não há como provar resposta, citação e abstenção.

## Scope

Uma API REST, sem tela. Um usuário se cadastra, recebe um workspace e envia Markdown. A API divide o texto em trechos, gera embeddings e responde uma pergunta só com o acervo daquele workspace. Se não houver evidência suficiente, a API diz isso e não chama o modelo de geração.

O acervo de demonstração é da empresa fictícia **Farol Software**, produto **Farol Desk** (central de chamados). São cinco arquivos Markdown, um por categoria:

| Categoria | Arquivo | O que o texto precisa fixar |
|-----------|---------|-----------------------------|
| Manual | `docs/acervo/farol-software/manual-farol-desk.md` | Login fictício em `https://app.farol.example/login`. Caminho do produto: Chamados, depois Novo. Prioridades: Baixa, Normal, Alta e Urgente. Anexo do produto Farol Desk: até 10 MB. |
| Política interna | `docs/acervo/farol-software/politica-interna-acesso.md` | Senha interna com no mínimo 12 caracteres. Analista N1 não acessa gravação de chamada. Ver chamado de outro cliente exige autorização do gestor. |
| Catálogo | `docs/acervo/farol-software/catalogo-planos.md` | Plano Luz: R$ 49 por usuário/mês, 1 fila, canal e-mail. Plano Farol: R$ 99 por usuário/mês, 5 filas, e-mail e chat. Plano Porto: R$ 199 por usuário/mês, filas ilimitadas, e-mail, chat e telefone. O catálogo afirma que não existe plano gratuito. |
| Procedimento | `docs/acervo/farol-software/procedimento-suporte-n1.md` | Confirmar o e-mail do solicitante. Classificar a prioridade. Se a prioridade for Urgente, avisar o plantão em até 15 minutos. Não prometer prazo de correção de bug. |
| Atendimento | `docs/acervo/farol-software/atendimento-roteiro.md` | Saudação: "Farol Software, meu nome é {nome}, como posso ajudar?". Não informar senha de outro usuário. Encerrar perguntando se o chamado ficou claro. |

Esses fatos são o contrato do acervo. O texto pode ter seções e exemplos curtos em volta deles. Não inclui CNPJ, integração com SAP, desconto anual, telefone real, nome de pessoa real nem dado de cliente.

Junto dos cinco arquivos, a fatia inclui dez perguntas de avaliação em `docs/acervo/farol-software/avaliacao.md`:

- quatro respondíveis por um único documento
- duas que exigem trechos de dois documentos
- quatro sem resposta no acervo, entre elas: CNPJ da Farol, integração com SAP e desconto anual

O conjunto de 30 perguntas do planejamento completo fica para uma fase posterior.

## Fora desta fatia

- Tela React
- PDF, OpenAPI de terceiros, OCR
- Versionamento com várias versões ativas e rollback
- Refresh token, recuperação de senha e três papéis
- Segundo workspace, convite e membros
- Worker assíncrono com fila
- API paga de modelo
- Microserviços
- Gravar o par final de modelos antes do teste de tempo
- Commit, push ou PR

## Arquitetura

Monólito modular Spring Boot 3.5.x, Java 21, Spring AI 1.1.x. A patch exata é a última estável dessa linha no dia em que o projeto for gerado.

| Peça | Onde roda |
|------|-----------|
| API na porta 8087 | Windows, com JDK e Maven |
| Arquivos enviados | Pasta `STORAGE_DIR` na máquina da API. Nome no disco: UUID. Nome original só no banco. A pasta fica fora do Git. |
| PostgreSQL 17 + pgvector | Ubuntu (WSL), publicado no host na porta 5436. Dentro do Ubuntu a porta é 5432. Pacote `postgresql-17-pgvector`. |
| Ollama | Máquina local. Primeiro teste: Qwen3 8B com thinking desligado, e BGE-M3. Se a CPU não aguentar: Qwen3 4B e Nomic Embed Text. |

Docker, se for usado para o Postgres da fatia, roda só no Ubuntu. Não há Docker Desktop no Windows. Teste de integração com banco roda no Ubuntu. Teste unitário da regra de negócio roda no Windows.

A dimensão do vetor (`vector(N)`) só entra na migration depois que o modelo de embedding for aprovado. Trocar esse modelo exige reindexar.

Spring AI fica atrás de duas portas do próprio projeto: uma para embedding e outra para geração. O restante do código não chama classe de fornecedor direto.

## Fluxo

1. `POST /api/v1/auth/register` com e-mail, senha de no mínimo 8 caracteres e nome do workspace cria o usuário e o único workspace dele. A resposta `201` traz o id do usuário e o id do workspace. Senha menor que 8 caracteres retorna `400`.
2. `POST /api/v1/auth/login` devolve JWT de acesso, tipo Bearer, válido por 30 minutos. A senha é guardada com BCrypt.
3. `POST /api/v1/documents`, com o token, recebe um `.md` de até 2 MB. A API recusa outro tipo, arquivo vazio, byte nulo e arquivo acima do limite, com mensagem clara.
4. Na mesma requisição, a API extrai o Markdown, corta por título de seção, mantém bloco de código inteiro dentro do trecho da seção, gera embeddings e responde `201` com status `COMPLETED`. Falha de extração ou de embedding grava `FAILED` e a mensagem da falha. Não há fila nesta fatia.
5. `GET /api/v1/documents/{id}` devolve nome original, status e, se houver, a mensagem de falha. Só o workspace do token.
6. `POST /api/v1/questions` com o texto da pergunta busca trechos apenas daquele workspace.
7. Sem trecho, ou com o melhor trecho abaixo do mínimo em configuração, a resposta é exatamente: `Não encontrei informação confiável na documentação disponível deste workspace.` O modelo de geração não é chamado. `abstained` vem `true` e `citations` vem vazio.
8. Com evidência, o modelo responde. Cada citação é conferida no servidor. Citação cujo trecho não estava na busca daquela pergunta é descartada. A resposta persistida guarda as citações aceitas, com nome original do documento e título da seção.

O mínimo de similaridade é configuração. O valor numérico inicial é escolhido no primeiro teste com o acervo, não nesta spec.

Pergunta e resposta ficam gravadas, ligadas ao usuário e ao workspace. Não há lista de conversas nem segundo turno obrigatório nesta fatia.

## Erros

| Situação | Resultado |
|----------|-----------|
| E-mail já cadastrado | 409 |
| E-mail ou senha inválidos no login | 401 |
| Token ausente ou expirado | 401 |
| Documento ou pergunta de outro workspace | 404 |
| Arquivo acima de 2 MB, extensão diferente de `.md`, vazio ou com byte nulo | 400, com o motivo |
| Ollama indisponível no upload | documento `FAILED`, resposta HTTP 503 |
| Ollama indisponível na pergunta, depois de haver evidência | 503, sem resposta inventada |

Senha, token e texto integral de documento não entram em log.

Documento importado é dado não confiável. Instrução escrita dentro do Markdown não altera regra de autenticação, de workspace nem de abstenção.

## Aceite

- Cadastro cria usuário e um workspace; o segundo cadastro com o mesmo e-mail retorna 409.
- Login devolve JWT que expira em 30 minutos; não existe refresh nem recuperação de senha.
- Upload de cada um dos cinco Markdown da Farol termina `COMPLETED` quando o Ollama de embedding responde.
- Upload inválido explica a falha e não grava trecho pesquisável.
- Usuário A não lê documento, trecho nem citação do workspace do usuário B. O teste espera 404.
- Pergunta "Qual o preço mensal por usuário do Plano Farol?" cita `catalogo-planos.md` e o valor R$ 99.
- Pergunta "Em quanto tempo o N1 avisa o plantão numa prioridade Urgente?" cita `procedimento-suporte-n1.md` e o prazo de 15 minutos.
- Pergunta "O Plano Porto inclui telefone e o que o N1 não pode prometer sobre bug?" cita `catalogo-planos.md` e `procedimento-suporte-n1.md`.
- Pergunta sobre CNPJ, SAP ou desconto anual retorna a frase fixa de abstenção, `abstained: true` e nenhuma citação, sem chamada ao modelo de geração.
- Citação com identificador que não estava na busca da pergunta não aparece na resposta.
- Swagger da própria API abre em `http://localhost:8087/swagger-ui.html`.
- Os cinco Markdown e `avaliacao.md` estão no Git. A pasta `STORAGE_DIR` não está.

## Verificação

- Teste unitário da regra de abstenção, do limite de upload e da conferência de citação, no Windows, com Maven.
- Teste de integração com PostgreSQL 17 e pgvector no Ubuntu.
- Leitura humana dos cinco Markdown contra a tabela desta spec.
- O teste de tempo do Ollama e o número do mínimo de evidência ficam registrados num complemento curto desta spec depois da medição. Até lá, o projeto pode ser gerado e o fluxo de auth, upload e validação pode ser testado; a busca vetorial com modelo travado espera essa medição.

## Riscos

- Embedding na mesma requisição pode passar do tempo confortável de HTTP na CPU. Se o primeiro teste mostrar isso, a fatia seguinte introduz fila. Esta fatia não antecipa a fila.
- A dimensão do vetor depende do modelo aprovado. Gerar a migration `vector(N)` antes dessa aprovação prende o banco no modelo errado.
- O pacote `postgresql-17-pgvector` precisa existir no Ubuntu usado. Se não existir, a instalação para e a decisão de versão do Postgres volta para aprovação.
