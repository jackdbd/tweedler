(defproject tweedler "0.1.0-SNAPSHOT"

  ;; The description text is searchable from repositories like Clojars.
  :description "A simple app to start practicing Clojure."
  :url "https://github.com/jackdbd/tweedler"

  ;; Project's license, and whether it is OK for public repositories to host
  ;; this project's artifacts.
  :license {:name "MIT License"
            :url "https://choosealicense.com/licenses/mit/"}

  ;; Warn me of earlier versions of Leiningen and tell TravisCI to use this one.
  :min-lein-version "2.0.0"
  
  ; :global-vars {*warn-on-reflection* true}

  :dependencies [;; HTML sanitizing
                 [com.googlecode.owasp-java-html-sanitizer/owasp-java-html-sanitizer "20191001.1"]
                 
                 ;; Redis client
                 [com.taoensso/carmine "2.19.1"]
                 
                 ;; Logging
                 [com.taoensso/timbre "4.10.0"]
                 
                 ;; Routing
                 [compojure "1.6.1"]
                 
                 ;; Server-side templating
                 [enlive "1.1.6"]
                 
                 ;; Access environment variables
                 [environ "1.2.0"]
                 
                 ;; Convert SQL queries into Clojure functions
                 [com.layerware/hugsql "0.5.1"]
                 
                 ;; Database connection pool
                 [hikari-cp "2.12.0"]
                 
                 ;; Database migrations (wrapper for Migratus)
                 [luminus-migrations "0.6.7"]
                 
                 ;; Markdown parsing
                 [markdown-clj "1.10.4"]
                 
                 ;; Database migrations
                 [migratus "1.2.8"]
                 
                 ;; UUID generator
                 [nano-id "1.0.0"]
                 
                 ;; Version of Clojure used in this project
                 [org.clojure/clojure "1.10.1"]
                 
                 ;; SQLite JDBC driver. It's required, otherwise we get "No
                 ;; suitable driver found for sqlite".
                 [org.xerial/sqlite-jdbc "3.30.1"]
                 
                 ;; HTTP server abstraction
                 [ring "1.8.0"]
                 ;; add some Ring middlewares
                 [ring/ring-defaults "0.3.2"]]

  ;; What to do in the case of dependencies' version conflicts.
  ; :pedantic? :warn
  
  ;; Configuration for lein-ring plugin.
  :ring {:handler tweedler.core/handler}

  ;; Non-code files included in classpath/jar.
  :resource-paths ["resources"]
  
  :main tweedler.core
  :target-path "target/%s"
  :uberjar-name "tweedler-standalone.jar"
  
  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")}
  
  :aliases {"seed-db" ["with-profile" "dev" "run" "-m" "tweedler.db/-main"]
            "test-all" ["with-profile" "default:+1.9:+1.10" "test"]
            "tr" ["trampoline" "run" "-m" "tweedler.core/-main"]}
  
  :profiles {:uberjar {:aot :all :uberjar-name "tweedler-standalone.jar"}
             :1.9  {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10  {:dependencies [[org.clojure/clojure "1.10.1"]]}
             
             ;; Define :dev and :test as Leiningen composite profiles
             :dev [:project/dev :profiles/dev]
             :test [:project/test :profiles/test]
             
             :project/dev {:dependencies [[io.aviso/pretty "0.1.37"]
                                          [pjstadig/humane-test-output "0.10.0"]
                                          [ring/ring-mock "0.4.0"]]
                           :injections [(require 'pjstadig.humane-test-output)
                                        (pjstadig.humane-test-output/activate!)]
                           :middleware [io.aviso.lein-pretty/inject]
                           :plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                     [io.aviso/pretty "0.1.37"]
                                     [jonase/eastwood "0.3.10"]
                                     [lein-cljfmt "0.6.7" :exclusions [org.clojure/clojure]]
                                     [lein-environ "1.2.0"]
                                     [lein-ring "0.12.5"]
                                     [migratus-lein "0.7.3"]]}
             :project/test {}
             
             ;; Only edit :profiles/* in profiles.clj (not tracked under version
             ;; control). For example:
             ;; {:profiles/dev {:env {:database-url "jdbc:sqlite:MY-DB-NAME.db"}}}.
             :profiles/dev {}
             :profiles/test {}})
