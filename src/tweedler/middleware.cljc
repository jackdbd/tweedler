(ns tweedler.middleware
  "This namespace contains the middlewares specific for the Tweedler app.")

(defn wrap-store
  "Add an instance of an atom-based store in the request map."
  [handler store]
  (fn [req]
    (handler (assoc req :store store))))
