-- ============================================================
-- SaaS Faturação AGT — Migração V1
-- Schema inicial completo (equivalente ao Prisma schema)
-- ============================================================

-- ── ENUMS ────────────────────────────────────────────────────

CREATE TYPE user_role AS ENUM (
    'SUPER_ADMIN', 'ADMIN', 'MANAGER', 'ACCOUNTANT', 'VIEWER'
);

CREATE TYPE user_status AS ENUM (
    'ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION'
);

CREATE TYPE company_status AS ENUM (
    'ACTIVE', 'INACTIVE', 'SUSPENDED', 'TRIAL'
);

CREATE TYPE company_type AS ENUM (
    'INDIVIDUAL', 'LDA', 'SA', 'UNIPESSOAL', 'OTHER'
);

CREATE TYPE tax_regime AS ENUM (
    'SIMPLIFIED', 'GENERAL', 'EXEMPT'
);

CREATE TYPE invoice_type AS ENUM (
    'INVOICE', 'INVOICE_RECEIPT', 'RECEIPT',
    'CREDIT_NOTE', 'DEBIT_NOTE', 'PRO_FORMA', 'QUOTE'
);

CREATE TYPE invoice_status AS ENUM (
    'DRAFT', 'PENDING', 'SENT', 'PAID',
    'PARTIALLY_PAID', 'OVERDUE', 'CANCELLED', 'CREDITED'
);

CREATE TYPE payment_method AS ENUM (
    'CASH', 'BANK_TRANSFER', 'CHECK',
    'MULTICAIXA', 'CREDIT_CARD', 'DEBIT_CARD', 'OTHER'
);

CREATE TYPE customer_type AS ENUM ('INDIVIDUAL', 'COMPANY');

CREATE TYPE product_type AS ENUM ('PRODUCT', 'SERVICE');

CREATE TYPE product_unit AS ENUM (
    'UNIT', 'KG', 'LITER', 'METER', 'M2', 'M3',
    'HOUR', 'DAY', 'MONTH', 'YEAR'
);

CREATE TYPE subscription_plan AS ENUM (
    'FREE', 'STARTER', 'PROFESSIONAL', 'ENTERPRISE'
);

CREATE TYPE subscription_status AS ENUM (
    'ACTIVE', 'TRIALING', 'PAST_DUE', 'CANCELLED', 'EXPIRED'
);

CREATE TYPE audit_action AS ENUM (
    'CREATE', 'UPDATE', 'DELETE',
    'LOGIN', 'LOGOUT', 'EXPORT', 'CANCEL', 'SEND', 'PAY'
);

-- ── TABELAS ───────────────────────────────────────────────────

CREATE TABLE users (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(255) NOT NULL UNIQUE,
    password          VARCHAR(255) NOT NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    phone             VARCHAR(30),
    avatar            VARCHAR(500),
    role              VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
    status            VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified_at TIMESTAMP,
    last_login_at     TIMESTAMP,
    refresh_token     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at        TIMESTAMP,
    
    CONSTRAINT chk_user_role CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'ACCOUNTANT', 'VIEWER')),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION'))
);

CREATE TABLE companies (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  VARCHAR(255) NOT NULL,
    trade_name            VARCHAR(255),
    nif                   VARCHAR(20) NOT NULL UNIQUE,
    type                  VARCHAR(50) NOT NULL DEFAULT 'LDA',
    tax_regime            VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    status                VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    address               VARCHAR(500),
    city                  VARCHAR(100),
    province              VARCHAR(100),
    country               VARCHAR(100) NOT NULL DEFAULT 'Angola',
    postal_code           VARCHAR(20),
    phone                 VARCHAR(30),
    phone2                VARCHAR(30),
    email                 VARCHAR(255),
    website               VARCHAR(255),
    bank_name             VARCHAR(100),
    bank_account          VARCHAR(100),
    bank_iban             VARCHAR(50),
    invoice_prefix      VARCHAR(20) NOT NULL DEFAULT 'FT',
    invoice_next_number   INTEGER NOT NULL DEFAULT 1,
    invoice_series      VARCHAR(20) NOT NULL DEFAULT 'A',
    default_due_days      INTEGER NOT NULL DEFAULT 30,
    default_currency    VARCHAR(10) NOT NULL DEFAULT 'AOA',
    default_notes         TEXT,
    terms_conditions      TEXT,
    logo                  VARCHAR(500),
    agt_credentials       TEXT,
    agt_registered        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMP,

    CONSTRAINT chk_company_type CHECK (type IN ('INDIVIDUAL', 'LDA', 'SA', 'UNIPESSOAL', 'OTHER')),
    CONSTRAINT chk_company_tax_regime CHECK (tax_regime IN ('SIMPLIFIED', 'GENERAL', 'EXEMPT')),
    CONSTRAINT chk_company_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'TRIAL'))
);

