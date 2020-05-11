(ns tweedler.store
  "This namespace defines the application store and its methods."
  (:require
   [nano-id.core :refer [nano-id]]
   [taoensso.timbre :as timbre :refer [debug]]
   [tweedler.db-fns :as db-fns]))

(defprotocol TweedStore
  "An abstraction of a store that holds the application's state."
  (get-tweeds [this] "Retrieve all tweeds from the store.")
  (put-tweed! [this tweed] "Insert a new tweed in the store.")
  (reset-tweeds! [this] "Delete all tweeds from the store.")
  (seed-tweeds! [this] "Seed the store with a few tweeds."))

(defrecord AtomStore [^String name data])

(defrecord SQLiteStore [datasource])

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

(defn make-atom-store
  "Instantiate a store that holds some state in an atom."
  [name]
  ; https://guide.clojure.style/#record-constructors
  (->AtomStore name (atom {:tweeds '()})))

(defn make-db-store
  "Instantiate a store that holds the app's state in a SQLite database."
  [datasource]
  (->SQLiteStore datasource))
