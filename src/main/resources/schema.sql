CREATE TABLE IF NOT EXISTS clinic_workspace (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(1000),
    phone VARCHAR(64),
    email VARCHAR(255)
);
