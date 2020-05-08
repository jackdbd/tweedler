(ns tweedler.utils-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [tweedler.utils :refer [escape-html]]))

(deftest sanitized-html-test
  (let [input-html (slurp (io/resource "templates/test-sanitize.html"))
        sanitized-html (escape-html input-html)]
    (testing "contains <a> elements"
      (is (= true (.contains sanitized-html "<a href=\"https://www.google.com/\""))))
    (testing "contains <p> elements"
      (is (= "<h1>Test h1</h1>" (re-find #"<h1>.*<\/h1>" sanitized-html))))
    (testing "contains <p> elements"
      (is (= "<p>Test p</p>" (re-find #"<p>.*<\/p>" sanitized-html))))
    (testing "does not contain <script> elements"
      (is (= false (.contains sanitized-html "<script"))))))
