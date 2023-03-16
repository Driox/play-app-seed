# -- Event schema
 
# --- !Ups
CREATE TABLE IF NOT EXISTS events (
  id          text        primary key,
  name        text        NOT NULL,
  sequence_nb bigint      NOT NULL DEFAULT 0,
  created_at  bigint      NOT NULL,
  created_by  text        NOT NULL,
  entity_id   text        NOT NULL,
  entity_type text        NOT NULL,
  payload     jsonb       NOT NULL,
  tags        text[]      DEFAULT '{}',

  UNIQUE(entity_id, entity_type, sequence_nb)
);

# --- !Downs

DROP TABLE events;
