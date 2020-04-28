(ns tweedler.utils
  "This namespace contains some utility functions."
  (:require [clojure.string :refer [escape]]))

(defn escape-html
  "Sanitize the user's input to mitigate XSS attacks."
  [^String s]
  (escape s {\> "&gt;" \< "&lt;"}))