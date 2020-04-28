(ns tweedler.store
  "This namespace defines the application store and its methods."
  (:require [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.tweed :refer [->Tweed]]))

(defprotocol TweedStore
  (get-tweeds [store])

  (put-tweed! [store tweed])

  (reset-tweeds! [store]))

(defrecord AtomStore [data])

(extend-protocol TweedStore
  AtomStore

  (get-tweeds
   [store]
   (debug "get-tweeds")
   (get @(:data store) :tweeds))

  (put-tweed!
   [store tweed]
   (let [{:keys [title content]} tweed]
     (debug "put-tweed!" "[title:" title "; characters:" (count content) "]"))
   ;; We use conj so the newest Tweed shows up first.
   (swap! (:data store) update-in [:tweeds] conj tweed))

  (reset-tweeds!
   [store]
   (debug "reset-tweeds!")
   (swap! (:data store) assoc-in [:tweeds] [])))

(defn- init-store
  "Initialize a store."
  []
  (->AtomStore (atom {:tweeds '()})))

(defonce store (init-store))

(defn seed-store
  "Add some fakes to test the app."
  []
  (put-tweed! store (->Tweed "First tweed" "test content 1"))
  (put-tweed! store (->Tweed "Second tweed" "test content 2"))
  (put-tweed! store (->Tweed "Third tweed" "test content 3")))
