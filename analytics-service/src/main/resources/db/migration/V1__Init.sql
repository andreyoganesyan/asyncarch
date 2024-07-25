CREATE TABLE analytics.task
(
    id               UUID NOT NULL,
    description      VARCHAR(255),
    assignment_price INTEGER,
    completion_price INTEGER,
    completion_date  DATE,
    CONSTRAINT pk_task PRIMARY KEY (id)
);

CREATE TABLE analytics.payment_transaction
(
    id          UUID                        NOT NULL,
    account_id  UUID                        NOT NULL,
    credit      INTEGER                     NOT NULL,
    debit       INTEGER                     NOT NULL,
    timestamp   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type        VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_payment_transaction PRIMARY KEY (id)
);