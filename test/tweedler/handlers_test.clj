(ns tweedler.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.util.response :refer [redirect]]
            [tweedler.handlers :refer [create-tweed get-store seed-tweeds]]
            [tweedler.store :refer [get-tweeds reset-tweeds!]]))

(deftest create-tweed-test
  (reset-tweeds! (get-store))
  (def req {:form-params {"title" "Some title" "content" "Some content"}})
  (let [tweeds-before (count (get-tweeds (get-store)))]
    (testing "creates a tweed and redirects to /"
      (is (= (redirect "/")
             (create-tweed req)))
      (is (= (inc tweeds-before)
             (count (get-tweeds (get-store))))))))

(deftest seed-tweeds-test
  (reset-tweeds! (get-store))
  (def req {})
  (let [tweeds-before (count (get-tweeds (get-store)))]
    (testing "seed the store with 3 tweeds and redirects to /"
      (is (= (redirect "/")
             (seed-tweeds)))
      (is (= (+ 3 tweeds-before)
             (count (get-tweeds (get-store))))))))
