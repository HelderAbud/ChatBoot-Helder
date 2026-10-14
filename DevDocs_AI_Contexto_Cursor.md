# DevDocs AI --- Contexto Geral do Projeto

**Documento de referência para planejamento, arquitetura e
desenvolvimento com Cursor**\
**Status:** planejamento --- nenhuma implementação de código autorizada
ainda\
**Idioma do projeto/documentação:** português (Brasil), podendo adotar
nomes técnicos em inglês no código\
**Ambiente inicial:** Windows, Cursor, sem Docker instalado

------------------------------------------------------------------------

## 1. Visão geral

**DevDocs AI** é uma plataforma de assistente de conhecimento técnico
para equipes de tecnologia. A ideia é permitir que uma pessoa faça
perguntas em linguagem natural sobre documentação interna e receba
respostas fundamentadas em trechos recuperados dos documentos, com
referências verificáveis.

O projeto tem dois objetivos simultâneos:

1.  **Portfólio profissional:** demonstrar competências de Java, Spring
    Boot, APIs REST, banco de dados, segurança, testes, integração com
    IA e arquitetura de software para buscar uma vaga júnior ou estágio.
2.  **Potencial comercial:** criar uma base de produto que, futuramente,
    possa ser evoluída para atender empresas e equipes de tecnologia.

O sistema deve priorizar confiabilidade, rastreabilidade, segurança e
simplicidade de manutenção. Não deve ser apenas uma interface de chat
conectada a um modelo de IA.

## 2. Problema que o produto resolve

Documentação técnica costuma estar distribuída em arquivos e formatos
diferentes. Encontrar a resposta correta pode exigir leitura manual,
conhecimento prévio do sistema ou ajuda de outra pessoa.

O DevDocs AI pretende centralizar a consulta desses materiais e
responder perguntas como:

-   "Como autenticar uma chamada nessa API?"
-   "Quais parâmetros esse endpoint aceita?"
-   "Qual procedimento está documentado para esse erro?"
-   "Em qual documento está descrita essa configuração?"
-   "A documentação disponível responde a esta pergunta?"

A resposta precisa apontar de onde veio a informação. Quando não houver
evidência suficiente, o sistema deverá admitir isso em vez de inventar
uma resposta.

## 3. Escopo inicial do MVP

### 3.1 Formatos aceitos desde a primeira versão

-   PDF com texto selecionável.
-   Markdown (`.md`).
-   OpenAPI/Swagger em JSON.
-   OpenAPI/Swagger em YAML.

A ingestão de OpenAPI deve extrair e organizar operações documentadas,
incluindo método HTTP, caminho, parâmetros, descrição, respostas e
demais informações relevantes presentes na especificação.

**Importante:** importar uma especificação OpenAPI significa indexar e
consultar a documentação. O MVP não deverá executar chamadas contra os
endpoints de terceiros.

### 3.2 Funcionalidades previstas

-   Cadastro e autenticação de usuários.
-   Espaços de trabalho (workspaces) para separar organizações ou
    equipes.
-   Associação de usuários a workspaces e papéis de acesso.
-   Upload e gerenciamento de documentos.
-   Validação do formato e apresentação de erros claros.
-   Extração e normalização do conteúdo.
-   Divisão do conteúdo em trechos (chunks).
-   Geração e armazenamento de embeddings.
-   Busca semântica com filtro obrigatório pelo workspace autorizado.
-   Respostas em linguagem natural apoiadas nos trechos recuperados.
-   Citações/referências verificáveis para as fontes usadas na resposta.
-   Mensagem explícita quando não houver evidência confiável suficiente.
-   Conversas e mensagens persistidas, com histórico consultável pelo
    usuário autorizado.
-   Estados de processamento: `PENDING`, `PROCESSING`, `COMPLETED` e
    `FAILED`.
-   Atualização/versionamento de documentos.
-   Exclusão de documentos com remoção ou invalidação dos trechos e
    embeddings associados.
-   Tratamento de indisponibilidade do modelo ou de outros serviços.
-   Testes automatizados, documentação da API e pipeline de integração
    contínua.

### 3.3 Fora do MVP

