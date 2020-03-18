(ns tweedler.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [tweedler.utils :refer [escape-html]]))

(deftest escapes-html
  (testing "Replaces < with &lt; and > with &gt;"
    (is (= "&lt;script&gt;alert('Hi')&lt;/script&gt;"
           (escape-html "<script>alert('Hi')</script>")))))
