DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS money_movement;
DROP TABLE IF EXISTS transaction_state;

CREATE TABLE IF NOT EXISTS accounts (
    account_id bigint NOT NULL,
    balance double NOT NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY(account_id)
);

CREATE TABLE IF NOT EXISTS money_movement (
    events_id identity NOT NULL,
    transaction_id varchar(36) NOT NULL,
    account_id bigint NOT NULL,
    amount double NOT NULL,
    currency varchar(3) NOT NULL,
    is_credit boolean NOT NULL,
    CONSTRAINT money_movement_pkey PRIMARY KEY (events_id)
);

CREATE TABLE IF NOT EXISTS transaction_state (
    transaction_state_id identity NOT NULL,
    transaction_id varchar(36) NOT NULL,
    initiator_account_id bigint NOT NULL,
    transaction_amount double NOT NULL,
    currency varchar(3) NOT NULL,
    response_code varchar(4) NOT NULL,
    state varchar(10) NOT NULL
);

INSERT INTO accounts values (2100001, 100);
INSERT INTO accounts values (2100002, 100);
INSERT INTO accounts values (2100003, 0);