(ns tweedler.core
  "Tweedler - a simple app to start practicing Clojure."
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.middleware.params :refer [wrap-params]]
   [taoensso.timbre :as timbre :refer [info]]
   [tweedler.routes :refer [app-routes]])
  (:import [org.eclipse.jetty.server Server])
  ; Tell the clojure compiler to generate a class for this namespace using the
  ; -main method as entry point.
  (:gen-class))

(def handler
  "The Ring main handler (i.e. the Ring application)."
  (-> app-routes
      wrap-params
      wrap-cookies))

;; Define a singleton that acts as the container for a Jetty server (note that
;; the Jetty Server is stateful: it can be started/stopped).
(defonce ^:private webserver (atom nil))

;; Use defn- so this function can be called only in this namespace.
(defn- run-server
  "Create a Jetty `Server`, run it, and store it in an atom.

  We use #'handler (which stands for (var handler)) to avoid stopping/restarting
  the jetty webserver every time we make changes to the Ring handler. We still
  have to refresh the browser though).
  We use `:join? false` to avoid blocking the thread while running the server
  (this is useful when running the application in the REPL)."
  [port]
  (info "Create and run Jetty server, listening on port" port)
  (reset! webserver (jetty/run-jetty #'handler {:join? false :port port}))

  (defn- stop-server
    "Stop the Jetty `Server` held in the atom."
    []
    (info "Stop Jetty server")
    ;; deref the atom to get the Jetty Server, then use Java interop to stop it.
    (.stop @webserver)
    ;; Probably resetting the atom is not really necessary...
    (reset! webserver nil)))

(defn run-dev []
  (run-server 3000))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (let [port 3000]
    (run-server port)))
