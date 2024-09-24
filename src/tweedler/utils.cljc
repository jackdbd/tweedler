(ns tweedler.utils
  "Miscellaneous utilities."
  (:require #?(:bb [cheshire.core :as json]
               :clj [nano-id.core :refer [nano-id]]))
  (:import #?(:bb (java.util UUID))))

#?(:bb  (defn gen-id
          "Generates a random ID with java.util.UUID."
          []
          (str (UUID/randomUUID)))
   :clj (defn gen-id
          "Generates a random ID with nano-id."
          []
          (nano-id)))

(defmacro docstring
  "Returns the doctring (if any) of a symbol."
  [symbol]
  `(:doc (meta #'~symbol)))

(comment
  (docstring gen-id)
  (gen-id)
  )