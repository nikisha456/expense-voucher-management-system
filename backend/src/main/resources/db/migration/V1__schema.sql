-- ExpenseFlow schema for ABC Company

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)  NOT NULL,
    email           VARCHAR(180)  NOT NULL,
    password        VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    employee_code   VARCHAR(50),
    department      VARCHAR(100),
    signature_path  VARCHAR(255),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('EMPLOYEE', 'DIRECTOR', 'ACCOUNTS'))
);

CREATE SEQUENCE voucher_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE vouchers (
    id                        BIGSERIAL PRIMARY KEY,
    voucher_number            VARCHAR(32)    NOT NULL,
    voucher_date              DATE           NOT NULL,
    expense_date              DATE           NOT NULL,
    department_name           VARCHAR(100)   NOT NULL,
    expense_title             VARCHAR(200)   NOT NULL,
    expense_category          VARCHAR(40)    NOT NULL,
    expense_description       TEXT,
    amount                    NUMERIC(12, 2) NOT NULL,
    employee_name             VARCHAR(150)   NOT NULL,
    employee_code             VARCHAR(50),
    employee_signature_path   VARCHAR(255),
    status                    VARCHAR(30)    NOT NULL,
    director_signature_path   VARCHAR(255),
    approval_date             DATE,
    rejection_reason          TEXT,
    created_by                BIGINT         NOT NULL,
    created_at                TIMESTAMPTZ    NOT NULL,
    updated_at                TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uq_vouchers_number UNIQUE (voucher_number),
    CONSTRAINT fk_vouchers_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT chk_vouchers_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_vouchers_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_vouchers_category CHECK (expense_category IN (
        'TRAVEL', 'FOOD_MEALS', 'ACCOMMODATION', 'OFFICE_SUPPLIES', 'SOFTWARE_SUBSCRIPTIONS',
        'CLIENT_ENTERTAINMENT', 'FUEL_TRANSPORT', 'TRAINING', 'COMMUNICATION', 'MISCELLANEOUS'
    )),
    CONSTRAINT chk_rejected_reason CHECK (
        status <> 'REJECTED' OR (rejection_reason IS NOT NULL AND char_length(rejection_reason) >= 10)
    ),
    CONSTRAINT chk_approved_director_signature CHECK (
        status <> 'APPROVED' OR director_signature_path IS NOT NULL
    ),
    CONSTRAINT chk_employee_signature_after_draft CHECK (
        status = 'DRAFT' OR employee_signature_path IS NOT NULL
    )
);

CREATE TABLE voucher_status_history (
    id           BIGSERIAL PRIMARY KEY,
    voucher_id   BIGINT       NOT NULL,
    from_status  VARCHAR(30),
    to_status    VARCHAR(30)  NOT NULL,
    changed_by   BIGINT       NOT NULL,
    remarks      TEXT,
    changed_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT fk_history_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by) REFERENCES users (id),
    CONSTRAINT chk_history_to_status CHECK (to_status IN ('DRAFT', 'SUBMITTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_vouchers_status ON vouchers (status);
CREATE INDEX idx_vouchers_created_by ON vouchers (created_by);
CREATE INDEX idx_vouchers_expense_date ON vouchers (expense_date);
CREATE INDEX idx_vouchers_department ON vouchers (department_name);
CREATE INDEX idx_vouchers_category ON vouchers (expense_category);
CREATE INDEX idx_history_voucher ON voucher_status_history (voucher_id);
CREATE INDEX idx_history_changed_at ON voucher_status_history (changed_at);
