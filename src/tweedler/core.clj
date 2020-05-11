(ns tweedler.core
  "Tweedler - a simple app to start practicing Clojure."
  (:gen-class)
  (:require
   [environ.core :refer [env]]
   [hikari-cp.core :refer [close-datasource make-datasource]]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [taoensso.timbre :as timbre :refer [info]]
   [tweedler.middleware :refer [wrap-store]]
   [tweedler.routes :refer [app-routes]]
   [tweedler.store :refer [make-atom-store make-db-store]]))

(def ^:dynamic *datasource*
  (make-datasource {:jdbc-url (env :database-url)}))

(def handler
  "The Ring main handler (i.e. the Ring application)."
  (-> app-routes
      (wrap-store (make-db-store {:datasource *datasource*}))
      ;; (wrap-store (make-atom-store "app-store"))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

(defn make-handler
  [ds]
  (-> app-routes
      (wrap-store (make-db-store {:datasource ds}))
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] true))))

;; Define a singleton that acts as the container for a Jetty server (note that
;; the Jetty Server is stateful: it can be started/stopped).
(defonce ^:private webserver (atom nil))

;; TODO: what is the best way to re-bind a HikariCP datasource and redefine the
;; ring handler? I could redefine the *datasource* symbol in start-server
;; (instead of simply use binding), and that would allow me to avoid having a
;; function to re-create the ring handler. But it doesn't look right to me. Is
;; there a better way?
;; (def ^:dynamic *datasource* (make-datasource {:jdbc-url (env :database-url)}))

(defn start-server
  "Create a Jetty `Server`, run it, and store it in an atom.
  We use #'handler (which stands for (var handler)) to avoid stopping/restarting
  the jetty webserver every time we make changes to the Ring handler. We still
  have to refresh the browser though).
  We use `:join? false` to avoid blocking the thread while running the server
  (this is useful when running the application in the REPL)."
  [port]
  (info "Re-bind datasource (HikariCP connection pool)")
  (binding [*datasource* (make-datasource {:jdbc-url (env :database-url)})]
    (info "Re-create ring handler")
    (info "Create and run Jetty server, listening on port" port)
    (reset! webserver (jetty/run-jetty (make-handler *datasource*) {:join? false :port port}))))

(defn stop-server
  "Stop the Jetty `Server` held in the atom."
  []
  (info "Close datasource (HikariCP connection pool)")
  (close-datasource *datasource*)
  (info "Stop Jetty server")
  ;; deref the atom to get the Jetty Server, then use Java interop to stop it.
  (.stop @webserver))

(defn -main
  "The entry-point for 'lein run'
   GOTCHA: avoid hardcoding a port number when deploying on Heroku.
   https://stackoverflow.com/a/15693371/3036129"
  [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (start-server port)))