Não incluir inicialmente:

-   Arquitetura de microserviços.
-   Agentes de IA que executem ações em sistemas externos.
-   Treinamento de um modelo próprio.
-   Integrações com GitHub, GitLab, Jira ou outras ferramentas
    corporativas.
-   Pagamentos, assinaturas ou faturamento.
-   OCR para PDFs digitalizados/escaneados.
-   Execução de endpoints de terceiros a partir de arquivos OpenAPI.
-   Funcionalidades comerciais complexas antes de validar o núcleo do
    produto.

Esses itens poderão ser reavaliados depois que o fluxo central estiver
confiável.

## 4. Regras de qualidade e comportamento da IA

1.  **Respostas fundamentadas:** o modelo deve receber os trechos
    recuperados e ser instruído a não apresentar suposições como fatos.
2.  **Abstenção:** quando as fontes não forem suficientes, responder que
    não foi encontrada informação confiável na documentação disponível.
3.  **Citações rastreáveis:** cada referência deve apontar para o
    documento e, quando possível, página do PDF, título/seção do
    Markdown ou método e caminho do endpoint OpenAPI.
4.  **Separação entre conteúdo e instruções:** arquivos importados são
    dados não confiáveis. Instruções encontradas nos documentos não
    podem substituir as regras de segurança e funcionamento do sistema.
5.  **Isolamento entre equipes:** uma pessoa nunca pode consultar
    documentos, trechos, conversas ou fontes de outro workspace sem
    autorização explícita.
6.  **Consistência de versões:** a indexação de uma nova versão não deve
    misturar silenciosamente trechos antigos e novos.
7.  **Exclusão coerente:** ao excluir ou desativar um documento, seus
    trechos não devem continuar aparecendo em futuras buscas.
8.  **Segredos protegidos:** chaves de API, senhas e outros segredos não
    podem ser versionados no Git.
9.  **Falhas compreensíveis:** erros de formato, processamento, banco ou
    provedor de IA devem ser tratados com mensagens úteis e registros
    apropriados.
10. **Observabilidade:** registrar informações operacionais relevantes,
    como latência, falhas e duração do processamento, evitando expor
    conteúdo sensível desnecessariamente.

## 5. Critérios iniciais de aceite

O MVP poderá ser considerado funcional quando:

-   [ ] Importar PDFs textuais, Markdown, OpenAPI JSON e OpenAPI YAML.
-   [ ] Preservar títulos e blocos de código relevantes em Markdown.
-   [ ] Extrair operações OpenAPI com método HTTP, caminho, parâmetros,
    descrições e respostas quando disponíveis.
-   [ ] Validar arquivos e explicar falhas de importação.
-   [ ] Mostrar estados de processamento do documento.
-   [ ] Responder perguntas usando fontes recuperadas.
-   [ ] Exibir referências que possam ser verificadas por uma pessoa.
-   [ ] Admitir quando a documentação não contém evidência suficiente.
-   [ ] Permitir consultar conversas anteriores dentro dos limites de
    autorização.
-   [ ] Atualizar documentos sem servir conteúdo antigo de forma
    silenciosa.
-   [ ] Impedir acesso cruzado entre workspaces em documentos, chunks,
    conversas e citações.
-   [ ] Tratar documentos importados como conteúdo não confiável.
-   [ ] Ter testes unitários e de integração para fluxos críticos.
-   [ ] Disponibilizar documentação OpenAPI/Swagger da própria API.
-   [ ] Executar testes automaticamente no pipeline de CI.

### Avaliação de qualidade

Criar um conjunto inicial de pelo menos **30 perguntas de avaliação**,
incluindo:

-   Perguntas respondíveis diretamente pela documentação.
-   Perguntas sem resposta na documentação.
-   Perguntas sobre endpoints parecidos, para avaliar confusões.
-   Perguntas que exigem recuperar mais de um trecho.

Como meta inicial de experimento, buscar recuperar a fonte esperada em
pelo menos **85%** das perguntas da avaliação. Esse número mede
recuperação de fontes, não garante que a resposta gerada esteja correta.
Registrar também erros de resposta, qualidade das citações e latência.

