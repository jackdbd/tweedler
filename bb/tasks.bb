(ns tasks
  (:require [babashka.classpath :refer [get-classpath split-classpath]]
            [babashka.curl :as curl]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre :refer [debug]]
            ;; [tweedler.store.turso :refer [api-get-tweeds api-seed-tweeds!]]
            [tweedler.utils :refer [gen-id]]))
  
(defn print-classpath
  []
  (println "=== CLASSPATH BEGIN ===")
  ;; all paths on a single line
  ;; (prn (split-classpath (get-classpath)))
  ;; each path on its own line
  (doseq [path (set (split-classpath (get-classpath)))]
    (println path))
  (println "=== CLASSPATH END ==="))

(defn migrate-turso
  [{:keys [database-token database-url]}]
  (debug "Migrate Turso DB" database-url)
  (let [filepath "json/turso/migrations.json"
        coerce-keys-to-keywords true
        requests (-> (io/resource filepath)
                     slurp
                     (json/parse-string coerce-keys-to-keywords)
                     :requests)
        url (str database-url "/v2/pipeline")
        body (json/generate-string {:requests requests})
        resp (curl/post url {:headers {"Authorization" (str "Bearer " database-token)
                                       "Content-Type" "application/json"}
                             :body body
                             :throw false})
        resp-body (json/parse-string (:body resp) coerce-keys-to-keywords)]
    (prn (:results resp-body))))

(defn- delete-tweed-body
  []
  (let [sql "DELETE FROM tweed"
        requests [{:type "execute" :stmt {:sql sql}}]]
    (json/generate-string {:requests requests})))

(defn- delete-tweed-by-id-body
  [{:keys [id]}]
  (let [sql "DELETE FROM tweed WHERE id = :id"
        named_args [{:name "id" :value {:type "text" :value id}}]
        requests [{:type "execute" :stmt {:sql sql :named_args named_args}}]]
    (json/generate-string {:requests requests})))

(defn- get-tweeds-body
  []
  (let [sql "SELECT * FROM tweed ORDER BY timestamp_creation DESC"
        requests [{:type "execute" :stmt {:sql sql}}]]
    (json/generate-string {:requests requests})))

(defn- put-tweed-body
  [{:keys [id title content]}]
  (let [sql "INSERT INTO tweed (id, title, content) VALUES (:id, :title, :content)"
        named_args [{:name "id" :value {:type "text" :value id}}
                    {:name "title" :value {:type "text" :value title}}
                    {:name "content" :value {:type "text" :value content}}]
        requests [{:type "execute" :stmt {:sql sql :named_args named_args}}]]
    (json/generate-string {:requests requests})))

(defn- seed-tweeds-body
  []
  (let [sql "INSERT INTO tweed (id, title, content) VALUES (:id, :title, :content)"
        named_args_one [{:name "id" :value {:type "text" :value (gen-id)}}
                        {:name "title" :value {:type "text" :value "Fake tweed one"}}
                        {:name "content" :value {:type "text" :value "This is the first fake tweed"}}]
        named_args_two [{:name "id" :value {:type "text" :value (gen-id)}}
                        {:name "title" :value {:type "text" :value "Fake tweed two"}}
                        {:name "content" :value {:type "text" :value "This is the second fake tweed"}}]
        requests [{:type "execute" :stmt {:sql sql :named_args named_args_one}}
                  {:type "execute" :stmt {:sql sql :named_args named_args_two}}]]
    (json/generate-string {:requests requests})))

(defn reset-turso
  [{:keys [database-token database-url]}]
  (debug "Reset tweeds in Turso DB" database-url)
  (let [coerce-keys-to-keywords true
        url (str database-url "/v2/pipeline")
        body (delete-tweed-body)
        resp (curl/post url {:headers {"Authorization" (str "Bearer " database-token)
                                       "Content-Type" "application/json"}
                             :throw false
                             :body body})
        resp-body (json/parse-string (:body resp) coerce-keys-to-keywords)]
    (prn (:results resp-body))))

(defn seed-turso
  [{:keys [database-token database-url]}]
  (debug "Seed Turso DB" database-url)
  (let [coerce-keys-to-keywords true
        url (str database-url "/v2/pipeline")
        body (seed-tweeds-body)
        resp (curl/post url {:headers {"Authorization" (str "Bearer " database-token)
                                       "Content-Type" "application/json"}
                             :throw false
                             :body body})
        resp-body (json/parse-string (:body resp) coerce-keys-to-keywords)]
    (prn (:results resp-body))))

(comment
  (print-classpath)
  (def database-url (System/getenv "TURSO_DATABASE_URL"))
  (def database-token (System/getenv "TURSO_DATABASE_TOKEN"))

  (migrate-turso {:database-token database-token :database-url database-url}) 
  (reset-turso {:database-token database-token :database-url database-url})
  (seed-turso {:database-token database-token :database-url database-url})

  (def url (str database-url "/v2/pipeline"))
  (def coerce-keys-to-keywords true)

  (def id (gen-id))
  (def body (put-tweed-body {:id id
                             :title "Hello World!"
                             :content "This is my first tweed"}))
  (def body (get-tweeds-body))
  (def body (delete-tweed-body))
  (def body (delete-tweed-by-id-body {:id id}))

  (def resp (curl/post url {:headers {"Authorization" (str "Bearer " (System/getenv "TURSO_DATABASE_TOKEN"))
                                      "Content-Type" "application/json"}
                            :throw false
                            :body body}))

  (json/parse-string (:body resp) coerce-keys-to-keywords)
  )