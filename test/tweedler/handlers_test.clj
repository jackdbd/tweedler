(ns tweedler.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.util.response :refer [redirect]]
            [tweedler.handlers :refer [handle-create-tweed]]
            [tweedler.store :refer [get-tweeds reset-tweeds! store]]))

(deftest handle-create-tweed-test
  (reset-tweeds! store)
  (def req {:params {"title" "Some title" "content" "Some content"}})
  (let [tweeds-before (count (get-tweeds store))]
    (testing "creates a tweed and redirects to /"
      (is (= (redirect "/")
             (handle-create-tweed req)))
      (let [tweeds-after (count (get-tweeds store))]
        (is (= tweeds-after
               (inc tweeds-before)))))))
