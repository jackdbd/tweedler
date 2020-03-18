(ns tweedler.handlers
  (:require [tweedler.tweed :refer [->Tweed]]
            [tweedler.utils :refer [escape-html]]
            [tweedler.store :refer [store put-tweed!]]))

(defn handle-create-tweed [{{title "title" content "content"} :params}]
  (put-tweed! store (->Tweed (escape-html title) (escape-html content)))
  {:body "" :status 302 :headers {"Location" "/"}})
