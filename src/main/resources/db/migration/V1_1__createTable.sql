CREATE TABLE IF NOT EXISTS flow
(
    name    VARCHAR(255) NOT NULL,
    version INTEGER      NOT NULL,
    content TEXT,
    PRIMARY KEY (name, version)
);
