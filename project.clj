(defproject tweedler "0.1.0-SNAPSHOT"

  ;; The description text is searchable from repositories like Clojars.
  :description "A simple app to start practicing Clojure."
  :url "https://github.com/jackdbd/tweedler"

  ;; Project's license, and whether it is OK for public repositories to host
  ;; this project's artifacts.
  :license {:name "MIT License"
            :url "https://choosealicense.com/licenses/mit/"}

  ;; Tell TravisCI and Heroku to use at least this Leiningen version.
  :min-lein-version "2.0.0"
  
  ;; Java reflection can be bad for performance.
  ;; https://clojure.org/reference/java_interop
  ;; :global-vars {*warn-on-reflection* true}

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
                 [com.layerware/hugsql "0.5.1" :exclusions [org.clojure/java.jdbc org.clojure/tools.reader]]
                 [com.layerware/hugsql-adapter-next-jdbc "0.5.1"]

                 ;; Database connection pool
                 [com.zaxxer/HikariCP "3.4.5"]

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
                 
                 ;; Better exception reporting for Ring applications
                 [prone "2020-01-17"]

                 ;; HTTP server abstraction
                 [ring "1.8.0"]
                 ;; add some Ring middlewares
                 [ring/ring-defaults "0.3.2"]
                 
                 ;; Next-gen JDBC
                 [seancorfield/next.jdbc "1.0.424" :exclusions [org.clojure/tools.logging]]]

  ;; What to do in the case of dependencies' version conflicts.
  ;; :pedantic? :warn
  
  ;; Configuration for lein-ring plugin.
  :ring {:handler tweedler.core/app}

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
                                          [ring/ring-mock "0.4.0" :exclusions [ring/ring-codec]]]
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
             
             ;; profiles could also be stored in profiles.clj
             :profiles/dev {:env {:database-url "jdbc:sqlite:tweedler_dev.db"
                                  :not-found-page-redirect-uri "http://localhost:3000/"
                                  :redis-host "127.0.0.1"
                                  :redis-port "6379"}
                            :ring {:stacktrace-middleware prone.middleware/wrap-exceptions}}
             :profiles/test {}})
