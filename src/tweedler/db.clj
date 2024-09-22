(ns tweedler.db
  "This namespace represents the bridge between the database world and the
   clojure world."
  (:require [nano-id.core :refer [nano-id]]
            [tweedler.db-fns :as db-fns]))

;; The functions created by HugSQL can accept a db-spec, a connection, a
;; connection pool, or a transaction object. Let's keep it simple and use a
;; db-spec for a SQLite database.
(def db-spec {:connection-uri (get (System/getenv) "JDBC_DATABASE_URL")})

(defn db-get-tweeds
  []
  (db-fns/get-tweeds db-spec))

(defn db-seed
  "Seed the database with some fakes (useful in the REPL)."
  []
  (let [fake-tweeds [[(nano-id) "First fake title" "First fake content"]
                     [(nano-id) "Second fake title" "Second fake content"]]]
    (db-fns/seed-tweed! db-spec {:fakes fake-tweeds})))

(defn -main
  "Seed the database with some fakes."
  []
  (prn "=== Seed the database with some fakes ===")
  (db-seed)
  (let [ids (for [tweed (db-get-tweeds)]
              (:id tweed))]
    (prn "IDs of fakes:" ids)))
