# ADR 0005 - Abstração de Storage

## Status

Aceito.

## Contexto

O ERP precisa anexar imagens, documentos, XMLs e PDFs em vários módulos.

## Decisão

Criar `StorageService`, `StorageFacade`, `StoredFile` e providers.

## Consequências

- Arquivo não conhece detalhes de storage.
- Troca de provider fica localizada.
- ProdutoImagem e DocumentoPessoa apenas vinculam metadados.
