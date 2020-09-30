(ns tweedler.core
  "Tweedler - a simple app to start practicing Clojure."
  (:gen-class)
  (:require
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [tweedler.middleware :refer [wrap-store]]
   [tweedler.routes :refer [app-routes]]
   [tweedler.store :refer [make-redis-store-list]]))

(defn make-handler
  []
  (-> app-routes
      ;; (wrap-store (make-redis-store-hashes "tweed:"))
      (wrap-store (make-redis-store-list "tweeds"))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

(def app
  "The Ring main handler (i.e. the Ring application)."
  (make-handler))
