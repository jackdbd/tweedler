(ns tweedler.db-fns
  "TODO..."
  (:require [hugsql.core :as hugsql]))

;; (migrations/migrate ["migrate"] (select-keys env [:database-url]))

;; HugSQL creates functions in this Clojure namespace based on the SQL queries
;; and statements in your HugSQL-flavored SQL file.
;; The path to the SQL files is relative to the Java classpath.

;; TODO clj-kondo complains that get-tweeds and seed-tweed! are unresolved symbols
;; but it's not true: these symbols area created when calling def-db-fns.

(hugsql/def-db-fns "sql/tweed.sql")
;; (hugsql/map-of-db-fns "sql/tweed.sql")