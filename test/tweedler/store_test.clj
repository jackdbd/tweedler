(ns tweedler.store-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [environ.core :refer [env]]
            [hikari-cp.core :refer [close-datasource make-datasource]]
            [migratus.core :as migratus]
            [tweedler.store :refer [get-tweeds reset-tweeds! seed-tweeds! make-atom-store make-db-store]]))

(defonce ^:private atom-store (make-atom-store "Atom Test Store"))

(deftest seed-store-test
  (reset-tweeds! atom-store)
  (let [tweeds-before (count (get-tweeds atom-store))]
    (testing "Adds 3 tweeds to the store"
      (is (= 0 tweeds-before))
      (seed-tweeds! atom-store)
      (let [tweeds-after (count (get-tweeds atom-store))]
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
          (let [db-store (make-db-store conn)]
            (is (= 0 (count (get-tweeds db-store))))
            (seed-tweeds! db-store)
            (is (= 2 (count (get-tweeds db-store))))))))
    (close-datasource @delay-ds)))