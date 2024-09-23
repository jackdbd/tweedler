(ns tweedler.db
  "This namespace represents the bridge between the database world and the
   clojure world."
  (:require [nano-id.core :refer [nano-id]]
            [next.jdbc :as jdbc]
            [tweedler.db-fns :as db-fns]))

(defn db-get-tweeds
  [db-spec]
  (db-fns/get-tweeds db-spec))

(defn db-seed
  "Seed the database with some fakes (useful in the REPL)."
  [db-spec]
  (let [fake-tweeds [[(nano-id) "First fake title" "First fake content"]
                     [(nano-id) "Second fake title" "Second fake content"]]]
    (db-fns/seed-tweed! db-spec {:fakes fake-tweeds})))

(defn -main
  "Seed the database with some fakes."
  []
  ;; The functions created by HugSQL can accept a db-spec, a connection, a
  ;; connection pool, or a transaction object. Let's keep it simple and use a
  ;; db-spec for a SQLite database.
  (let [db-spec {:connection-uri (get (System/getenv) "JDBC_DATABASE_URL")}]
    (prn "=== Seed the database with some fakes ===")
    (db-seed db-spec)
    (let [ids (for [tweed (db-get-tweeds db-spec)]
                (:id tweed))]
      (prn "IDs of fakes:" ids))))

(comment
  (def db-spec {:jdbcUrl (System/getenv "JDBC_DATABASE_URL")})
  (def datasource (jdbc/get-datasource db-spec))
  (def conn (jdbc/get-connection datasource))
  (jdbc/execute! conn ["SELECT * FROM tweed"])
  (.close conn)
  )
