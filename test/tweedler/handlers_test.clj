(ns tweedler.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.util.response :refer [redirect]]
            [tweedler.handlers :refer [create-tweed seed-tweeds]]
            [tweedler.store :refer [get-tweeds make-store]]))

;;  (defn- get-store
;;    "Access the store (private variable) from the handlers namespace.

;;    https://guide.clojure.style/#access-private-var"
;;    []
;;    @#'tweedler.handlers/store)

(deftest create-tweed-test
  (let [req {:form-params {"title" "Some title" "content" "Some content"}
             :store (make-store "test-store")}
        tweeds-before (count (get-tweeds (:store req)))]
    (testing "creates a tweed and redirects to /"
      (is (= (redirect "/")
             (create-tweed req)))
      (is (= (inc tweeds-before)
             (count (get-tweeds (:store req))))))))

(deftest seed-tweeds-test
  (let [req {:store (make-store "test-store")}
        tweeds-before (count (get-tweeds (:store req)))]
    (testing "seed the store with 3 tweeds and redirects to /"
      (is (= (redirect "/")
             (seed-tweeds req)))
      (is (= (+ 3 tweeds-before)
             (count (get-tweeds (:store req))))))))
