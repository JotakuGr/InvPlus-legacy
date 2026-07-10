# ADR 0008 - Importação de extratos por parser

## Status

Aceita.

## Contexto

Extratos podem chegar por OFX, CSV, CNAB ou APIs bancárias. Acoplar essas origens
às entidades financeiras faria o domínio depender de formatos externos.

## Decisão

Cada formato implementa `ExtratoParser` e converte o conteúdo para o modelo
intermediário `ExtratoImportado`. O serviço de importação identifica a conta e
alimenta `ExtratoBancario` e `ExtratoBancarioLancamento` pelos services existentes.
`ImportacaoExtratoBancario` registra somente o processo e seu resultado.

## Consequências

- O domínio bancário não conhece OFX, CSV ou Open Finance.
- Novos parsers podem ser adicionados sem alterar as entidades de extrato.
- Hash SHA-256 garante idempotência por conteúdo.
- Arquivos podem ser processados em memória sem exposição em storage público.
- Criação do extrato e lançamentos permanece atômica.
