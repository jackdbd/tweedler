(ns tweedler.handlers
  (:require [taoensso.timbre :as timbre :refer [debug get-env info]]
            [tweedler.tweed :refer [->Tweed]]
            [tweedler.utils :refer [escape-html]]
            [tweedler.store :refer [store put-tweed!]]))

(defn handle-create-tweed [{{title "title" content "content"} :params}]
  (put-tweed! store (->Tweed (escape-html title) (escape-html content)))
  (info "handle-create-tweed [title:" title "; content:" content)
  (debug "Lexical env:" (get-env))
  (info "Redirect to /")
  {:body "" :status 302 :headers {"Location" "/"}})
