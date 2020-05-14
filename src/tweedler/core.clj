(ns tweedler.core
  "Tweedler - a simple app to start practicing Clojure."
  (:gen-class)
  (:require
   [environ.core :refer [env]]
   [next.jdbc.connection :as connection]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [taoensso.timbre :as timbre :refer [info]]
   [tweedler.middleware :refer [wrap-store]]
   [tweedler.routes :refer [app-routes]]
   [tweedler.store :refer [make-db-store make-redis-store-list]])
  (:import [com.zaxxer.hikari HikariDataSource]
           [org.eclipse.jetty.server Server]))

(def db-spec {:jdbcUrl (env :database-url)})

(defn make-handler
  [datasource]
  (-> app-routes
      ;; (wrap-store (make-redis-store-hashes "tweed:"))
      ;; (wrap-store (make-redis-store-list "tweeds"))
      (wrap-store (make-db-store datasource))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

(def app
  "The Ring main handler (i.e. the Ring application)."
  (let [ds (connection/->pool HikariDataSource db-spec)]
    (make-handler ds)))

;; Define a singleton that acts as the container for a Jetty server (note that
;; the Jetty Server is stateful: it can be started/stopped).
(defonce ^:private ^Server webserver (atom nil))

;; Define a dynamic variable for the datasource, so we can start/stop it from
;; the REPL.
(def ^:dynamic ^HikariDataSource *ds* (connection/->pool HikariDataSource db-spec))

;; TODO: what is the best way to re-bind a HikariCP datasource and redefine the
;; ring handler? I could redefine the *datasource* symbol in start-server
;; (instead of simply use binding), and that would allow me to avoid having a
;; function to re-create the ring handler. But it doesn't look right to me. Is
;; there a better way?

(defn start-server
  "Create a Jetty `Server`, run it, and store it in an atom.
  We use #'handler (which stands for (var handler)) to avoid stopping/restarting
  the jetty webserver every time we make changes to the Ring handler. We still
  have to refresh the browser though).
  We use `:join? false` to avoid blocking the thread while running the server
  (this is useful when running the application in the REPL)."
  [port]
  (info "Create HikariDataSource")
  (binding [^HikariDataSource *ds* (connection/->pool HikariDataSource db-spec)]
    (info "Create ring handler")
    (let [handler (make-handler *ds*)]
      (info "Create and run Jetty server, listening on port" port)
      (reset! ^Server webserver (jetty/run-jetty handler {:join? false :port port})))))

(defn stop-server
  "Stop the Jetty `Server` held in the atom."
  []
  (info "Close datasource (HikariCP connection pool)")
  (.close *ds*)
  (info "Stop Jetty server")
  (.stop ^Server @webserver))

(defn -main
  "The entry-point for 'lein run'
   GOTCHA: avoid hardcoding a port number when deploying on Heroku.
   https://stackoverflow.com/a/15693371/3036129"
  [& [port]]
  (let [port (or port (Integer/valueOf ^String (env :port)) 5000)]
    (start-server port)))
