(ns tweedler.db-fns
  "This namespace contains the functions that HugSQL generates from SQL queries
   and statements.
   See also: https://stackoverflow.com/a/61728066/3036129"
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(hugsql/def-db-fns "sql/tweed.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment 
  (declare get-tweeds) ;; HugSQL creates symbols from tweed.sql

  (def db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")})
  (get-tweeds db-spec)
  )