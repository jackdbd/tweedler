(ns tweedler.store-test
  (:require [clojure.test :refer [deftest is testing]]
            [migratus.core :as migratus]
            [next.jdbc.connection :as connection]
            [tweedler.store :as s])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")})

(deftest atom-store-seed-tweeds!-test
  (let [store (s/make-atom-store "Atom Test Store")
        tweeds-before (count (s/get-tweeds store))]
    (testing "Adds 3 tweeds to the store"
      (is (= 0 tweeds-before))
      (s/seed-tweeds! store)
      (is (= 3 (count (s/get-tweeds store)))))))

(deftest db-store-seed-tweeds!-test
  (with-open [^HikariDataSource ds (connection/->pool HikariDataSource db-spec)]
    (let [config {:store :database
                  :db {:datasource ds}}
          store (s/make-db-store ds)]
      (testing "Adds 2 tweeds to the store"
        (migratus/reset config)
        (migratus/migrate config)
        (is (= 0 (count (s/get-tweeds store))))
        (s/seed-tweeds! store)
        (is (= 2 (count (s/get-tweeds store))))))))

;; TODO: re-enable this test when connection to Redis can be passed to the Redis-based store
#_(deftest redis-list-store-seed-tweeds!-test
  (let [redis-key "tweeds"
        store (s/make-redis-store-list redis-key)]
    (s/reset-tweeds! store)
    (let [tweeds-before (count (s/get-tweeds store))]
      (is (= 0 tweeds-before))
      (testing "Adds 3 tweeds to the store"
        (s/seed-tweeds! store)
        (let [tweeds-after (count (s/get-tweeds store))]
          (is (= 3 tweeds-after)))))
    (s/reset-tweeds! store)))

;; TODO: re-enable this test when connection to Redis can be passed to the Redis-based store
#_(deftest redis-hashes-store-seed-tweeds!-test
  (let [redis-key-prefix "tweed:"
        store (s/make-redis-store-hashes redis-key-prefix)]
    (s/reset-tweeds! store)
    (let [tweeds-before (count (s/get-tweeds store))]
      (is (= 0 tweeds-before))
      (testing "Adds 3 tweeds to the store"
        (s/seed-tweeds! store)
        (let [tweeds-after (count (s/get-tweeds store))]
          (is (= 3 tweeds-after)))))
    (s/reset-tweeds! store)))
