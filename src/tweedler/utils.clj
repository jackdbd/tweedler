(ns tweedler.utils
  (:require [clojure.string :refer [escape]]))

(defn escape-html [^String s]
  (escape s {\> "&gt;" \< "&lt;"}))