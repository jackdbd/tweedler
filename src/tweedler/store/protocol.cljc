(ns tweedler.store.protocol)

(defprotocol IStore
  "Interface representing a store that holds the application's state."

  (get-tweeds [this]
    "Retrieves all tweeds from the store.")
  
  (put-tweed! [this tweed]
    "Inserts a new tweed in the store.")
  
  (reset-tweeds! [this]
    "Deletes all tweeds from the store.")
  
  (seed-tweeds! [this]
    "Seeds the store with a few tweeds."))
