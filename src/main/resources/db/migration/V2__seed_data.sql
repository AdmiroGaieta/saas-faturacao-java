-- ============================================================
-- V2 — Dados Iniciais (Super Admin + Empresa Demo)
-- ============================================================

-- Super Admin (password: SuperAdmin@123)
INSERT INTO users (id, email, password, first_name, last_name, role, status, email_verified_at)
VALUES (
    gen_random_uuid(),
    'superadmin@saas.ao',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/JlKaINq',
    'Super', 'Admin',
    'SUPER_ADMIN', 'ACTIVE', NOW()
);

-- Admin Demo (password: Admin@123)
INSERT INTO users (id, email, password, first_name, last_name, role, status, email_verified_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@empresa-demo.ao',
    '$2a$12$8Vh5Q2PkOnBr7cO1EXYhUeM2.yY7oVXI3GDp.5lFH6EZB8KrYpNfC',
    'João', 'Silva',
    'ADMIN', 'ACTIVE', NOW()
);

-- Empresa Demo
INSERT INTO companies (
    id, name, trade_name, nif, type, tax_regime, status,
    address, city, province,
    phone, email,
    bank_name, bank_account,
    invoice_prefix, invoice_series, invoice_next_number, default_due_days,
    default_notes
) VALUES (
    '00000000-0000-0000-0000-000000000010',
    'Empresa Demo, Lda.', 'Demo',
    '5000000001', 'LDA', 'GENERAL', 'ACTIVE',
    'Rua do Comércio, 123, Ingombotas', 'Luanda', 'Luanda',
    '+244 222 000 000', 'geral@empresa-demo.ao',
    'BAI - Banco Angolano de Investimentos', '123456789012345678',
    'FT', 'A', 2, 30,
    'Obrigado pela sua preferência.'
);

-- Associar admin à empresa
INSERT INTO company_users (user_id, company_id, role, is_default)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000010',
    'ADMIN', TRUE
);

-- Subscrição Professional para demo
INSERT INTO subscriptions (
    company_id, plan, status,
    max_users, max_invoices, max_customers, max_products,
    current_period_start, current_period_end
) VALUES (
    '00000000-0000-0000-0000-000000000010',
    'PROFESSIONAL', 'ACTIVE',
    10, 500, 2000, 2000,
    NOW(), NOW() + INTERVAL '365 days'
);

-- Taxas IVA
INSERT INTO tax_rates (id, company_id, name, rate, is_default, is_active)
VALUES
    ('00000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-000000000010', 'IVA 14%', 14.00, TRUE, TRUE),
    ('00000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000010', 'Isento',   0.00, FALSE, TRUE),
    ('00000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000010', 'IVA 7%',   7.00, FALSE, TRUE);

-- Cliente Demo
INSERT INTO customers (
    id, company_id, type, company_name, nif,
    city, province, email, phone, payment_terms
) VALUES (
    '00000000-0000-0000-0000-000000000030',
    '00000000-0000-0000-0000-000000000010',
    'COMPANY', 'Petro Angola, S.A.', '5000000100',
    'Luanda', 'Luanda', 'compras@petroangola.ao', '+244 222 111 000', 30
);

-- Produto Demo
INSERT INTO products (
    id, company_id, tax_rate_id, code, name, type, unit, price
) VALUES (
    '00000000-0000-0000-0000-000000000040',
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000020',
    'CONS-001', 'Consultoria em TI', 'SERVICE', 'HOUR', 15000.00
);

-- Factura demo paga
INSERT INTO invoices (
    id, company_id, customer_id,
    type, series, number, full_number, status,
    issue_date, due_date, paid_at,
    subtotal, tax_amount, total, amount_paid, amount_due,
    notes
) VALUES (
    '00000000-0000-0000-0000-000000000050',
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000030',
    'INVOICE', 'A', 1, 'FT 2024/A/000001', 'PAID',
    '2024-01-15', '2024-02-15', '2024-01-20',
    120000.00, 16800.00, 136800.00, 136800.00, 0.00,
    'Consultoria em TI - Janeiro 2024'
);

INSERT INTO invoice_items (
    invoice_id, product_id, tax_rate_id,
    description, quantity, unit, unit_price,
    tax_rate, tax_amount, subtotal, total
) VALUES (
    '00000000-0000-0000-0000-000000000050',
    '00000000-0000-0000-0000-000000000040',
    '00000000-0000-0000-0000-000000000020',
    'Consultoria em TI - Janeiro 2024', 8, 'HOUR', 15000.00,
    14.00, 16800.00, 120000.00, 136800.00
);

INSERT INTO payments (invoice_id, amount, method, paid_at, reference)
VALUES (
    '00000000-0000-0000-0000-000000000050',
    136800.00, 'BANK_TRANSFER', '2024-01-20', 'TRF-20240120-001'
);
