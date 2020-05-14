(ns tweedler.store-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [environ.core :refer [env]]
            [hikari-cp.core :refer [close-datasource make-datasource]]
            [migratus.core :as migratus]
            [tweedler.store :as s]))

(defonce ^:private atom-store (s/make-atom-store "Atom Test Store"))

(deftest seed-store-test
  (s/reset-tweeds! atom-store)
  (let [tweeds-before (count (s/get-tweeds atom-store))]
    (testing "Adds 3 tweeds to the store"
      (is (= 0 tweeds-before))
      (s/seed-tweeds! atom-store)
      (let [tweeds-after (count (s/get-tweeds atom-store))]
        (is (= 3 tweeds-after))))))

(deftest db-store-seed-tweeds-test
  (let [delay-ds (delay (make-datasource {:jdbc-url (env :database-url)}))]
    (jdbc/with-db-connection
      [conn {:datasource @delay-ds}]
      (let [config {:store :database
                    :db {:datasource (:datasource conn)}}]
        (testing "Adds 2 tweeds to the store"
          (migratus/reset config)
          (migratus/migrate config)
          (let [db-store (s/make-db-store conn)]
            (is (= 0 (count (s/get-tweeds db-store))))
            (s/seed-tweeds! db-store)
            (is (= 2 (count (s/get-tweeds db-store))))))))
    (close-datasource @delay-ds)))

(deftest redis-list-store-test
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

(deftest redis-hashes-store-test
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