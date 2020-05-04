(ns tweedler.routes
  "This namespace maps the application routes to the request handlers."
  (:require [compojure.core :refer [context defroutes GET POST]]
            [compojure.route :as route]
            [tweedler.handlers :as h]))

(defroutes app-routes
  (GET "/" req (h/home-handler req))
  (POST "/" req (h/create-tweed req))
  (POST "/seed" req (h/seed-tweeds req))
  (context "/admin" []
    (GET "/login" [] "Logging in")
    (GET "/logout" [] "Logging out"))
  (route/not-found (h/not-found-handler))
  (route/resources "/css" {:root "/css"})
  (route/resources "/img" {:root "/img"}))