## 6. Arquitetura proposta

### 6.1 Estilo arquitetural

Começar com um **monólito modular em Spring Boot**. É mais simples de
desenvolver, testar, executar e apresentar em portfólio do que
microserviços. Os limites entre módulos devem ser claros para permitir
evolução posterior.

### 6.2 Tecnologias candidatas

  -----------------------------------------------------------------------
  Área                                Escolha inicial
  ----------------------------------- -----------------------------------
  Linguagem                           Java 21 ou uma versão LTS
                                      compatível com as versões
                                      selecionadas

  Backend                             Spring Boot

  Integração de IA                    Spring AI

  API                                 REST com documentação OpenAPI

  Persistência relacional             PostgreSQL

  Busca vetorial                      pgvector

  Acesso a dados                      Spring Data JPA

  Migrações de banco                  Flyway

  Segurança                           Spring Security

  Testes                              JUnit 5, Mockito e Testcontainers
                                      quando aplicável

  IA local                            Ollama

  Controle de versão                  Git e GitHub

  Frontend futuro                     React + TypeScript, se necessário

  CI                                  GitHub Actions

  Containerização                     Docker opcional em etapa posterior
  -----------------------------------------------------------------------

As versões exatas deverão ser escolhidas e validadas juntas, conferindo
a compatibilidade entre Java, Spring Boot, Spring AI, PostgreSQL e
pgvector antes da implementação.

### 6.3 Provedores de IA

A prioridade de desenvolvimento é **não gerar custos com APIs pagas**. A
primeira experiência deverá usar modelos locais pelo Ollama.

Modelos candidatos a testar:

-   **Geração de texto:** Qwen3 8B; considerar Qwen3 4B se o desempenho
    for insuficiente.
-   **Embeddings:** BGE-M3; alternativa a avaliar: Nomic Embed Text.

Essas opções ainda não estão finalizadas. O resultado deve ser validado
com documentos reais e medições de desempenho.

O computador informado tem Windows, pelo menos 64 GB de RAM e GPU
integrada, sem placa gráfica dedicada. Isso permite experimentar modelos
locais, mas a velocidade real dependerá de CPU, quantização, tamanho do
contexto e modelo escolhido.

OpenAI API pode ser considerada como provedor futuro, mas seu uso é
cobrado separadamente da assinatura do ChatGPT. A arquitetura deve
permitir trocar o provedor sem espalhar dependências específicas por
toda a lógica de negócio.

**Atenção aos embeddings:** o modelo usado para indexar documentos deve
corresponder ao modelo usado para incorporar perguntas. Trocar o modelo
de embeddings normalmente exige reindexar os documentos. Abstrações
ajudam a desacoplar provedores, mas não garantem comportamento idêntico
entre modelos.

### 6.4 Módulos sugeridos

1.  **Identity:** usuários, autenticação e credenciais.
2.  **Workspace:** organizações, membros e papéis.
3.  **Knowledge Base:** documentos, versões, metadados e exclusão.
4.  **Ingestion:** validação, extração de PDF/Markdown/OpenAPI e
    processamento.
5.  **Retrieval:** chunks, embeddings, busca e seleção de fontes.
6.  **Assistant:** perguntas, respostas, citações e conversas.
7.  **Observability:** logs, latência, falhas e métricas operacionais.

Os nomes são propostas iniciais; a organização final dos pacotes será
decidida no desenho técnico.

## 7. Fluxo principal de pergunta e resposta (RAG)

1.  O usuário autenticado envia uma pergunta.
2.  O sistema valida sua identidade e autorização no workspace.
3.  A pergunta é transformada em embedding usando o modelo configurado.
4.  O sistema procura trechos relevantes no pgvector, sempre filtrando
    pelo workspace autorizado.
5.  Avalia se os resultados oferecem evidência suficiente.
6.  Se não houver evidência confiável, informa que a documentação
    disponível não permite responder com segurança.
7.  Se houver evidência, envia a pergunta e os trechos selecionados ao
    modelo de geração, com instruções para responder somente com suporte
    nas fontes.
