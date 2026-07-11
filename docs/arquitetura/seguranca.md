# Arquitetura de seguranca

```text
Usuario -> UsuarioEmpresa -> Empresa
                    |
                    -> UsuarioEmpresaFilial -> Filial

JWT ACCESS -> JwtAuthenticationFilter -> UsuarioEmpresaAccessValidator
           -> AuthenticatedUserDetails -> EmpresaContext
```

O principal armazena somente UUIDs. Entidades JPA nao sao mantidas no `SecurityContext`.
