(ns tweedler.core
  "Tweedler - a simple app to start practicing Clojure."
  (:gen-class)
  (:require [clojure.string :as str]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.middleware :refer [wrap-store]]
            [tweedler.routes :refer [app-routes]]
            [tweedler.store.atom :refer [atom-store]]
            [tweedler.store.redis :refer [redis-store-hashes redis-store-list]]
            [tweedler.store.sqlite :refer [sqlite-store]])
  (:import [com.zaxxer.hikari HikariDataSource]
           [org.eclipse.jetty.server Server]))

(defn make-atom-handler
  ([]
   (make-atom-handler {:name "Tweedler Atom Store"}))
  ([{:keys [name]
     :or {name "Tweedler Atom Store"}}]
   (debug "Create atom handler")
   (-> app-routes
       (wrap-store (atom-store {:name name :atom (atom {:tweeds '()})}))
       (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true)))))

(defn make-datasource-handler
  [{:keys [datasource]}]
  (debug "Apply DB migrations")
  (migratus/migrate {:store :database :db {:datasource datasource}})
  (debug "Create datasource handler")
  (-> app-routes
      (wrap-store (sqlite-store {:datasource datasource}))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

(defn make-redis-hashes-handler
  []
  (debug "Create redis hashes handler")
  (-> app-routes
      (wrap-store (redis-store-hashes {:redis-key-prefix "tweed:"}))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

(defn make-redis-list-handler
  []
  (debug "Create redis list handler")
  (-> app-routes
      (wrap-store (redis-store-list {:redis-key "tweeds"}))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

;; Singleton that acts as the container for the Jetty server (it's stateful: it
;; can be started/stopped).
(defonce ^:private ^Server webserver (atom nil))

;; Singleton that acts as the container for the HikariCP connection pool (it's
;; stateful: it can be created/destroyed).
(defonce ^:private ^HikariDataSource hikari-ds (atom nil))
;; Alternative approach: define a dynamic variable for the datasource
;; (def ^:dynamic ^HikariDataSource *ds* nil)

(defn create-connection-pool!
  "Creates the HikariCP connection pool."
  [db-spec]
  (if (str/includes? (:jdbcUrl db-spec) ":memory:")
    (debug "Create HikariCP connection pool (in-memory DB)") 
    (debug "Create HikariCP connection pool"))
  (reset! ^HikariDataSource hikari-ds (connection/->pool HikariDataSource db-spec)))

(defn destroy-connection-pool!
  "Destroy the HikariCP connection pool."
  []
  (debug "Destroy HikariCP connection pool")
  (.close @hikari-ds)
  (reset! hikari-ds nil))

(defn create-and-run-server!
  "Creates the Jetty `Server`, run it, and store it in an atom.
   
   We use `:join? false` to avoid blocking the thread while running the server
   (this is useful when running the application in the REPL)."
  [{:keys [handler port]}]
  (debug "Create and run Jetty server, listening on port" port)
  (reset! ^Server webserver (jetty/run-jetty handler {:join? false :port port})))

(defn stop-server!
  "Stops the Jetty `Server` held in the atom."
  []
  (.stop ^Server @webserver))

(defn start!
  "Starts the app."
  [{:keys [db-spec port]
    :or {db-spec nil port 80}}]
  (if db-spec
    (do
      (create-connection-pool! db-spec) ;; maybe let the datasource handler create the connection pool
      (let [handler (make-datasource-handler {:datasource @hikari-ds})]
        (create-and-run-server! {:handler handler :port port})))
    (let [handler (make-atom-handler)]
      (create-and-run-server! {:handler handler :port port}))))

(defn stop!
  "Stops the app."
  []
  (when @hikari-ds
    (destroy-connection-pool!))
  (stop-server!))

(defn -main
  []
  (start! {:db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")}
           :port (Integer/parseInt (System/getenv "PORT"))}))

(comment
  (def db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")})
  
  (create-connection-pool! db-spec)
  (def conn (jdbc/get-connection @hikari-ds))
  (jdbc/execute! conn ["SELECT * FROM tweed"])
  (destroy-connection-pool!)
  
  (def db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")})
  (def datasource (connection/->pool HikariDataSource db-spec))
  (def ring-handler (make-datasource-handler {:datasource datasource}))
  (def request {})
  (ring-handler request)

  ;; App with atom store
  (start! {:port 3000})
  (stop!)

  ;; App with persistent SQLite store
  (start! {:db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")} :port 3000})
  (stop!)

  ;; App with in-memory SQLite store (we need to apply migrations)
  (start! {:db-spec {:jdbcUrl "jdbc:sqlite::memory:"} :port 3000})
  (def config {:store :database :db {:datasource @hikari-ds}})
  (migratus/migrate config)
  (stop!)
  )