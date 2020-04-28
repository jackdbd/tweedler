(ns tweedler.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.util.response :refer [redirect]]
            [tweedler.handlers :refer [create-tweed seed-tweeds]]
            [tweedler.store :refer [get-tweeds reset-tweeds! store]]))

(deftest create-tweed-test
  (reset-tweeds! store)
  (def req {:params {"title" "Some title" "content" "Some content"}})
  (let [tweeds-before (count (get-tweeds store))]
    (testing "creates a tweed and redirects to /"
      (is (= (redirect "/")
             (create-tweed req)))
      (let [tweeds-after (count (get-tweeds store))]
        (is (= tweeds-after
               (inc tweeds-before)))))))

(deftest seed-tweeds-test
  (reset-tweeds! store)
  (def req {})
  (let [tweeds-before (count (get-tweeds store))]
    (testing "seed the store with 3 tweeds and redirects to /"
      (is (= (redirect "/")
             (seed-tweeds req)))
      (is (= 3 (count (get-tweeds store)))))))
