(ns tweedler.store.sqlite
  "Store the application state in SQLite."
  (:require [nano-id.core :refer [nano-id]]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.db-fns :as db-fns]
            [tweedler.store.protocol :refer [IStore get-tweeds put-tweed!]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defrecord SQLiteStore [^HikariDataSource datasource]
  IStore

  (get-tweeds
   [this]
   (debug "get-tweeds")
   (db-fns/get-tweeds (:datasource this)))
  
  (put-tweed!
   [this tweed]
   (let [{:keys [title content]} tweed]
     (debug "put-tweed!" title content)
     (db-fns/put-tweed! (:datasource this) {:id (nano-id) :title title :content content})))
  
  (reset-tweeds!
   [this]
   (debug "reset-tweeds!")
   (db-fns/delete-tweed! (:datasource this)))
  
  (seed-tweeds!
   [this]
   (debug "seed-tweeds!")
   (def fake-tweeds [[(nano-id) "Fake title 0" "Fake content 0"]
                     [(nano-id) "Fake title 1" "Fake content 1"]])
   (db-fns/seed-tweed! (:datasource this) {:fakes fake-tweeds})))

(defn sqlite-store
  [{:keys [datasource]}]
  (->SQLiteStore datasource))

(comment
  (def db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")})
  ;; (def datasource (jdbc/get-datasource db-spec))
  (def datasource (connection/->pool HikariDataSource db-spec))
  (def config {:store :database :db {:datasource datasource}})
  (migratus/reset config)
  (migratus/init config)
  (migratus/migrate config)
  (def store (sqlite-store {:datasource datasource}))
  (get-tweeds store)
  (put-tweed! store {:title "Hello" :content "This is my first tweed"})
  (put-tweed! store {:title "Bye" :content "This is my last tweed"})
  (get-tweeds store)

  (def db-spec {:jdbcUrl "jdbc:sqlite::memory:"})
  (def datasource (jdbc/get-datasource db-spec))
  (def conn (jdbc/get-connection datasource))
  (jdbc/execute! conn ["CREATE TABLE IF NOT EXISTS tweed (id TEXT PRIMARY KEY NOT NULL, title TEXT, content TEXT, timestamp_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)"])
  (jdbc/execute! conn ["SELECT * FROM tweed"])
  (.close conn)

  (def db-spec {:jdbcUrl "jdbc:sqlite::memory:"})
  (def datasource (connection/->pool HikariDataSource db-spec))
  (def config {:store :database :db {:datasource datasource}})
  (migratus/reset config)
  (migratus/migrate config)
  (def store (sqlite-store {:datasource datasource}))
  (get-tweeds store)
  (put-tweed! store {:title "Hello" :content "This is my first tweed"})
  (put-tweed! store {:title "Bye" :content "This is my last tweed"})
  (get-tweeds store)
  )
