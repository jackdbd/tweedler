(ns tweedler.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]
            [tweedler.routes :refer [app-routes]]))

(def app (-> app-routes
             (wrap-params)))

;; Use #'app (which stands for (var app)) to avoid stopping/restarting the jetty
;; webserver. We still have to refresh the browser though).
(def server (jetty/run-jetty #'app {:port 3000 :join? false}))
