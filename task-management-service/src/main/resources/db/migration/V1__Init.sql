CREATE TABLE tasks."user"
(
    id           UUID         NOT NULL,
    email        VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role         VARCHAR(255) NOT NULL,
    is_active    BOOLEAN      NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE tasks.task
(
    id          UUID         NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(255) NOT NULL,
    assignee_id UUID         NOT NULL,
    CONSTRAINT pk_task PRIMARY KEY (id)
);

ALTER TABLE tasks.task
    ADD CONSTRAINT FK_TASK_ON_ASSIGNEE FOREIGN KEY (assignee_id) REFERENCES tasks."user" (id);