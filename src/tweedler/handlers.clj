(ns tweedler.handlers
  "This namespace contains the handlers invoked when visiting the app routes."
  (:require [markdown.core :refer [md-to-html-string]]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :refer [redirect]]
            [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.store.protocol :refer [get-tweeds put-tweed! seed-tweeds!]]
            [tweedler.security :refer [escape-html]]))

;; This is just to make clj-kondo happy
;; https://stackoverflow.com/questions/61727582/how-to-avoid-unresolved-symbol-with-clj-kond-when-using-hugsql-def-db-fns-macro
(declare
 csrf-token
 href
 index-template
 not-found-template
 tweed
 tweed-template
 tweeds)

(html/deftemplate not-found-template "templates/404.html"
  [href]
  [:a.home-link] (html/set-attr :href href))

(html/defsnippet tweed-template "templates/index.html" [[:article.tweed html/first-of-type]]
  [tweed]
  [:.title] (html/html-content (:title tweed))
  [:.content] (html/html-content (md-to-html-string (:content tweed))))

(html/deftemplate index-template "templates/index.html"
  [tweeds csrf-token]
  [:section.tweeds] (html/content (map tweed-template tweeds))
  [:form.new-tweed] (html/set-attr :method "post" :action "/")
  [:input.csrf-token] (html/set-attr :value csrf-token)
  [:form.seed-tweeds] (html/set-attr :method "post" :action "/seed"))

(defn home-handler
  "Return the home page."
  [req]
  (index-template (get-tweeds (:store req)) (:anti-forgery-token req)))

(defn create-tweed
  "Extract `title` and `content` from the Ring request, add a new tweed in the
   Tweed store (mutation), then redirect to /."
  [req]
  (let [form-params (:form-params req)
        title (get form-params "title")
        content (get-in req [:form-params "content"])]
    (debug "create-tweed [title:" title "; content:" content "]")
    (put-tweed! (:store req) {:title (escape-html title) :content (escape-html content)})
    (debug "Redirect to /")
    (redirect "/" 302)))

(defn seed-tweeds
  "Seed the store with a few tweeds, then redirect to /."
  [{store :store}]
  (debug "seed-tweeds")
  (seed-tweeds! store)
  (debug "Redirect to /")
  (redirect "/" 302))

(defn not-found-handler
  "Return a 404 page."
  []
  (not-found-template (System/getenv "NOT_FOUND_PAGE_REDIRECT_URI")))

