CREATE TABLE accounting."user"
(
    id           UUID         NOT NULL,
    email        VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role         VARCHAR(255) NOT NULL,
    account_id   UUID         NOT NULL,
    is_active    BOOLEAN      NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE accounting.task
(
    id               UUID    NOT NULL,
    description      VARCHAR(255),
    assignment_price INTEGER NOT NULL,
    completion_price INTEGER NOT NULL,
    CONSTRAINT pk_task PRIMARY KEY (id)
);

CREATE TABLE accounting.billing_cycle
(
    id              UUID    NOT NULL,
    account_id      UUID    NOT NULL,
    initial_balance INTEGER NOT NULL,
    start_date      date    NOT NULL,
    end_date        date    NOT NULL,
    status          VARCHAR(255),
    CONSTRAINT pk_billing_cycle PRIMARY KEY (id)
);

CREATE TABLE accounting.payment_transaction
(
    id               UUID                        NOT NULL,
    account_id       UUID                        NOT NULL,
    description      VARCHAR(255)                NOT NULL,
    credit           INTEGER                     NOT NULL,
    debit            INTEGER                     NOT NULL,
    timestamp        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type             VARCHAR(255)                NOT NULL,
    billing_cycle_id UUID,
    CONSTRAINT pk_payment_transaction PRIMARY KEY (id)
);

ALTER TABLE accounting.payment_transaction
    ADD CONSTRAINT FK_PAYMENT_TRANSACTION_ON_BILLING_CYCLE FOREIGN KEY (billing_cycle_id) REFERENCES accounting.billing_cycle (id);