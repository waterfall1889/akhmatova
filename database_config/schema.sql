-- PostgreSQL schema for beckend (tables match JPA entities).
-- Run after connecting to your target database, e.g. psql -U postgres -d postgres -f schema.sql

-- Optional: use a dedicated database (uncomment and run separately if needed)
-- CREATE DATABASE beckend OWNER postgres;
-- \\c beckend

CREATE TABLE IF NOT EXISTS login (
    id BIGINT PRIMARY KEY,
    password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS user_information (
    id BIGINT PRIMARY KEY,
    user_name VARCHAR(20) NOT NULL,
    user_email TEXT NOT NULL UNIQUE
);
