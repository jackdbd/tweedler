(ns user
  (:require [portal.api :as p]
            [tweedler.security :refer [escape-html]]
            [tweedler.utils :refer [docstring gen-id]]))

(comment
  (def portal (p/open {:window-title "Portal UI"}))
  (add-tap #'p/submit)

  (tap> {:foo "bar"})
  (p/clear)

  (escape-html "<div><h1>Hello</h1><script>alert('XSS')</script></div>")

  (p/close portal)

  (docstring gen-id)
  (gen-id) 
  )