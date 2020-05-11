-- :name get-tweeds
-- :command :query
-- :result :raw
-- :doc Get all records from the `tweed` table
SELECT * FROM tweed;

-- :name put-tweed!
-- :command :execute
-- :result :affected
-- :doc Insert a single record in the `tweed` table.
INSERT
INTO
    tweed
    (id, title, content)
VALUES (:id, :title, :content);

-- :name seed-tweed!
-- :command :execute
-- :result :affected
-- :doc Seed the `tweed` table with some fakes.
-- This is a multi-record insert with [SQL Tuple Lists](https://www.hugsql.org/#param-tuple-list).
INSERT
INTO
    tweed
    (id, title, content)
VALUES :t*:fakes;

-- :name delete-tweed!
-- :command :execute
-- :result :affected
-- :doc Delete all records from the `tweed` table.
DELETE FROM tweed;

-- :name delete-tweed-by-id!
-- :command :execute
-- :result :affected
-- :doc Delete record by id from the `tweed` table.
DELETE FROM tweed WHERE id = :id
