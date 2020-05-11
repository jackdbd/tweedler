(ns tweedler.middleware
  "This namespace contains the middlewares specific for the Tweedler app."
  (:require [tweedler.store :refer [make-atom-store]]))

(defn wrap-store
  "Add an instance of an atom-based store in the request map."
  [handler store]
  (fn [req]
    (handler (assoc req :store store))))

(comment
  (defn my-handler
    [req]
    {:body "<h1>hello</h1>" :status 200 :headers {"Content-Type" "text/html"}})

  (def my-ring-app (->
                    my-handler
                    (wrap-store (make-atom-store "name of my store")))))
