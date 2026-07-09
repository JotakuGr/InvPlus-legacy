# Regras de Negócio - Cadastros

- Cadastros base usam paginação, soft delete, auditoria e versionamento.
- Categoria permite hierarquia, mas não pode ser pai dela mesma nem criar ciclo.
- Categoria usa `ordem` para listagens previsíveis.
- UnidadeMedida possui sigla única entre registros ativos.
- Marca possui nome único entre registros ativos.
- Empresa exige CNPJ único entre registros ativos.
- Arquivo registra metadados, storage, hash SHA-256 e vínculo genérico por `entidade` e `entidadeId`.