CREATE TABLE company_users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    role        VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, company_id),

    CONSTRAINT chk_company_user_role CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'ACCOUNTANT', 'VIEWER'))
);

CREATE TABLE tax_rates (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    rate        NUMERIC(5,2) NOT NULL,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL DEFAULT 'COMPANY',
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    company_name    VARCHAR(255),
    trade_name      VARCHAR(255),
    nif             VARCHAR(20),
    address         VARCHAR(500),
    city            VARCHAR(100),
    province        VARCHAR(100),
    country         VARCHAR(100) NOT NULL DEFAULT 'Angola',
    postal_code     VARCHAR(20),
    email           VARCHAR(255),
    phone           VARCHAR(30),
    phone2          VARCHAR(30),
    credit_limit    NUMERIC(15,2),
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    payment_terms   INTEGER NOT NULL DEFAULT 30,
    notes           TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP,

    CONSTRAINT chk_customer_type CHECK (type IN ('INDIVIDUAL', 'COMPANY'))
);


CREATE TABLE products (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    tax_rate_id     UUID REFERENCES tax_rates(id),
    code            VARCHAR(50),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(50) NOT NULL DEFAULT 'SERVICE',
    unit            VARCHAR(50) NOT NULL DEFAULT 'UNIT',
    price           NUMERIC(15,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    manage_stock    BOOLEAN NOT NULL DEFAULT FALSE,
    stock_quantity  NUMERIC(10,2),
    min_stock       NUMERIC(10,2),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP,

    CONSTRAINT chk_product_type CHECK (type IN ('PRODUCT', 'SERVICE')),
    CONSTRAINT chk_product_unit CHECK (unit IN ('UNIT', 'KG', 'LITER', 'METER', 'M2', 'M3', 'HOUR', 'DAY', 'MONTH', 'YEAR'))
);

CREATE TABLE invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          UUID NOT NULL REFERENCES companies(id),
    customer_id         UUID NOT NULL REFERENCES customers(id),
    type                VARCHAR(50) NOT NULL DEFAULT 'INVOICE',
    series              VARCHAR(5) NOT NULL,
    number              INTEGER NOT NULL,
    full_number         VARCHAR(50) NOT NULL UNIQUE,
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    issue_date          DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date            DATE,
    paid_at             TIMESTAMP,
    sent_at             TIMESTAMP,
    cancelled_at        TIMESTAMP,
    currency            VARCHAR(5) NOT NULL DEFAULT 'AOA',
    exchange_rate       NUMERIC(15,6) NOT NULL DEFAULT 1,
    subtotal            NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_amount          NUMERIC(15,2) NOT NULL DEFAULT 0,
    discount_amount     NUMERIC(15,2) NOT NULL DEFAULT 0,
    total               NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_paid         NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_due          NUMERIC(15,2) NOT NULL DEFAULT 0,
    reference_number    VARCHAR(100),
    purchase_order      VARCHAR(100),
    original_invoice_id UUID REFERENCES invoices(id),
    notes               TEXT,
    terms_conditions    TEXT,
    pdf_url             VARCHAR(500),
    pdf_generated_at    TIMESTAMP,
    agt_hash            VARCHAR(255),
    agt_qr_code         TEXT,
    agt_submitted_at    TIMESTAMP,
    agt_status          VARCHAR(50),
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE invoice_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id      UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id      UUID REFERENCES products(id),
    tax_rate_id     UUID REFERENCES tax_rates(id),
    description     VARCHAR(500) NOT NULL,
    quantity        NUMERIC(10,2) NOT NULL,
    unit            VARCHAR(50) NOT NULL DEFAULT 'UNIT',
    unit_price      NUMERIC(15,2) NOT NULL,
    discount_pct    NUMERIC(5,2) NOT NULL DEFAULT 0,
    discount_amt    NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(5,2) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(15,2) NOT NULL DEFAULT 0,
    subtotal        NUMERIC(15,2) NOT NULL,
    total           NUMERIC(15,2) NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_invoice_item_unit CHECK (unit IN ('UNIT', 'KG', 'LITER', 'METER', 'M2', 'M3', 'HOUR', 'DAY', 'MONTH', 'YEAR'))
);

