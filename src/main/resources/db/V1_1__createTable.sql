CREATE TABLE IF NOT EXISTS flow
(
    name    VARCHAR(255) NOT NULL,
    version NUMBER       NOT NULL,
    content TEXT         NOT NULL,
    PRIMARY KEY (name, version)
)
