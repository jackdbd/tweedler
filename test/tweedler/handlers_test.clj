(ns tweedler.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as s]
            [ring.mock.request :as mock]
            [ring.util.response :refer [redirect]]
            [tweedler.core :refer [handler]]
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

(defn- contains-sub?
  [cookie-str sub]
  (let [subs (s/split cookie-str #";")]
    (some #(= sub %) subs)))

(deftest response-when-requesting-home-page-test
  (testing "has the expected Content-Type header"
    (let [response (handler (mock/request :get "/"))]
      (is (= "text/html; charset=utf-8"
             (get-in response [:headers "Content-Type"])))))
  (testing "contains the expected secure headers"
    (let [response (handler (mock/request :get "/"))
          headers (:headers response)]
      (is (contains? headers "X-XSS-Protection"))
      (is (contains? headers "X-Content-Type-Options"))
      (is (contains? headers "X-Frame-Options"))))
  (testing "has a ring-session cookie that has the expected substrings"
    (let [response (handler (mock/request :get "/"))
          headers (:headers response)
          set-cookie-header (get-in headers ["Set-Cookie"])
          ring-session-cookie (first set-cookie-header)] ; TODO what if it's not the first cookie?
      (is (.contains ring-session-cookie "ring-session"))
      (is (= true (contains-sub? ring-session-cookie "HttpOnly")))
      (is (= true (contains-sub? ring-session-cookie "Path=/")))
      (is (= true (contains-sub? ring-session-cookie "SameSite=Strict")))))
  (testing "contains a CSRF token that has the expected name"
    (let [response (handler (mock/request :get "/"))
          body (:body response)]
      (is (= true
             (some #(= "__anti-forgery-token" %) body))))))
