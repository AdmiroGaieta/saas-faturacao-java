-- ============================================================
-- V3 — CRM + Gestão de Custos + Recursos Humanos
-- ============================================================

-- ── ENUMS CRM ─────────────────────────────────────────────────

CREATE TYPE lead_status AS ENUM (
    'NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION',
    'WON', 'LOST', 'INACTIVE'
);

CREATE TYPE lead_source AS ENUM (
    'WEBSITE', 'REFERRAL', 'COLD_CALL', 'EMAIL', 'SOCIAL_MEDIA',
    'EVENT', 'PARTNER', 'OTHER'
);

CREATE TYPE activity_type AS ENUM (
    'CALL', 'EMAIL', 'MEETING', 'TASK', 'NOTE', 'WHATSAPP', 'VISIT'
);

CREATE TYPE activity_status AS ENUM (
    'PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED'
);

CREATE TYPE proposal_status AS ENUM (
    'DRAFT', 'SENT', 'VIEWED', 'ACCEPTED', 'REJECTED', 'EXPIRED'
);

CREATE TYPE contract_status AS ENUM (
    'DRAFT', 'ACTIVE', 'EXPIRED', 'TERMINATED', 'RENEWED'
);

-- ── ENUMS DESPESAS ─────────────────────────────────────────────

CREATE TYPE expense_status AS ENUM (
    'DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PAID', 'CANCELLED'
);

CREATE TYPE expense_category_type AS ENUM (
    'OPERATIONAL', 'ADMINISTRATIVE', 'FINANCIAL', 'MARKETING',
    'HR', 'IT', 'TRAVEL', 'UTILITIES', 'OTHER'
);

CREATE TYPE payable_status AS ENUM (
    'PENDING', 'PARTIAL', 'PAID', 'OVERDUE', 'CANCELLED'
);

-- ── ENUMS RH ──────────────────────────────────────────────────

CREATE TYPE employee_status AS ENUM (
    'ACTIVE', 'INACTIVE', 'SUSPENDED', 'ON_LEAVE', 'TERMINATED'
);

CREATE TYPE contract_type AS ENUM (
    'PERMANENT', 'FIXED_TERM', 'PART_TIME', 'INTERN', 'CONSULTANT', 'OUTSOURCED'
);

CREATE TYPE leave_type AS ENUM (
    'ANNUAL', 'SICK', 'MATERNITY', 'PATERNITY',
    'UNPAID', 'COMPASSIONATE', 'STUDY', 'OTHER'
);

CREATE TYPE leave_status AS ENUM (
    'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'
);

CREATE TYPE payroll_status AS ENUM (
    'DRAFT', 'CALCULATED', 'APPROVED', 'PAID', 'CANCELLED'
);

CREATE TYPE attendance_type AS ENUM (
    'PRESENT', 'ABSENT', 'HALF_DAY', 'REMOTE', 'PUBLIC_HOLIDAY', 'WEEKEND'
);

CREATE TYPE gender AS ENUM ('MALE', 'FEMALE', 'OTHER');

-- ══════════════════════════════════════════════════════════════
-- CRM TABLES
-- ══════════════════════════════════════════════════════════════

