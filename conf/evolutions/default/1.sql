# Users schema
 
# --- !Ups
CREATE TABLE users (
    id bigserial primary key,
    uuid varchar(255) NOT NULL,
    created_at timestamp NOT NULL,
    email varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    first_name varchar(255) DEFAULT NULL,
    last_name varchar(255) DEFAULT NULL,
    avatar_url varchar(255) DEFAULT NULL,
    phone varchar(255) DEFAULT NULL,
    language varchar(5) DEFAULT 'fr',
    birthday date DEFAULT NULL,
    visible boolean DEFAULT true
);

# --- !Downs

DROP TABLE users;
