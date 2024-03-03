CREATE TABLE auth."user"
(
    id           UUID         NOT NULL,
    email        VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role         VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);