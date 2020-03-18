(ns tweedler.core
  (:require [net.cgrand.enlive-html :as html]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :refer [wrap-params]]))

(defrecord Tweed [title content])

(defprotocol TweedStore
  (get-tweeds [store])
  (put-tweed! [store tweed]))

(defrecord AtomStore [data])

(extend-protocol TweedStore
  AtomStore
  (get-tweeds [store]
    "Get all Tweeds."
    (get @(:data store) :tweeds))
  (put-tweed! [store tweed]
    "Insert a new Tweed. We use conj so the newest Tweed shows up first."
    (swap! (:data store)
           update-in [:tweeds] conj tweed)))

(def store (->AtomStore (atom {:tweeds '()})))

;; (get-tweeds store)

;; (put-tweed! store (->Tweed "Test title" "test content"))

(html/defsnippet tweed-template "templates/index.html" [[:article.tweed html/first-of-type]]
  [tweed]
  [:.title] (html/html-content (:title tweed))
  [:.content] (html/html-content (:content tweed)))
  
(html/deftemplate index-template "templates/index.html"
  [tweeds]
  [:section.tweeds] (html/content (map tweed-template tweeds))
  [:form] (html/set-attr :method "post" :action "/"))

(defn handle-create-tweed [{{title "title" content "content"} :params}]
  (put-tweed! store (->Tweed title content))
  {:body "" :status 302 :headers {"Location" "/"}})
  
(defroutes app-routes
  (GET "/" [] (index-template (get-tweeds store)))
  (POST "/" request (handle-create-tweed request))
  (resources "/css" {:root "/css"})
  (resources "/img" {:root "/img"}))

(def app (-> app-routes
             (wrap-params)))

;; Use #'app (which stands for (var app)) to avoid stopping/restarting the jetty
;; webserver. We still have to refresh the browser though).
(def server (jetty/run-jetty #'app {:port 3000 :join? false}))