CREATE TABLE payments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id  UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    amount      NUMERIC(15,2) NOT NULL,
    currency    VARCHAR(5) NOT NULL DEFAULT 'AOA',
    method      VARCHAR(50) NOT NULL DEFAULT 'BANK_TRANSFER',
    paid_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    reference   VARCHAR(100),
    notes       TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_payment_method CHECK (method IN ('CASH', 'BANK_TRANSFER', 'CHECK', 'MULTICAIXA', 'CREDIT_CARD', 'DEBIT_CARD', 'OTHER'))
);

CREATE TABLE subscriptions (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id            UUID NOT NULL UNIQUE REFERENCES companies(id) ON DELETE CASCADE,
    plan                  VARCHAR(50) NOT NULL DEFAULT 'FREE',
    status                VARCHAR(50) NOT NULL DEFAULT 'TRIALING',
    trial_ends_at         TIMESTAMP,
    current_period_start  TIMESTAMP,
    current_period_end    TIMESTAMP,
    cancelled_at          TIMESTAMP,
    max_users             INTEGER NOT NULL DEFAULT 2,
    max_invoices          INTEGER NOT NULL DEFAULT 10,
    max_customers         INTEGER NOT NULL DEFAULT 50,
    max_products          INTEGER NOT NULL DEFAULT 100,
    invoices_this_month   INTEGER NOT NULL DEFAULT 0,
    external_id           VARCHAR(100),
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_subscription_plan CHECK (plan IN ('FREE', 'STARTER', 'PROFESSIONAL', 'ENTERPRISE')),
    CONSTRAINT chk_subscription_status CHECK (status IN ('ACTIVE', 'TRIALING', 'PAST_DUE', 'CANCELLED', 'EXPIRED'))
);

CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID REFERENCES companies(id),
    user_id     UUID REFERENCES users(id),
    action      VARCHAR(50) NOT NULL,
    entity      VARCHAR(100) NOT NULL,
    entity_id   UUID,
    old_values  JSONB,
    new_values  JSONB,
    ip_address  VARCHAR(50),
    user_agent  TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_audit_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'EXPORT', 'CANCEL', 'SEND', 'PAY'))
);

-- ── ÍNDICES ───────────────────────────────────────────────────

CREATE INDEX idx_invoices_company     ON invoices(company_id);
CREATE INDEX idx_invoices_customer    ON invoices(customer_id);
CREATE INDEX idx_invoices_status      ON invoices(status);
CREATE INDEX idx_invoices_issue_date  ON invoices(issue_date);
CREATE INDEX idx_invoices_full_number ON invoices(full_number);
CREATE INDEX idx_customers_company    ON customers(company_id);
CREATE INDEX idx_products_company     ON products(company_id);
CREATE INDEX idx_audit_company        ON audit_logs(company_id);
CREATE INDEX idx_audit_user           ON audit_logs(user_id);
CREATE INDEX idx_audit_entity         ON audit_logs(entity, entity_id);
CREATE INDEX idx_company_users_user   ON company_users(user_id);

-- ── TRIGGER: updated_at automático ───────────────────────────

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at    BEFORE UPDATE ON users    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_companies_updated_at BEFORE UPDATE ON companies FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_customers_updated_at BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_products_updated_at  BEFORE UPDATE ON products  FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_invoices_updated_at  BEFORE UPDATE ON invoices  FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_items_updated_at     BEFORE UPDATE ON invoice_items FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_payments_updated_at  BEFORE UPDATE ON payments  FOR EACH ROW EXECUTE FUNCTION update_updated_at();
