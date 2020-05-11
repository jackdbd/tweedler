(ns tweedler.store
  "This namespace defines the application store and its methods."
  (:require
   [environ.core :refer [env]]
   [luminus-migrations.core :as migrations]
   [nano-id.core :refer [nano-id]]
   [taoensso.timbre :as timbre :refer [debug]]
   [tweedler.db-fns :as db-fns]))

;; The functions created by HugSQL can accept a db-spec, a connection, a
;; connection pool, or a transaction object. Let's keep it simple and use a
;; db-spec for a SQLite database.
(def database-spec {:classname "org.sqlite.JDBC"
                    :subprotocol "sqlite"
                    :subname (env :database-subname)})

(defprotocol TweedStore
  "An abstraction of a store that holds the application's state."
  (get-tweeds [this] "Retrieve all tweeds from the store.")
  (put-tweed! [this tweed] "Insert a new tweed in the store.")
  (reset-tweeds! [this] "Delete all tweeds from the store.")
  (seed-tweeds! [this] "Seed the store with a few tweeds."))

(defrecord AtomStore [^String name data])

(defrecord SQLiteStore [db-spec])

(extend-protocol TweedStore

  AtomStore
  (get-tweeds
    [this]
    (debug "get-tweeds")
    (get @(:data this) :tweeds))
  (put-tweed!
    [this tweed]
    (let [{:keys [title content]} tweed]
      (debug "put-tweed!" "[title:" title "; characters:" (count content) "]")
      ;; We use conj so the newest Tweed shows up first.
      (swap! (:data this) update-in [:tweeds] conj tweed)))
  (reset-tweeds!
    [this]
    (debug "reset-tweeds!")
    (swap! (:data this) assoc-in [:tweeds] []))
  (seed-tweeds!
    [this]
    (debug "seed-tweeds!")
    (put-tweed! this {:title "First tweed" :content "test content 1"})
    (put-tweed! this {:title "Second tweed" :content "test content 2"})
    (put-tweed! this {:title "Third tweed" :content "test content 3"}))

  SQLiteStore
  (get-tweeds
    [this]
    (debug "get-tweeds")
    (db-fns/get-tweeds (:db-spec this)))
  (put-tweed!
    [this tweed]
    (let [{:keys [title content]} tweed]
      (debug "put-tweed!" title content)
      (db-fns/put-tweed! (:db-spec this) {:id (nano-id) :title title :content content})))
  (reset-tweeds!
    [this]
    (debug "reset-tweeds!")
    (db-fns/delete-tweed! (:db-spec this)))
  (seed-tweeds!
    [this]
    (debug "seed-tweeds!")
    (def fake-tweeds [[(nano-id) "Fake title 0" "Fake content 0"]
                      [(nano-id) "Fake title 1" "Fake content 1"]])
    (db-fns/seed-tweed! (:db-spec this) {:fakes fake-tweeds})))

(defn make-store
  "Instantiate a store that holds some state in an atom."
  [name]
  ; https://guide.clojure.style/#record-constructors
  (->AtomStore name (atom {:tweeds '()})))

(defn make-db-store
  "Instantiate a store that get/retrieve state from a SQLite database."
  [db-spec]
  (->SQLiteStore db-spec))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Example
(def db-store (make-db-store database-spec))

;; before tests
(migrations/migrate ["reset"] (select-keys env [:database-url]))
(migrations/migrate ["migrate"] (select-keys env [:database-url]))

(let [fakes [[(nano-id) "Fake title 123" "Fake content 123"]
             [(nano-id) "Fake title 456" "Fake content 456"]]]
  (db-fns/seed-tweed! (:db-spec db-store) {:fakes fakes}))
(db-fns/get-tweeds (:db-spec db-store))

;; after tests
(migrations/migrate ["reset"] (select-keys env [:database-url]))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;