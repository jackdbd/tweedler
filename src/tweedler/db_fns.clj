(ns tweedler.db-fns
  "This namespace contains the functions that HugSQL generates from SQL queries
   and statements.
   See also: https://stackoverflow.com/a/61728066/3036129"
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "sql/tweed.sql")
