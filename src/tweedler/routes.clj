(ns tweedler.routes
  "This namespace maps the application routes to the request handlers."
  (:require [compojure.core :refer [defroutes ANY GET POST]]
            [compojure.route :as route :refer [resources]]
            [tweedler.handlers :as h]))

(defroutes app-routes
  (GET "/" _ (h/home-handler))
  (POST "/" req (h/create-tweed req))
  (POST "/seed" _ (h/seed-tweeds))
  (ANY "*" _ (h/not-found-handler))
  (resources "/css" {:root "/css"})
  (resources "/img" {:root "/img"}))
