CREATE TABLE users (
    id   INT,
    first_name VARCHAR NOT NULL UNIQUE,
    last_name  VARCHAR NOT NULL,
    email      VARCHAR NOT NULL,
    birthday   INT     NOT NULL,
    role       VARCHAR NOT NULL,
    status     VARCHAR NOT NULL
);

CREATE TABLE todos (
    id   INT,
    description  VARCHAR NOT NULL
);