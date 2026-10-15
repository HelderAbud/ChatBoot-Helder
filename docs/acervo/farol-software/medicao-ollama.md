# Medição Ollama — 14/10/2026

Ambiente: Ubuntu 24.04 no WSL, Ollama 0.40.4, CPU. O serviço escuta em `127.0.0.1:11434`.

## Geração

- Modelo: `qwen3:8b`
- Thinking: desligado (`--think=false`)
- Prompt: `Responda só: ok`
- Resposta: `ok`

Primeira chamada, com o modelo ainda fora da memória:

| Medida | Valor |
| --- | --- |
| Duração total | 44,82 s |
| Carga do modelo | 37,90 s |
| Avaliação do prompt | 6,29 s (22 tokens, 3,50 tokens/s) |
| Geração | 0,40 s (2 tokens, 4,97 tokens/s) |
| Relógio de parede | 45,53 s |

Segunda chamada, com o modelo já na memória:

| Medida | Valor |
| --- | --- |
| Duração total | 0,89 s |
| Carga do modelo | 0,005 s |
| Avaliação do prompt | 0,43 s (22 tokens, 21 em cache, 2,32 tokens/s) |
| Geração | 0,45 s (2 tokens, 4,49 tokens/s) |
| Relógio de parede | 0,98 s |

A resposta curta completou. Não foi preciso repetir com `qwen3:4b`.

## Embedding

- Modelo: `bge-m3`
- Dimensão: **1024**
- Texto de prova: `Farol Desk`
- Primeira chamada: 9,99 s
- Segunda chamada, com o modelo já na memória: 0,84 s

## Fora desta medição

- Não há coluna `vector`.
- O status do upload continua `TEXT_STORED`.
- Não existe `POST /api/v1/questions`.
- Par registrado no AGENTS.md em 14/10/2026. O mínimo de evidência continua em aberto.
