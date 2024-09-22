(ns tasks
  (:require [babashka.classpath :refer [get-classpath split-classpath]]))

(defn print-classpath
  []
  (println "=== CLASSPATH BEGIN ===")
  ;; all paths on a single line
  ;; (prn (split-classpath (get-classpath)))
  ;; each path on its own line
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(comment
  (print-classpath))