(ns tweedler.store.atom
  "Store the application state in an atom."
  (:require [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.store.protocol :refer [IStore get-tweeds put-tweed!]]))

(defrecord AtomStore [^String name atom]
  IStore

  (get-tweeds
   [this]
   (debug "get-tweeds")
   (get @(:atom this) :tweeds))
  
  (put-tweed!
   [this tweed]
   (let [{:keys [title content]} tweed]
     (debug "put-tweed!" "[title:" title "; characters:" (count content) "]")
     ;; We use conj so the newest Tweed shows up first.
     (swap! (:atom this) update-in [:tweeds] conj tweed)))
  
  (reset-tweeds!
   [this]
   (debug "reset-tweeds!")
   (swap! (:atom this) assoc-in [:tweeds] []))
  
  (seed-tweeds!
   [this]
   (debug "seed-tweeds!")
   (put-tweed! this {:title "First tweed" :content "test content 1"})
   (put-tweed! this {:title "Second tweed" :content "test content 2"})
   (put-tweed! this {:title "Third tweed" :content "test content 3"})))

(defn atom-store
  ([]
   (atom-store {:name "Tweedler store" :atom (atom {:tweeds '()})}))
  ([{:keys [name atom]}]
   (->AtomStore name atom)))

(comment
  (def store (atom-store))
  (get-tweeds store)
  (put-tweed! store {:title "Hello" :content "This is my first tweed"})
  (put-tweed! store {:title "Bye" :content "This is my last tweed"})
  (get-tweeds store)

  (def store (atom-store {:name "My Store" :atom (atom {:tweeds '()})}))
  (get-tweeds store)
  )
