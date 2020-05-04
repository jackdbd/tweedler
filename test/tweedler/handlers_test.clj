(ns tweedler.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.util.response :refer [redirect]]
            [tweedler.handlers :refer [create-tweed seed-tweeds]]
            [tweedler.store :refer [get-tweeds reset-tweeds!]]))

(defn- get-store
  "Access the store (private variable) from the handlers namespace.
   
   https://guide.clojure.style/#access-private-var"
  []
  @#'tweedler.handlers/store)

(deftest create-tweed-test
  (reset-tweeds! (get-store))
  (let [req {:form-params {"title" "Some title" "content" "Some content"}}
        tweeds-before (count (get-tweeds (get-store)))]
    (testing "creates a tweed and redirects to /"
      (is (= (redirect "/")
             (create-tweed req)))
      (is (= (inc tweeds-before)
             (count (get-tweeds (get-store))))))))

(deftest seed-tweeds-test
  (reset-tweeds! (get-store))
  (let [tweeds-before (count (get-tweeds (get-store)))]
    (testing "seed the store with 3 tweeds and redirects to /"
      (is (= (redirect "/")
             (seed-tweeds)))
      (is (= (+ 3 tweeds-before)
             (count (get-tweeds (get-store))))))))
