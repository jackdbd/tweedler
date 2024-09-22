(ns user
  (:require
   [portal.api :as p]
   [tweedler.utils :refer [escape-html]]))

(comment
  (def portal (p/open {:window-title "Portal UI"}))
  (add-tap #'p/submit)

  (tap> {:foo "bar"})
  (p/clear)

  (escape-html "<script>alert('XSS')</script>")

  (p/close portal)
  )