# Users schema
 
# --- !Ups
CREATE TABLE users (
    id varchar(36) primary key,
    uuid varchar(36) NOT NULL,
    created_at timestamptz NOT NULL,
    deleted_at timestamptz DEFAULT NULL,
    email text NOT NULL,
    password text NOT NULL,
    first_name text DEFAULT NULL,
    last_name text DEFAULT NULL,
    avatar_url text DEFAULT NULL,
    phone text DEFAULT NULL,
    language varchar(5) DEFAULT 'fr',
    birthday date DEFAULT NULL
);

# --- !Downs

DROP TABLE users;
