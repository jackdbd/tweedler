(ns tweedler.store
  "This namespace defines the application store and its methods."
  (:require [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.tweed :refer [->Tweed]]))

(defprotocol TweedStore
  "An abstraction of a store that holds the application's state."
  (get-tweeds [this] "Retrieve all tweeds from the store.")
  (put-tweed! [this tweed] "Insert a new tweed in the store.")
  (reset-tweeds! [this] "Delete all tweeds from the store.")
  (seed-tweeds! [this] "Seed the store with a few tweeds."))

(defrecord AtomStore [^String name data])

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
    (put-tweed! this (->Tweed "First tweed" "test content 1"))
    (put-tweed! this (->Tweed "Second tweed" "test content 2"))
    (put-tweed! this (->Tweed "Third tweed" "test content 3"))))

(defn make-store
  "Instantiate a store that holds some state in an atom."
  [name]
  ; https://guide.clojure.style/#record-constructors
  (->AtomStore name (atom {:tweeds '()})))
