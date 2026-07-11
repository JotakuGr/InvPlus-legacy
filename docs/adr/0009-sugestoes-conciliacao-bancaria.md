# ADR 0009 - Sugestoes de conciliacao bancaria

## Decisao

- Sugestoes sao calculadas sob demanda e nao sao persistidas.
- Apenas movimentos ativos, nao conciliados, da mesma empresa, conta, tipo, valor e janela de tres dias sao candidatos.
- A pontuacao usa data, documento e similaridade textual por tokens.
- Apenas confianca ALTA ou MEDIA e retornada nesta versao.
- A confirmacao permanece manual e repete todas as validacoes de dominio.
- Movimentos legados sem conta bancaria nao participam das sugestoes.

## Consequencias

A consulta e explicavel, mas nao reserva nem garante o candidato. Concorrencia e duplicidade continuam
protegidas pelo servico de conciliacao e pelos indices unicos do banco.
