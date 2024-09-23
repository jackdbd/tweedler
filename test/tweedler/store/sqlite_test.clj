(ns tweedler.store.sqlite-test
  (:require [clojure.test :refer [deftest is testing]]
            [migratus.core :as migratus]
            [next.jdbc.connection :as connection]
            [tweedler.store.protocol :refer [get-tweeds seed-tweeds!]]
            [tweedler.store.sqlite :refer [sqlite-store]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def db-spec {:jdbcUrl "jdbc:sqlite::memory:"})

(deftest sqlite-store-seed-tweeds!-test
  (with-open [^HikariDataSource ds (connection/->pool HikariDataSource db-spec)]
    (let [config {:store :database :db {:datasource ds}}
          store (sqlite-store {:datasource ds})]
      (testing "Adds 2 tweeds to the store"
        (migratus/reset config)
        (migratus/migrate config)
        (is (= 0 (count (get-tweeds store))))
        (seed-tweeds! store)
        (is (= 2 (count (get-tweeds store))))))))
