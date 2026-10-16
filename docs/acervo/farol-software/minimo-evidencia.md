# Mínimo de evidência — 16/10/2026

Medição do melhor score da busca, antes do portão, no workspace `42f13dce-c088-408e-85e0-e8c6d1609958`. Embedding `bge-m3`. A busca é a mesma SQL da API: no máximo 5 trechos, só deste workspace, score = `1 - distância`. O modelo de geração não foi chamado.

O número do mínimo não foi escolhido. `CHATBOOT_EVIDENCE_MINIMUM` continua `1.01`.

## O que aconteceu no upload

Os cinco Markdown voltaram `COMPLETED`. O vetor não ficou gravado: a atualização do embedding rodou antes do Hibernate inserir o trecho, então o `update` não achou a linha. O `DocumentService` passou a usar `saveAndFlush` antes de gravar o vetor. Os 23 trechos desta medição foram preenchidos depois, com `bge-m3` e o mesmo `update` da API. Um segundo upload não foi feito, para não duplicar o acervo.

## Respondíveis por um documento

| # | Pergunta | Melhor score | Primeiro trecho | Fonte esperada nos 5 |
|---|----------|--------------|-----------------|----------------------|
| 1 | Qual o preço mensal por usuário do Plano Farol? | 0,771 | catalogo-planos.md / Plano Farol | sim |
| 2 | Em quanto tempo o N1 avisa o plantão numa prioridade Urgente? | 0,694 | procedimento-suporte-n1.md / Urgente | sim |
| 3 | Onde fica o login do Farol Desk? | 0,781 | manual-farol-desk.md / Acesso | sim |
| 4 | Qual é o mínimo de caracteres da senha interna? | 0,686 | politica-interna-acesso.md / Senha | sim |

## Dois documentos

| # | Pergunta | Melhor score | Primeiro trecho | Fontes esperadas nos 5 |
|---|----------|--------------|-----------------|------------------------|
| 5 | O Plano Porto inclui telefone e o que o N1 não pode prometer sobre bug? | 0,556 | catalogo-planos.md / Plano Porto | sim, as duas. Bug ficou em 0,460 |
| 6 | Qual o caminho para abrir chamado e qual saudação o atendimento usa? | 0,582 | manual-farol-desk.md / Abrir chamado | sim, as duas. Saudação ficou em 0,580 |

## Sem resposta no acervo

| # | Pergunta | Melhor score | Primeiro trecho |
|---|----------|--------------|-----------------|
| 7 | Qual é o CNPJ da Farol Software? | 0,543 | politica-interna-acesso.md / Senha |
| 8 | A Farol Software integra com SAP? | 0,577 | atendimento-roteiro.md / Saudação |
| 9 | Qual é o desconto anual dos planos? | 0,500 | catalogo-planos.md / Plano Luz |
| 10 | Qual é o endereço do escritório da Farol Software? | 0,619 | atendimento-roteiro.md / Saudação |

## O que a tabela mostra

O menor melhor score entre as perguntas respondíveis é 0,556, na pergunta 5. O maior melhor score entre as perguntas sem resposta é 0,619, na pergunta 10. Um único corte não separa os dois grupos: um mínimo que rejeita a pergunta 10 também rejeita as perguntas 5 e 6.
