(ns tweedler.store
  (:require [tweedler.tweed :refer [->Tweed]]))

(defprotocol TweedStore
  (get-tweeds [store])
  (put-tweed! [store tweed]))

(defrecord AtomStore [data])

(extend-protocol TweedStore
  AtomStore
  (get-tweeds [store]
    (get @(:data store) :tweeds))
  (put-tweed! [store tweed]
              "We use conj so the newest Tweed shows up first."
              (swap! (:data store)
                     update-in [:tweeds] conj tweed)))

(def store (->AtomStore (atom {:tweeds '()})))

;; Add some tweeds just for testing the app
(put-tweed! store (->Tweed "First tweed" "test content 1"))
(put-tweed! store (->Tweed "Second tweed" "test content 2"))
(put-tweed! store (->Tweed "Third tweed" "test content 3"))