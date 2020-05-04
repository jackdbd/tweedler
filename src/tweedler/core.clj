(ns tweedler.core
  "Tweedler - a simple app to start practicing Clojure."
  (:gen-class)
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [taoensso.timbre :as timbre :refer [info]]
   [tweedler.middleware :refer [wrap-store]]
   [tweedler.routes :refer [app-routes]]
   [tweedler.store :refer [make-store]]))

(def handler
  "The Ring main handler (i.e. the Ring application)."
  (-> app-routes
      (wrap-store (make-store "app-store"))
      ;; TODO: re-enable :anti-forgery to look for CSRF token in
      ;;       POST/PUT/DELETE requests.
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

;; Define a singleton that acts as the container for a Jetty server (note that
;; the Jetty Server is stateful: it can be started/stopped).
(defonce ^:private webserver (atom nil))

(defn start-server
  "Create a Jetty `Server`, run it, and store it in an atom.
  We use #'handler (which stands for (var handler)) to avoid stopping/restarting
  the jetty webserver every time we make changes to the Ring handler. We still
  have to refresh the browser though).
  We use `:join? false` to avoid blocking the thread while running the server
  (this is useful when running the application in the REPL)."
  [port]
  (info "Create and run Jetty server, listening on port" port)
  (reset! webserver (jetty/run-jetty #'handler {:join? false :port port})))

(defn stop-server
  "Stop the Jetty `Server` held in the atom."
  []
  (info "Stop Jetty server")
  ;; deref the atom to get the Jetty Server, then use Java interop to stop it.
  (.stop @webserver))

(defn -main
  "The entry-point for 'lein run'"
  []
  (start-server 3000))
