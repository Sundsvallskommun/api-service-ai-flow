CREATE TABLE IF NOT EXISTS `instance`
(
    `id`              VARCHAR(36)  NOT NULL,
    `municipality_id` VARCHAR(4)   NOT NULL,
    `base_url`        VARCHAR(255) NOT NULL,
    `token_url`       VARCHAR(255) NOT NULL,
    `username`        VARCHAR(255) NOT NULL,
    `password`        VARCHAR(255) NOT NULL,
    `connect_timeout` INTEGER      NOT NULL,
    `read_timeout`    INTEGER      NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `instance`
    ADD INDEX `idx_municipality_id` (`municipality_id`);
ALTER TABLE `instance`
    ADD CONSTRAINT `uk_instance_municipality_id` UNIQUE (`municipality_id`);