CREATE TABLE crm_pipelines (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE crm_stages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_id UUID NOT NULL REFERENCES crm_pipelines(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    probability NUMERIC(5,2) NOT NULL DEFAULT 0,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    color       VARCHAR(7) DEFAULT '#3b82f6',
    is_won      BOOLEAN NOT NULL DEFAULT FALSE,
    is_lost     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE crm_leads (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    pipeline_id     UUID REFERENCES crm_pipelines(id),
    stage_id        UUID REFERENCES crm_stages(id),
    customer_id     UUID REFERENCES customers(id),
    assigned_to     UUID REFERENCES users(id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          lead_status NOT NULL DEFAULT 'NEW',
    source          lead_source DEFAULT 'OTHER',
    value           NUMERIC(15,2),
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    probability     NUMERIC(5,2) DEFAULT 0,
    expected_close  DATE,
    closed_at       TIMESTAMP,
    lost_reason     TEXT,
    -- Contacto (quando não é cliente existente)
    contact_name    VARCHAR(255),
    contact_email   VARCHAR(255),
    contact_phone   VARCHAR(30),
    contact_company VARCHAR(255),
    contact_nif     VARCHAR(20),
    tags            TEXT[],
    notes           TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE crm_activities (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    lead_id     UUID REFERENCES crm_leads(id) ON DELETE CASCADE,
    customer_id UUID REFERENCES customers(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    type        activity_type NOT NULL DEFAULT 'TASK',
    status      activity_status NOT NULL DEFAULT 'PENDING',
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_min INTEGER,
    outcome     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE crm_proposals (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id),
    lead_id         UUID REFERENCES crm_leads(id),
    customer_id     UUID NOT NULL REFERENCES customers(id),
    number          VARCHAR(50) NOT NULL UNIQUE,
    title           VARCHAR(255) NOT NULL,
    status          proposal_status NOT NULL DEFAULT 'DRAFT',
    issue_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    expiry_date     DATE,
    subtotal        NUMERIC(15,2) NOT NULL DEFAULT 0,
    tax_amount      NUMERIC(15,2) NOT NULL DEFAULT 0,
    discount        NUMERIC(15,2) NOT NULL DEFAULT 0,
    total           NUMERIC(15,2) NOT NULL DEFAULT 0,
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    notes           TEXT,
    terms           TEXT,
    sent_at         TIMESTAMP,
    viewed_at       TIMESTAMP,
    accepted_at     TIMESTAMP,
    rejected_at     TIMESTAMP,
    invoice_id      UUID REFERENCES invoices(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE crm_proposal_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proposal_id     UUID NOT NULL REFERENCES crm_proposals(id) ON DELETE CASCADE,
    product_id      UUID REFERENCES products(id),
    description     VARCHAR(500) NOT NULL,
    quantity        NUMERIC(10,2) NOT NULL,
    unit_price      NUMERIC(15,2) NOT NULL,
    discount_pct    NUMERIC(5,2) NOT NULL DEFAULT 0,
    tax_rate        NUMERIC(5,2) NOT NULL DEFAULT 0,
    total           NUMERIC(15,2) NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE crm_contracts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id),
    customer_id     UUID NOT NULL REFERENCES customers(id),
    lead_id         UUID REFERENCES crm_leads(id),
    number          VARCHAR(50) NOT NULL UNIQUE,
    title           VARCHAR(255) NOT NULL,
    status          contract_status NOT NULL DEFAULT 'DRAFT',
    start_date      DATE NOT NULL,
    end_date        DATE,
    renewal_date    DATE,
    value           NUMERIC(15,2),
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    billing_cycle   VARCHAR(20),
    auto_renew      BOOLEAN NOT NULL DEFAULT FALSE,
    description     TEXT,
    terms           TEXT,
    signed_at       TIMESTAMP,
    terminated_at   TIMESTAMP,
    termination_reason TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ══════════════════════════════════════════════════════════════
-- GESTÃO DE CUSTOS / DESPESAS
-- ══════════════════════════════════════════════════════════════

CREATE TABLE expense_categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    parent_id   UUID REFERENCES expense_categories(id),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(20),
    type        expense_category_type NOT NULL DEFAULT 'OPERATIONAL',
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cost_centers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(20),
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE suppliers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    trade_name      VARCHAR(255),
    nif             VARCHAR(20),
    email           VARCHAR(255),
    phone           VARCHAR(30),
    address         VARCHAR(500),
    city            VARCHAR(100),
    province        VARCHAR(100),
    bank_name       VARCHAR(100),
    bank_account    VARCHAR(100),
    bank_iban       VARCHAR(50),
    payment_terms   INTEGER NOT NULL DEFAULT 30,
    credit_limit    NUMERIC(15,2),
    notes           TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP
);

CREATE TABLE expenses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id),
    category_id     UUID REFERENCES expense_categories(id),
    cost_center_id  UUID REFERENCES cost_centers(id),
    supplier_id     UUID REFERENCES suppliers(id),
    submitted_by    UUID NOT NULL REFERENCES users(id),
    approved_by     UUID REFERENCES users(id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          expense_status NOT NULL DEFAULT 'DRAFT',
    amount          NUMERIC(15,2) NOT NULL,
    tax_amount      NUMERIC(15,2) NOT NULL DEFAULT 0,
    total           NUMERIC(15,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,
    paid_at         TIMESTAMP,
    payment_method  payment_method,
    reference       VARCHAR(100),
    receipt_url     VARCHAR(500),
    notes           TEXT,
    rejected_reason TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE accounts_payable (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id),
    expense_id      UUID REFERENCES expenses(id),
    supplier_id     UUID REFERENCES suppliers(id),
    description     VARCHAR(255) NOT NULL,
    amount          NUMERIC(15,2) NOT NULL,
    amount_paid     NUMERIC(15,2) NOT NULL DEFAULT 0,
    amount_due      NUMERIC(15,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    issue_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE NOT NULL,
    paid_at         TIMESTAMP,
    status          payable_status NOT NULL DEFAULT 'PENDING',
    reference       VARCHAR(100),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ══════════════════════════════════════════════════════════════
-- RECURSOS HUMANOS
-- ══════════════════════════════════════════════════════════════

CREATE TABLE hr_departments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    parent_id   UUID REFERENCES hr_departments(id),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(20),
    description TEXT,
    manager_id  UUID,  -- FK para employees (circular, adicionada depois)
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hr_positions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    department_id   UUID REFERENCES hr_departments(id),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(20),
    description     TEXT,
    min_salary      NUMERIC(15,2),
    max_salary      NUMERIC(15,2),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hr_employees (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id             UUID REFERENCES users(id),
    department_id       UUID REFERENCES hr_departments(id),
    position_id         UUID REFERENCES hr_positions(id),
    manager_id          UUID REFERENCES hr_employees(id),
    -- Dados pessoais
    employee_number     VARCHAR(20) UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    gender              gender,
    birth_date          DATE,
    nationality         VARCHAR(100) DEFAULT 'Angolana',
    id_number           VARCHAR(20),   -- BI
    nif                 VARCHAR(20),
    inss_number         VARCHAR(20),   -- INSS
    -- Contactos
    email               VARCHAR(255),
    email_work          VARCHAR(255),
    phone               VARCHAR(30),
    phone_emergency     VARCHAR(30),
    emergency_contact   VARCHAR(100),
    address             VARCHAR(500),
    city                VARCHAR(100),
    province            VARCHAR(100),
    -- Dados laborais
    hire_date           DATE NOT NULL,
    termination_date    DATE,
    status              employee_status NOT NULL DEFAULT 'ACTIVE',
    contract_type       contract_type NOT NULL DEFAULT 'PERMANENT',
    -- Remuneração
    base_salary         NUMERIC(15,2) NOT NULL DEFAULT 0,
    currency            VARCHAR(5) NOT NULL DEFAULT 'AOA',
    bank_name           VARCHAR(100),
    bank_account        VARCHAR(100),
    bank_iban           VARCHAR(50),
    -- Outros
    photo               VARCHAR(500),
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP
);

-- FK circular department manager
ALTER TABLE hr_departments ADD CONSTRAINT fk_dept_manager
    FOREIGN KEY (manager_id) REFERENCES hr_employees(id) ON DELETE SET NULL;

CREATE TABLE hr_employment_contracts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL REFERENCES hr_employees(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES companies(id),
    contract_type   contract_type NOT NULL DEFAULT 'PERMANENT',
    start_date      DATE NOT NULL,
    end_date        DATE,
    probation_end   DATE,
    position_id     UUID REFERENCES hr_positions(id),
    department_id   UUID REFERENCES hr_departments(id),
    base_salary     NUMERIC(15,2) NOT NULL,
    currency        VARCHAR(5) NOT NULL DEFAULT 'AOA',
    weekly_hours    NUMERIC(5,2) DEFAULT 40,
    status          contract_status NOT NULL DEFAULT 'ACTIVE',
    terms           TEXT,
    signed_at       TIMESTAMP,
    terminated_at   TIMESTAMP,
    termination_reason TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hr_leave_balances (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL REFERENCES hr_employees(id) ON DELETE CASCADE,
    year            INTEGER NOT NULL,
    leave_type      leave_type NOT NULL DEFAULT 'ANNUAL',
    entitled_days   NUMERIC(5,1) NOT NULL DEFAULT 22,
    used_days       NUMERIC(5,1) NOT NULL DEFAULT 0,
    pending_days    NUMERIC(5,1) NOT NULL DEFAULT 0,
    remaining_days  NUMERIC(5,1) GENERATED ALWAYS AS (entitled_days - used_days - pending_days) STORED,
    UNIQUE (employee_id, year, leave_type)
);

CREATE TABLE hr_leaves (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL REFERENCES hr_employees(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES companies(id),
    approved_by     UUID REFERENCES hr_employees(id),
    leave_type      leave_type NOT NULL DEFAULT 'ANNUAL',
    status          leave_status NOT NULL DEFAULT 'PENDING',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    days            NUMERIC(5,1) NOT NULL,
    reason          TEXT,
    notes           TEXT,
    rejected_reason TEXT,
    approved_at     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hr_attendance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL REFERENCES hr_employees(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES companies(id),
    date            DATE NOT NULL,
    type            attendance_type NOT NULL DEFAULT 'PRESENT',
    check_in        TIME,
    check_out       TIME,
    hours_worked    NUMERIC(5,2),
    overtime_hours  NUMERIC(5,2) DEFAULT 0,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, date)
);

-- ── FOLHA DE PAGAMENTO ────────────────────────────────────────

CREATE TABLE hr_payrolls (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id),
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL CHECK (period_month BETWEEN 1 AND 13), -- 13 = 13º mês
    description     VARCHAR(255),
    status          payroll_status NOT NULL DEFAULT 'DRAFT',
    total_gross     NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_inss_emp  NUMERIC(15,2) NOT NULL DEFAULT 0, -- INSS empregado (3%)
    total_inss_emp2 NUMERIC(15,2) NOT NULL DEFAULT 0, -- INSS empregador (8%)
    total_irt       NUMERIC(15,2) NOT NULL DEFAULT 0, -- IRT (tabela progressiva)
    total_deductions NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_net       NUMERIC(15,2) NOT NULL DEFAULT 0,
    approved_by     UUID REFERENCES users(id),
    approved_at     TIMESTAMP,
    paid_at         TIMESTAMP,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, period_year, period_month)
);

CREATE TABLE hr_payslips (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payroll_id      UUID NOT NULL REFERENCES hr_payrolls(id) ON DELETE CASCADE,
    employee_id     UUID NOT NULL REFERENCES hr_employees(id),
    company_id      UUID NOT NULL REFERENCES companies(id),
    -- Dados do período
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,
    working_days    INTEGER NOT NULL DEFAULT 22,
    attended_days   INTEGER NOT NULL DEFAULT 22,
    absent_days     INTEGER NOT NULL DEFAULT 0,
    -- Remuneração base
    base_salary     NUMERIC(15,2) NOT NULL,
    -- Subsídios (abonos)
    food_allowance        NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Subsídio de alimentação
    transport_allowance   NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Subsídio de transporte
    housing_allowance     NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Subsídio de habitação
    family_allowance      NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Abono de família
    production_bonus      NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Bónus de produção
    overtime_pay          NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Horas extra
    other_allowances      NUMERIC(15,2) NOT NULL DEFAULT 0,
    -- Gross
    gross_salary    NUMERIC(15,2) NOT NULL,
    -- Descontos legais Angola
    inss_employee   NUMERIC(15,2) NOT NULL DEFAULT 0,  -- 3% do salário base
    inss_employer   NUMERIC(15,2) NOT NULL DEFAULT 0,  -- 8% do salário base (custo empresa)
    irt_amount      NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Imposto Rendimento Trabalho
    -- Outros descontos
    advance_deduction     NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Adiantamento
    loan_deduction        NUMERIC(15,2) NOT NULL DEFAULT 0,  -- Empréstimo
    other_deductions      NUMERIC(15,2) NOT NULL DEFAULT 0,
    -- Totais
    total_allowances  NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_deductions  NUMERIC(15,2) NOT NULL DEFAULT 0,
    net_salary        NUMERIC(15,2) NOT NULL,
    -- Estado
    is_paid           BOOLEAN NOT NULL DEFAULT FALSE,
    paid_at           TIMESTAMP,
    pdf_url           VARCHAR(500),
    notes             TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (payroll_id, employee_id)
);

-- ── ÍNDICES ───────────────────────────────────────────────────

CREATE INDEX idx_crm_leads_company    ON crm_leads(company_id);
CREATE INDEX idx_crm_leads_status     ON crm_leads(status);
CREATE INDEX idx_crm_leads_assigned   ON crm_leads(assigned_to);
CREATE INDEX idx_crm_activities_lead  ON crm_activities(lead_id);
CREATE INDEX idx_crm_activities_user  ON crm_activities(user_id);
CREATE INDEX idx_expenses_company     ON expenses(company_id);
CREATE INDEX idx_expenses_status      ON expenses(status);
CREATE INDEX idx_employees_company    ON hr_employees(company_id);
CREATE INDEX idx_employees_dept       ON hr_employees(department_id);
CREATE INDEX idx_payslips_payroll     ON hr_payslips(payroll_id);
CREATE INDEX idx_payslips_employee    ON hr_payslips(employee_id);
CREATE INDEX idx_attendance_employee  ON hr_attendance(employee_id, date);
CREATE INDEX idx_leaves_employee      ON hr_leaves(employee_id);

-- ── TRIGGERS updated_at ───────────────────────────────────────
CREATE TRIGGER trg_crm_leads_upd       BEFORE UPDATE ON crm_leads        FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_crm_activities_upd  BEFORE UPDATE ON crm_activities   FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_crm_proposals_upd   BEFORE UPDATE ON crm_proposals    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_crm_contracts_upd   BEFORE UPDATE ON crm_contracts    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_expenses_upd        BEFORE UPDATE ON expenses         FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_suppliers_upd       BEFORE UPDATE ON suppliers        FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_accounts_pay_upd    BEFORE UPDATE ON accounts_payable FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_employees_upd       BEFORE UPDATE ON hr_employees     FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_payrolls_upd        BEFORE UPDATE ON hr_payrolls      FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_payslips_upd        BEFORE UPDATE ON hr_payslips      FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_leaves_upd          BEFORE UPDATE ON hr_leaves        FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_emp_contracts_upd   BEFORE UPDATE ON hr_employment_contracts FOR EACH ROW EXECUTE FUNCTION update_updated_at();
