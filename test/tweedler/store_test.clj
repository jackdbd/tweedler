(ns tweedler.store-test
  (:require [clojure.test :refer [deftest is testing]]
            [tweedler.store :refer [get-tweeds reset-tweeds! seed-tweeds! make-store]]))

(defonce ^:private store (make-store "Atom Test Store"))

(deftest seed-store-test
  (reset-tweeds! store)
  (let [tweeds-before (count (get-tweeds store))]
    (testing "Adds 3 tweeds to the store"
      (is (= 0 tweeds-before))
      (seed-tweeds! store)
      (let [tweeds-after (count (get-tweeds store))]
        (is (= 3 tweeds-after))))))
