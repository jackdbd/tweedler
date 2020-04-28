(ns tweedler.handlers
  "This namespace contains the handlers invoked when visiting the app routes."
  (:require [ring.util.response :refer [redirect]]
            [taoensso.timbre :as timbre :refer [debug get-env info]]
            [tweedler.store :refer [put-tweed! store]]
            [tweedler.tweed :refer [->Tweed]]
            [tweedler.utils :refer [escape-html]]))

(defn handle-create-tweed
  "Extract `title` and `content` from the Ring request `:params`, add a new
  tweed in the Tweed store by mutating it, then return a Ring response."
  [{{title "title" content "content"} :params}]
  (info "handle-create-tweed [title:" title "; content:" content)
  (put-tweed! store (->Tweed (escape-html title) (escape-html content)))
  (debug "Lexical env:" (get-env))
  (info "Redirect to /")
  (redirect "/" 302))
