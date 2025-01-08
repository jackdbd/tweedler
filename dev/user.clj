(ns user
  "Tools for interactive development with the REPL.
   This file should not be included in a production build of the application."
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