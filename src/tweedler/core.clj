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
            [tweedler.store.sqlite :refer [sqlite-store]]
            [tweedler.store.turso :refer [turso-store]])
  (:import [com.zaxxer.hikari HikariDataSource]
           [org.eclipse.jetty.server Server]))

(defn make-atom-handler
  ([]
   (make-atom-handler {:name "Tweedler Atom Store"}))
  ([{:keys [name]
     :or {name "Tweedler Atom Store"}}]
   (debug "Create atom handler" name)
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

(defn make-turso-handler
  [{:keys [database-url auth-token]}]
  (debug "Create Turso handler")
  (-> app-routes
      (wrap-store (turso-store {:database-url database-url :auth-token auth-token}))
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
  [{:keys [port store]
    :or {port 80 store nil}}]
  (let [handler (cond
                  (contains? store :sqlite) (do
                                              (create-connection-pool! (:sqlite store)) ;; maybe let the datasource handler create the connection pool
                                              (make-datasource-handler {:datasource @hikari-ds}))
                  (contains? store :turso) (make-turso-handler (:turso store))
                  (contains? store :atom) (make-atom-handler (:atom store))
                  :else (throw (ex-info "Unsupported store" {:causes #{:store-not-implemented}
                                                             :supported-stores #{:atom :sqlite :turso}})))]
    (create-and-run-server! {:handler handler :port port})))

(defn stop!
  "Stops the app."
  []
  (when @hikari-ds
    (destroy-connection-pool!))
  (stop-server!))

(defn -main
  []
  (start! {:port (Integer/parseInt (System/getenv "PORT"))
          ;;  :store {:sqlite {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")}}
           :store {:turso {:database-url (System/getenv "TURSO_DATABASE_URL")
                           :auth-token (System/getenv "TURSO_AUTH_TOKEN")}}}))

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

  (def port 3000)
  ;; App with atom store
  (start! {:store {:atom {:name "My custom store"}}
           :port port})
  (stop!)

  ;; App with persistent SQLite store
  (start! {:store {:sqlite {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")}} 
           :port port})
  (stop!)

  ;; App with in-memory SQLite store
  (start! {:store {:sqlite {:jdbcUrl "jdbc:sqlite::memory:"}}
           :port port})
  (stop!)

  ;; App with Turso
  (start! {:store {:turso {:database-url (System/getenv "TURSO_DATABASE_URL")
                           :auth-token (System/getenv "TURSO_AUTH_TOKEN")}}
           :port port})
  (stop!)

  ;; Exception: unsupported store
  (try 
    (start! {:store {:foo {:color "red"}}
             :port port})
    (catch Exception e
      (ex-data e)))
  )

(comment
  (defn my-handler
    [_req]
    {:body "<h1>hello</h1>" :status 200 :headers {"Content-Type" "text/html"}})

  (def my-ring-app (->
                    my-handler
                    (wrap-store (atom-store {:name "My Store"
                                             :atom (atom {:tweeds '()})})))))