8.  A resposta é persistida.
9.  As referências usadas são persistidas junto à resposta.
10. A aplicação devolve a resposta e suas fontes ao cliente.

O limiar de relevância e a estratégia de recuperação serão definidos por
testes, não por um valor arbitrário considerado definitivo desde o
início.

## 8. Modelo de dados conceitual

A modelagem abaixo é preliminar. Restrições, cardinalidades, índices e
nomes definitivos devem ser definidos durante o desenho do banco.

### Entidades candidatas

-   **Workspace:** `id`, `name`, `createdAt`, `status`.
-   **User:** `id`, `email`, `passwordHash`, `createdAt`, `status`.
-   **WorkspaceMember:** `workspaceId`, `userId`, `role`.
-   **KnowledgeDocument:** `id`, `workspaceId`, `name`, `format`,
    `status`, `createdAt`.
-   **DocumentVersion:** `id`, `documentId`, `versionNumber`,
    `contentHash`, `status`, `createdAt`.
-   **DocumentChunk:** `id`, `versionId`, `workspaceId`, `chunkIndex`,
    `content`, `embedding`, `metadata`.
-   **Conversation:** identificador, workspace, usuário proprietário e
    datas relevantes.
-   **Message:** identificador, conversa, papel/remetente, conteúdo e
    data.
-   **AnswerSource** (ou estrutura equivalente): relação entre uma
    mensagem de resposta e os chunks usados, com metadados de
    localização da fonte.

É importante definir o ciclo de vida de documentos e versões para evitar
que chunks de uma versão antiga apareçam junto com os da versão ativa.

### Estratégia para atualização de documentos

Preferir um fluxo seguro: processar e validar a nova versão antes de
torná-la ativa, mantendo a possibilidade de rollback quando viável. A
troca de versão ativa deve ser consistente, sem uma janela em que
conteúdo antigo e novo se misturem indevidamente.

### Estratégia de exclusão

A exclusão deve invalidar o documento para futuras buscas e limpar ou
tornar inacessíveis seus chunks e embeddings de acordo com uma política
de retenção definida. O comportamento de histórico e auditoria precisa
ser explicitado na especificação.

## 9. Segurança e isolamento de dados

-   Usar Spring Security para autenticação e autorização.
-   Papéis iniciais candidatos: `OWNER`, `ADMIN` e `MEMBER`.
-   Verificar autorização no servidor em todas as operações relevantes.
-   Aplicar filtro de workspace em buscas vetoriais e consultas
    relacionais.
-   Proteger também o acesso às fontes citadas e às conversas
    históricas.
-   Considerar Row-Level Security do PostgreSQL como camada adicional de
    defesa, depois de avaliar a complexidade operacional.
-   Não confiar em IDs fornecidos pelo cliente como prova de
    autorização.
-   Tratar arquivos e texto importados como entrada não confiável,
    inclusive contra prompt injection.
-   Não executar conteúdo de arquivos ou chamadas a endpoints descritos
    no OpenAPI.
-   Guardar credenciais e chaves fora do repositório, por variáveis de
    ambiente ou mecanismo local apropriado.
-   Evitar registrar senhas, tokens, segredos ou conteúdo sensível
    completo nos logs.
-   Testar explicitamente tentativas de acesso cruzado entre workspaces.

## 10. Ambiente de desenvolvimento no Windows, sem Docker

O desenvolvimento inicial **não depende de Docker**. Os serviços podem
ser instalados e executados localmente.

Componentes a preparar, na ordem adequada ao progresso:

1.  Cursor.
2.  JDK compatível com a versão escolhida de Spring Boot.
3.  Git.
4.  PostgreSQL.
5.  Extensão pgvector compatível com a instalação do PostgreSQL no
    Windows.
6.  Ollama e os modelos selecionados após avaliar compatibilidade e
    desempenho.
7.  Ferramentas de teste e utilitários necessários ao projeto.

A instalação do pgvector nativo no Windows precisa ser verificada para a
versão escolhida do PostgreSQL. Se isso se mostrar difícil, comparar
alternativas locais sem Docker antes de decidir por outra configuração.
Não instalar ou configurar tudo às cegas: confirmar versões e
compatibilidade primeiro.

