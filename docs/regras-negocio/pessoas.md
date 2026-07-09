# Regras de Negócio - Pessoas

- Pessoa é o aggregate root do módulo.
- CPF/CNPJ é único entre registros ativos.
- Cliente, Fornecedor, Funcionário e Transportadora são papéis de Pessoa.
- Uma Pessoa pode exercer múltiplos papéis.
- Services de papéis não devem alterar dados cadastrais centrais da Pessoa.
- ContatoPessoa representa contatos operacionais.
- DocumentoPessoa pode se vincular a Arquivo quando houver anexo.
