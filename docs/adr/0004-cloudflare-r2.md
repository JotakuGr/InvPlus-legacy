# ADR 0004 - Cloudflare R2

## Status

Preparado.

## Contexto

Arquivos do ERP podem crescer e não devem depender apenas do disco local.

## Decisão

Preparar `CLOUDFLARE_R2` como provider de storage.

## Consequências

- Upload local continua disponível para desenvolvimento.
- Produção pode usar storage externo e CDN.
- Metadados de arquivo ficam desacoplados do provedor.