O Docker pode ser adicionado futuramente para padronizar o ambiente,
facilitar demonstrações e simplificar a execução por outras pessoas, mas
não é um pré-requisito para iniciar o planejamento.

## 11. Estratégia de testes

-   **Unitários:** regras de negócio, validações, permissões e
    transições de estado.
-   **Integração:** persistência, migrações, importação, recuperação e
    APIs.
-   **Segurança:** tentativas de acessar recursos de outro workspace.
-   **Ingestão:** PDFs inválidos, Markdown com código, OpenAPI JSON/YAML
    inválidos e documentos válidos.
-   **RAG:** perguntas respondíveis, não respondíveis e confusões entre
    endpoints semelhantes.
-   **Versionamento:** confirmar que documentos atualizados não misturam
    chunks antigos e novos.
-   **Falhas:** indisponibilidade do Ollama, erros no processamento e
    falhas no banco.
-   **Regressão:** manter o conjunto de avaliação de perguntas e
    executá-lo após alterações relevantes.

Testcontainers pode ser útil quando a configuração de testes estiver
pronta; sua adoção não obriga a usar Docker no ambiente diário desde o
primeiro dia. A forma exata de executar testes de integração deverá ser
decidida conforme as ferramentas disponíveis.

## 12. Armazenamento de arquivos --- decisão pendente

A estratégia de armazenamento ainda precisa ser escolhida. Alternativas
discutidas:

1.  **Diretório local protegido:** mais simples para desenvolvimento
    inicial, desde que o caminho e as permissões sejam controlados e os
    arquivos não fiquem expostos publicamente.
2.  **Armazenamento compatível com S3:** aproxima a arquitetura de um
    produto SaaS e permite trocar o serviço por configuração, mas
    adiciona complexidade. Pode ser adotado localmente mais tarde se
    houver uma implementação compatível adequada.
3.  **Arquivos dentro do PostgreSQL:** não é a opção preferida para o
    MVP; aumenta o peso e a complexidade do banco para esse caso.

A escolha final deve considerar a simplicidade do MVP, a possibilidade
de evoluir para SaaS e o custo operacional.

## 13. Plano de trabalho proposto

**Fase 1 --- Planejamento** - Consolidar requisitos e critérios de
aceite. - Desenhar os módulos e os limites de responsabilidade. -
Definir contratos REST, autenticação e autorização. - Escolher
estratégia de armazenamento de arquivos. - Definir o modelo de dados e
os estados de documentos. - Selecionar versões compatíveis das
tecnologias.

**Fase 2 --- Preparação do ambiente** - Verificar JDK, Cursor e Git. -
Instalar e validar PostgreSQL. - Confirmar a viabilidade do pgvector no
ambiente Windows. - Instalar Ollama e comparar modelos localmente. -
Criar o repositório e as instruções do projeto.

**Fase 3 --- Base da aplicação** - Estrutura do Spring Boot e
configuração por ambiente. - Migrações de banco. - Autenticação,
workspaces e autorização. - Testes automatizados dos limites de
segurança.

**Fase 4 --- Conhecimento e ingestão** - Upload e validação de
arquivos. - Extração dos formatos suportados. - Versionamento e estados
de processamento. - Chunking, embeddings e indexação. - Atualização e
exclusão consistentes.

**Fase 5 --- Assistente RAG** - Busca semântica filtrada por
workspace. - Seleção e avaliação de evidências. - Geração de resposta
com citações. - Abstenção quando não houver evidência suficiente. -
Persistência de conversas e referências.

**Fase 6 --- Qualidade e apresentação** - Conjunto de pelo menos 30
perguntas de avaliação. - Testes unitários, de integração e segurança. -
Documentação OpenAPI da própria API. - CI com execução de testes. -
README com arquitetura, instruções, decisões e limitações. - Interface
React + TypeScript se for necessária para uma demonstração mais
completa. - Docker opcional para facilitar a execução por terceiros.

As fases são uma sequência proposta, não uma autorização para começar a
programar automaticamente.

## 14. Como usar o Cursor neste projeto

O Cursor deverá ser usado de forma incremental e controlada:

