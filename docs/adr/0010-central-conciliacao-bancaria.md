# ADR 0010 - Central de conciliacao bancaria

## Decisao

- A central nao possui entidade ou tabela propria.
- Indicadores e extratos resumidos sao calculados sob demanda por projecoes.
- Pendencias sao paginadas, limitadas a 50 itens e carregam apenas relacionamentos ManyToOne necessarios.
- Sugestoes continuam transientes e reutilizam o mesmo servico de pontuacao.
- A confirmacao em lote recebe pares explicitamente escolhidos e permite sucesso parcial.
- Cada item do lote passa por outro bean Spring com transacao `REQUIRES_NEW`.
- Toda confirmacao reutiliza e reexecuta as regras do servico de conciliacao.
- Enquanto nao existir contexto empresarial no JWT, `empresaId` e obrigatorio e cada recurso e validado contra ele.

## Consequencias

O painel permanece leve e sem estado derivado obsoleto. Falhas operacionais isoladas nao desfazem itens validos,
enquanto erros estruturais do request interrompem o lote com resposta 400.
