# Estrutura do Projeto — invPlus ERP

## Estrutura Geral

```text
invPlus/
backend/
frontend/
database/
docker/
docs/
scripts/
.gitignore
README.md
docker-compose.yml


backend/
└── src/main/java/br/com/invplus/
    ├── config/
    ├── shared/
    ├── security/
    └── modules/

database/
├── migrations/
├── seeds/
├── backups/
└── scripts/

docker/
├── postgres/
├── redis/
├── rabbitmq/
└── compose/

docs/
├── arquitetura/
├── api/
├── der/
├── requisitos/
├── regras-negocio/
└── sprint/

scripts/