-   Manter este documento como fonte de contexto do projeto.
-   Criar instruções específicas para o repositório depois de fechar as
    decisões principais.
-   Pedir mudanças pequenas e delimitadas, em vez de gerar a aplicação
    inteira de uma vez.
-   Solicitar explicação da solução, dos trade-offs e dos arquivos
    alterados.
-   Exigir testes para regras de negócio e autorização.
-   Revisar as alterações antes de aceitá-las.
-   Não permitir que o assistente invente requisitos ou altere
    silenciosamente decisões aprovadas.
-   Nunca colocar chaves reais, senhas ou tokens no prompt ou em
    arquivos versionados.
-   Executar os testes após cada etapa e registrar decisões
    arquiteturais importantes.

### Diretriz para futuras instruções ao Cursor

> Trabalhe somente no escopo solicitado. Antes de alterar código,
> examine a estrutura existente e explique o plano. Preserve as decisões
> de arquitetura e os critérios de aceite deste documento. Não introduza
> microserviços, serviços pagos, Docker obrigatório ou integrações
> externas sem autorização explícita. Implemente mudanças pequenas,
> inclua testes apropriados, não exponha segredos e informe os arquivos
> alterados, os comandos de validação executados e qualquer limitação
> encontrada. Se houver ambiguidade relevante, pergunte antes de
> decidir.

## 15. Decisões ainda pendentes

-   [ ] Estratégia de armazenamento de arquivos.
-   [ ] Prioridade entre simplicidade do MVP, equilíbrio MVP/SaaS ou
    estrutura mais próxima de produção.
-   [ ] Versões exatas e compatíveis de Java, Spring Boot, Spring AI,
    PostgreSQL e pgvector.
-   [ ] Resultado dos testes locais dos modelos de geração e embeddings.
-   [ ] Contratos REST completos, com exemplos de requisição/resposta e
    códigos HTTP.
-   [ ] Estratégia detalhada de autenticação (por exemplo, sessão ou
    token) e recuperação de conta.
-   [ ] Regras de retenção de conversas, versões e documentos excluídos.
-   [ ] Necessidade e escopo da interface React + TypeScript para o MVP.
-   [ ] Política de limites de arquivo, tamanho, tipos MIME e tempo
    máximo de processamento.
-   [ ] Critério quantitativo para considerar uma evidência suficiente
    para responder.

## 16. Princípios que não devem ser perdidos

1.  Primeiro planejar; só implementar quando houver autorização
    explícita.
2.  Manter o projeto simples, mas com boas práticas que possam ser
    demonstradas em entrevista.
3.  Priorizar Java, Spring Boot, REST, PostgreSQL, segurança, testes e
    fundamentos de RAG.
4.  Evitar custos de API durante o desenvolvimento inicial.
5.  Tratar isolamento de dados e rastreabilidade como requisitos
    centrais, não como melhorias opcionais.
6.  Nunca prometer que uma resposta está correta apenas porque foi
    produzida por um modelo.
7.  Usar métricas e testes para avaliar recuperação e qualidade.
8.  Construir por etapas, mantendo o sistema executável e verificável.

------------------------------------------------------------------------

**Resumo:** DevDocs AI será um assistente de documentação técnica
baseado em RAG, construído como monólito modular em Java/Spring Boot,
com PostgreSQL/pgvector e modelos locais via Ollama no início. O
ambiente de trabalho será Windows + Cursor, sem dependência inicial de
Docker. O foco é combinar um portfólio sólido com uma base de produto
comercial, sem começar a implementação antes de fechar as decisões de
planejamento.


---

## 17. Estado confirmado do ambiente de desenvolvimento (14/10/2026)

Esta seção registra apenas o que foi confirmado durante a preparação inicial. Não presume que a conexão com o banco já tenha sido validada.

