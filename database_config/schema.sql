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

-- 小说车间（表名与 notes/story 一致：story_paragragh / paragragh_text）
CREATE TABLE IF NOT EXISTS story_meta (
    story_id BIGINT PRIMARY KEY,
    story_name TEXT NOT NULL,
    author_id BIGINT NOT NULL REFERENCES user_information (id),
    build_time TIMESTAMPTZ NOT NULL,
    last_edit_time TIMESTAMPTZ NOT NULL,
    read_times BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS story_paragragh (
    paragragh_id BIGINT PRIMARY KEY,
    story_id BIGINT NOT NULL REFERENCES story_meta (story_id) ON DELETE CASCADE,
    paragraph_id BIGINT NOT NULL,
    index integer NOT NULL,
    paragraph_title TEXT NOT NULL,
    CONSTRAINT uq_story_paragragh_paragraph_id UNIQUE (paragraph_id)
);

CREATE TABLE IF NOT EXISTS paragragh_text (
    paragraph_id BIGINT PRIMARY KEY REFERENCES story_paragragh (paragraph_id) ON DELETE CASCADE,
    details TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_story_meta_author ON story_meta (author_id);

CREATE INDEX IF NOT EXISTS idx_story_paragragh_story ON story_paragragh (story_id);

CREATE INDEX IF NOT EXISTS idx_story_paragragh_story_index ON story_paragragh (story_id, index);
