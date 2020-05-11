-- :doc Create table `tweed`
-- Remember that SQLite have a dynamic type system and each datatype is
-- converted into one of 5 SQLite affinities.
-- https://www.sqlite.org/datatype3.html
CREATE TABLE tweed (
    -- According to the SQL standard, PRIMARY KEY should always imply NOT NULL.
    -- Unfortunately, due to a bug in some early versions, this is not the case
    -- in SQLite.
    -- https://sqlite.org/lang_createtable.html
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT,
    content TEXT,
    timestamp_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