| Componente | Estado confirmado |
|---|---|
| Sistema operacional | Windows 10, 64 bits, conforme saída do Maven |
| Editor | Cursor instalado |
| Java/JDK | Java 21.0.10; `javac 21.0.10` |
| Maven | Apache Maven 3.9.12 |
| Git | `git version 2.56.0.windows.2` |
| PostgreSQL | Serviço Windows `postgresql-x64-18` em execução |
| Cliente PostgreSQL | `psql (PostgreSQL) 18.6`, executado pelo caminho completo |
| Caminho do cliente PostgreSQL | `C:\Program Files\PostgreSQL\18\bin\psql.exe` |
| `psql` no PATH | Ainda não reconhecido como comando simples; não alterar o PATH sem necessidade |
| Pasta do projeto | `C:\Users\Pessoal\Desktop\DevDocsAI` existe |
| Conteúdo da pasta do projeto | Ainda não inspecionado nesta etapa |
| Estado do Git dentro da pasta do projeto | Ainda não verificado |
| Conexão autenticada ao PostgreSQL | Ainda não confirmada pelo usuário |
| pgvector | Compatibilidade/instalação ainda não verificada |
| Ollama e modelos locais | Ainda não verificados |

### Próximas verificações operacionais

1. Inspecionar a pasta `DevDocsAI` sem modificar ou apagar arquivos.
2. Confirmar se ela já é um repositório Git e verificar `git status`.
3. Confirmar a conexão ao PostgreSQL com o usuário local apropriado, sem registrar ou compartilhar senhas.
4. Verificar a compatibilidade e a disponibilidade do pgvector para a instalação PostgreSQL 18 no Windows.
5. Antes de criar o projeto Spring Boot, fechar e registrar as decisões de versões compatíveis e o escopo inicial.

Não interpretar um item “ainda não confirmado” como falha; significa apenas que falta executar a verificação correspondente.

## 18. Instrução inicial para colar no Cursor

Abra a pasta existente `DevDocsAI` no Cursor e disponibilize este documento no contexto do projeto. Em seguida, cole a instrução abaixo no chat do Cursor.

> Você está trabalhando no projeto DevDocs AI. Leia integralmente o arquivo `DevDocs_AI_Contexto_Cursor.md` (ou o nome do arquivo de contexto que estiver aberto) antes de propor qualquer solução.
>
> **Nesta primeira tarefa, NÃO implemente funcionalidades, NÃO crie arquivos, NÃO instale dependências e NÃO altere código ou configuração.**
>
> 1. Inspecione somente a estrutura que já existe no repositório e identifique os arquivos relevantes.
> 2. Informe se a pasta é um repositório Git e resuma o estado atual sem executar operações destrutivas.
> 3. Compare o estado real encontrado com os objetivos, a arquitetura proposta, os requisitos de segurança e as decisões pendentes deste documento.
> 4. Liste fatos confirmados, dúvidas e riscos, separando claramente cada categoria. Não invente arquivos, configurações, versões ou funcionalidades.
> 5. Recomende apenas o próximo passo pequeno e verificável, explicando por que ele vem primeiro.
> 6. Aguarde autorização explícita antes de fazer qualquer alteração.
>
> Respeite as decisões do projeto: monólito modular Spring Boot; Java 21 como ambiente atualmente instalado, sujeito à validação de compatibilidade com as versões escolhidas; PostgreSQL; pgvector a validar; preferência por modelos locais via Ollama; sem API paga, microserviços ou Docker obrigatório sem autorização. Documentos importados serão conteúdo não confiável. O isolamento entre workspaces, as citações verificáveis, a abstenção quando faltarem evidências e os testes são requisitos centrais.
>
> Trabalhe de forma incremental. Em cada etapa futura, explique o objetivo, os arquivos que pretende alterar, os comandos de validação e os critérios de aceite. Não exponha segredos e nunca solicite que eu cole senhas, tokens ou chaves no chat.

## 19. Regra de condução passo a passo

O desenvolvimento será conduzido em etapas curtas. Ao concluir uma etapa:

- explicar o que foi feito e por quê;
- mostrar os arquivos criados ou alterados;
- informar os comandos executados e seus resultados;
- apontar limitações e verificações ainda pendentes;
- executar ou orientar os testes adequados;
- parar e aguardar a confirmação antes de avançar para a próxima etapa.

Não considerar uma etapa concluída apenas porque o código foi gerado. A conclusão exige validação compatível com o objetivo da etapa.
