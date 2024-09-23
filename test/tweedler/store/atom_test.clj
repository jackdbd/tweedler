(ns tweedler.store.atom-test
  (:require [clojure.test :refer [deftest is testing]]
            [tweedler.store.atom :refer [atom-store]]
            [tweedler.store.protocol :refer [get-tweeds seed-tweeds!]]))

(deftest atom-store-seed-tweeds!-test
  (let [store (atom-store)
        tweeds-before (count (get-tweeds store))]
    (testing "Adds 3 tweeds to the store"
      (is (= 0 tweeds-before))
      (seed-tweeds! store)
      (is (= 3 (count (get-tweeds store)))))))
