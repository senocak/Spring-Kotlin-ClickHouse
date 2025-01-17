CREATE TABLE roles (
    created_at DateTime64(6),
    updated_at DateTime64(6),
    id UUID,
    name String
)
ENGINE = MergeTree()
ORDER BY (id)
SETTINGS index_granularity = 8192;

CREATE TABLE users (
    created_at DateTime64(6),
    updated_at DateTime64(6),
    id UUID,
    name String,
    email String,
    password String
)
ENGINE = MergeTree()
ORDER BY (id)
SETTINGS index_granularity = 8192;

CREATE TABLE user_roles (
    role_id UUID,
    user_id UUID
)
ENGINE = MergeTree()
ORDER BY (role_id, user_id)
SETTINGS index_granularity = 8192;
