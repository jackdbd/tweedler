(defproject tweedler "0.1.0-SNAPSHOT"

  ;; The description text is searchable from repositories like Clojars.
  :description "A simple app just to start practicing Clojure."
  :url "http://example.com/FIXME"

  ;; Project's license, and whether it is OK for public repositories to host
  ;; this project's artifacts.
  :license {:name "MIT License"
            :url "https://choosealicense.com/licenses/mit/"}

  ;; Warns users of earlier versions of Leiningen.
  :min-lein-version "2.0.0"

  :dependencies [;; Use Auth0 in a Ring application
                 [auth0-ring "0.4.4"]
                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]
                 ;; Routing
                 [compojure "1.6.1"]
                 ;; Server-side templating
                 [enlive "1.1.6"]
                 ;; Markdown parsing
                 [markdown-clj "1.10.4"]
                 ;; Version of Clojure used in this project
                 [org.clojure/clojure "1.10.1"]
                 ;; HTTP server abstraction
                 [ring "1.8.0"]]
  ;; What to do in the case of dependencies' version conflicts.
  :pedantic? :warn
  
  :plugins [;; cljfmt plugin to format code idiomatically.
            [lein-cljfmt "0.6.7"]
            ;; Ring plugin to automate common Ring tasks.
            [lein-ring "0.12.5"]]
  ;; Configuration for lein-cljfmt plugin.
  :cljfmt {:indentation? true}  
  ;; Configuration for lein-ring plugin.
  :ring {:handler tweedler.core/handler}

  ; Non-code files included in classpath/jar.
  :resource-paths ["resources"]
  
  ;; Skip Ahead Of Time compilation to make it easy to deploy on a PaaS.
  ;; https://stackoverflow.com/questions/11174459/reason-for-skipping-aot
  :main ^:skip-aot tweedler.core
  :target-path "target/%s"
  
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "tweedler.core/run-dev"]}
                   :dependencies [[pjstadig/humane-test-output "0.10.0"]]
                   :plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]
                             [jonase/eastwood "0.3.5"]]}
             :uberjar {:uberjar-name "tweedler-standalone.jar"}})
