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
  (route/resources "/css" {:root "/css"})
  (route/resources "/js" {:root "/js"})
  (route/resources "/img" {:root "/img"})
  ;; The not-found route MUST be the last one
  (route/not-found (h/not-found-handler)))
