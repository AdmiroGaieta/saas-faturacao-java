# SaaS Faturação AGT — Backend Java 11

Backend completo em Java 11 com Spring Boot 2.7, JPA/Hibernate, Flyway e **JasperReports** para geração de PDFs de facturas e relatórios.

## Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 11 | Runtime |
| Spring Boot | 2.7.18 | Framework |
| Spring Security | 5.7 | JWT Auth |
| JPA / Hibernate | 5.6 | ORM |
| PostgreSQL | 15 | Base de dados |
| Flyway | 9 | Migrações |
| **JasperReports** | **6.20.6** | **PDF/XLSX/CSV** |
| MapStruct | 1.5 | DTOs |
| Lombok | 1.18 | Boilerplate |
| SpringDoc | 1.7 | Swagger UI |

## Arranque Rápido

### Pré-requisitos
- Java 11+ (JDK)
- Maven 3.8+
- Docker + Docker Compose

### 1. Base de dados
```bash
docker-compose up -d postgres
```

### 2. Compilar e arrancar
```bash
mvn clean package -DskipTests
java -jar target/faturacao-1.0.0.jar
```

### 3. Com Docker (produção)
```bash
docker-compose up -d
```

### Variáveis de Ambiente
```env
DATABASE_URL=jdbc:postgresql://localhost:5432/saas_faturacao
DATABASE_USER=postgres
DATABASE_PASS=postgres123
JWT_SECRET=chave-secreta-minimo-32-caracteres
FRONTEND_URL=http://localhost:4200
```

## Endpoints Principais

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/v1/auth/login` | Login JWT |
| POST | `/api/v1/auth/register` | Registo |
| GET | `/api/v1/companies` | Listar empresas |
| GET | `/api/v1/companies/{id}/dashboard` | Dashboard stats |
| GET | `/api/v1/companies/{id}/invoices` | Listar facturas |
| POST | `/api/v1/companies/{id}/invoices` | Criar factura |
| **GET** | **`/api/v1/companies/{cid}/invoices/{id}/pdf`** | **Download PDF (JasperReports)** |
| GET | `/api/v1/companies/{cid}/invoices/{id}/xlsx` | Download XLSX |
| POST | `/api/v1/companies/{cid}/invoices/{id}/payments` | Registar pagamento |
| POST | `/api/v1/companies/{cid}/invoices/{id}/cancel` | Anular factura |
| POST | `/api/v1/companies/{cid}/invoices/{id}/credit-note` | Nota de Crédito |
| **GET** | **`/api/v1/companies/{id}/reports/sales?format=pdf`** | **Relatório Vendas** |
| **GET** | **`/api/v1/companies/{id}/reports/iva?format=pdf`** | **Declaração IVA** |
| **GET** | **`/api/v1/companies/{id}/reports/monthly?format=xlsx`** | **Relatório Mensal** |
| GET | `/api/v1/companies/{id}/reports/customer-balance` | Saldos clientes |

**Swagger UI:** `http://localhost:8080/api/docs`

## JasperReports — Templates

Os templates JRXML estão em `src/main/resources/reports/templates/`:

| Ficheiro | Descrição |
|----------|-----------|
| `invoice.jrxml` | Comprovativo/Factura — A4 portrait, conforme AGT |
| `sales_report.jrxml` | Relatório de Vendas — A4 landscape, 11 colunas |
| `iva_report.jrxml` | Declaração de IVA — A4 portrait, agrupado por taxa |
| `monthly_report.jrxml` | Relatório Anual Mensal — A4 portrait |

Para **pré-compilar** os templates para `.jasper` (mais rápido em produção):
```bash
# Via Maven ou programaticamente no startup
# Os .jasper pré-compilados devem ficar na mesma pasta dos .jrxml
```

## Segurança

- **JWT** (HMAC SHA-256) com access token (7 dias) e refresh token (30 dias)
- **Multiempresa**: header `X-Company-Id` valida acesso por empresa
- **Roles**: SUPER_ADMIN > ADMIN > MANAGER > ACCOUNTANT > VIEWER
- **CORS**: configurável via `app.cors.allowed-origins`
- Todas as passwords em **BCrypt** (cost 12)

## Estrutura de Pacotes

```
ao.saas.faturacao/
├── config/                   # SecurityConfig, SwaggerConfig, JwtConfig
├── common/                   # BaseEntity, enums, exceptions, response wrappers
├── security/                 # JwtService, JwtAuthFilter, UserDetailsServiceImpl
└── modules/
    ├── auth/                 # Login, registo, refresh token
    ├── users/                # Perfil do utilizador
    ├── companies/            # Empresa + dashboard stats
    ├── customers/            # CRUD clientes
    ├── products/             # Catálogo produtos/serviços
    ├── invoices/             # Facturas completas + PDF JasperReports
    ├── payments/             # Pagamentos
    ├── taxrates/             # Taxas IVA por empresa
    ├── reports/              # Todos os relatórios JasperReports
    ├── subscriptions/        # Planos e limites (scheduler)
    └── audit/                # Log de auditoria assíncrono
```

## Credenciais Demo

| Email | Password | Role |
|-------|----------|------|
| admin@empresa-demo.ao | Admin@123 | ADMIN |
| superadmin@saas.ao | SuperAdmin@123 | SUPER_ADMIN |
