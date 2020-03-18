(ns tweedler.routes
  (:require [net.cgrand.enlive-html :as html]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources]]
            [markdown.core :refer [md-to-html-string]]
            [tweedler.handlers :refer [handle-create-tweed]]
            [tweedler.store :refer [store get-tweeds]]))

(html/defsnippet tweed-template "templates/index.html" [[:article.tweed html/first-of-type]]
  [tweed]
  [:.title] (html/html-content (:title tweed))
  [:.content] (html/html-content (md-to-html-string (:content tweed))))

(html/deftemplate index-template "templates/index.html"
  [tweeds]
  [:section.tweeds] (html/content (map tweed-template tweeds))
  [:form] (html/set-attr :method "post" :action "/"))

(defroutes app-routes
  (GET "/" [] (index-template (get-tweeds store)))
  (POST "/" request (handle-create-tweed request))
  (resources "/css" {:root "/css"})
  (resources "/img" {:root "/img"}))