(ns tweedler.db
  "This namespace represents the bridge between the database world and the
   clojure world."
  (:require [environ.core :refer [env]]
            ;; [luminus-migrations.core :as migrations]
            [nano-id.core :refer [nano-id]]
            [tweedler.db-fns :as db-fns]))

;; The functions created by HugSQL can accept a db-spec, a connection, a
;; connection pool, or a transaction object. Let's keep it simple and use a
;; db-spec for a SQLite database.
(def db-spec {:classname "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname (env :database-subname)})

;; (defn db-reset
;;   "Reset the database (CAUTION: LOSS OF DATA).

;;   luminus-migrations is a small command line wrapper for Migratus. In Migratus
;;   resetting the database means applying all 'down' migrations, then applying all
;;   'up' migrations.

;;   [Migratus](https://github.com/yogthos/migratus)."
;;   []
;;   (println "Reset db: apply all 'down' migrations; then all 'up' migrations.")
;;   (migrations/migrate ["reset"] (select-keys env [:database-url])))

;; (defn db-migrate
;;   "Migrate the database up for all outstanding migrations (CAUTION: LOSS OF DATA).
;;   This applies all 'up' migrations that were not yet applied."
;;   []
;;   (println "Apply all 'up' db migrations.")
;;   (migrations/migrate ["migrate"] (select-keys env [:database-url])))

;; (defn db-rollback
;;   "Rollback latest database migration (CAUTION: LOSS OF DATA).
;;   This applies only the latest 'down' migration."
;;   []
;;   (println "Apply latest 'down' db migration.")
;;   (migrations/migrate ["rollback"] (select-keys env [:database-url])))

;; (defn db-create-migration
;;   "Create a new 'up' and 'down' migration file with a generated timestamp and
;;   `name`.
;;   You will need to edit those files and write the SQL statements (in the SQL
;;   dialect of your database of choice) to migrate the database yourself."
;;   [name]
;;   (migrations/create name (select-keys env [:database-url])))

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
  "Seed the database with some fakes.
   Run this function as a Leiningen task:
   lein run 'tweedler.db/-main' (or use the alias lein seed-db)"
  []
  (prn "=== Seed the database with some fakes ===")
  (db-seed)
  (let [ids (for [tweed (db-fns/get-tweeds db-spec)]
              (:id tweed))]
    (prn "IDs of fakes:" ids)))
