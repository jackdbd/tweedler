(ns tweedler.store.redis-test
  (:require [clojure.test :refer [deftest is testing]]
            [tweedler.store.redis :refer [redis-store-hashes redis-store-list]]
            [tweedler.store.protocol :refer [get-tweeds reset-tweeds! seed-tweeds!]]))


;; TODO: re-enable this test when connection to Redis can be passed to the Redis-based store
#_(deftest redis-list-store-seed-tweeds!-test
  (let [store (redis-store-list {:redis-key "tweeds"})]
    (reset-tweeds! store)
    (let [tweeds-before (count (get-tweeds store))]
      (is (= 0 tweeds-before))
      (testing "Adds 3 tweeds to the store"
        (seed-tweeds! store)
        (let [tweeds-after (count (get-tweeds store))]
          (is (= 3 tweeds-after)))))
    (reset-tweeds! store)))

;; TODO: re-enable this test when connection to Redis can be passed to the Redis-based store
#_(deftest redis-hashes-store-seed-tweeds!-test
  (let [store (redis-store-hashes {:redis-key-prefix "tweed:"})]
    (reset-tweeds! store)
    (let [tweeds-before (count (get-tweeds store))]
      (is (= 0 tweeds-before))
      (testing "Adds 3 tweeds to the store"
        (seed-tweeds! store)
        (let [tweeds-after (count (get-tweeds store))]
          (is (= 3 tweeds-after)))))
    (reset-tweeds! store)))
