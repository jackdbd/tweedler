(defproject tweedler "0.1.0-SNAPSHOT"
  :description "A simple app just to start practicing Clojure."
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://choosealicense.com/licenses/mit/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring "1.8.0"]
                 [compojure "1.6.1"]
                 [enlive "1.1.6"]
                 [markdown-clj "1.10.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.jakemccrary/lein-test-refresh "0.24.1"]]
  :main ^:skip-aot tweedler.core
  :target-path "target/%s"
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]]}
             :uberjar {:aot :all}})
