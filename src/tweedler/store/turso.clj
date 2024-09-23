(ns tweedler.store.turso
  (:require [nano-id.core :refer [nano-id]]
            [cheshire.core :as json]
            [clj-http.client :as client]
            [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.store.protocol :refer [IStore get-tweeds put-tweed! reset-tweeds! seed-tweeds!]]))

(defn delete-tweed-body
  []
  (let [sql "DELETE FROM tweed"
        requests [{:type "execute" :stmt {:sql sql}}]]
    (json/generate-string {:requests requests})))

(defn delete-tweed-by-id-body
  [{:keys [id]}]
  (let [sql "DELETE FROM tweed WHERE id = :id"
        named_args [{:name "id" :value {:type "text" :value id}}]
        requests [{:type "execute" :stmt {:sql sql :named_args named_args}}]]
    (json/generate-string {:requests requests})))

(defn get-tweeds-body
  []
  (let [sql "SELECT * FROM tweed ORDER BY timestamp_creation DESC"
        requests [{:type "execute" :stmt {:sql sql}}]]
    (json/generate-string {:requests requests})))

(defn put-tweed-body
  [{:keys [id title content]}]
  (let [sql "INSERT INTO tweed (id, title, content) VALUES (:id, :title, :content)"
        named_args [{:name "id" :value {:type "text" :value id}}
                    {:name "title" :value {:type "text" :value title}}
                    {:name "content" :value {:type "text" :value content}}]
        requests [{:type "execute" :stmt {:sql sql :named_args named_args}}]]
    (json/generate-string {:requests requests})))

(defn seed-tweeds-body
  []
  (let [sql "INSERT INTO tweed (id, title, content) VALUES (:id, :title, :content)"
        named_args_one [{:name "id" :value {:type "text" :value (nano-id)}}
                        {:name "title" :value {:type "text" :value "Fake tweed one"}}
                        {:name "content" :value {:type "text" :value "This is the first fake tweed"}}]
        named_args_two [{:name "id" :value {:type "text" :value (nano-id)}}
                        {:name "title" :value {:type "text" :value "Fake tweed two"}}
                        {:name "content" :value {:type "text" :value "This is the second fake tweed"}}]
        requests [{:type "execute" :stmt {:sql sql :named_args named_args_one}}
                  {:type "execute" :stmt {:sql sql :named_args named_args_two}}]]
    (json/generate-string {:requests requests})))

(defn row->m [[id title content timestamp_creation]]
  {:id (:value id)
   :title (:value title)
   :content (:value content)
   :timestamp_creation (:value timestamp_creation)})

(def options {:content-type :json
              ;; https://stackoverflow.com/questions/7360520/connectiontimeout-versus-sockettimeout
              :socket-timeout 5000      ;; in milliseconds
              :connection-timeout 5000  ;; in milliseconds
              :accept :json})

(defn api-get-tweeds
  [{:keys [auth-token database-url]}]
  (debug "get-tweeds")
  (let [coerce-keys-to-keywords true
        url (str database-url "/v2/pipeline")
        resp (client/post url (merge options {:headers {"Authorization" (str "Bearer " auth-token)}
                                              :body (get-tweeds-body)}))
        rows (-> (json/parse-string (:body resp) coerce-keys-to-keywords)
                 :results
                 first
                 :response
                 :result
                 :rows)]
    (map row->m rows)))

(defn api-put-tweed!
  [{:keys [auth-token database-url]} {:keys [id title content]}]
  (debug "put-tweed!" {:id id :title title})
  (let [coerce-keys-to-keywords true
        url (str database-url "/v2/pipeline")
        resp (client/post url (merge options {:headers {"Authorization" (str "Bearer " auth-token)}
                                              :body (put-tweed-body {:id id :title title :content content})}))]
    (-> (json/parse-string (:body resp) coerce-keys-to-keywords)
        :results
        first
        :response
        :result)))

(defn api-reset-tweeds!
  [{:keys [auth-token database-url]}]
  (debug "reset-tweeds!")
  (let [coerce-keys-to-keywords true
        url (str database-url "/v2/pipeline")
        resp (client/post url (merge options {:headers {"Authorization" (str "Bearer " auth-token)}
                                              :body (delete-tweed-body)}))]
    (-> (json/parse-string (:body resp) coerce-keys-to-keywords)
        :results
        first
        :response
        :result)))

(defn api-seed-tweeds!
  [{:keys [auth-token database-url]}]
  (debug "seed-tweeds!")
  (let [coerce-keys-to-keywords true
        url (str database-url "/v2/pipeline")
        resp (client/post url (merge options {:headers {"Authorization" (str "Bearer " auth-token)}
                                              :body (seed-tweeds-body)}))]
    (-> (json/parse-string (:body resp) coerce-keys-to-keywords)
        :results
        first
        :response
        :result)))

(defrecord TursoStore [database-url auth-token]
  IStore

  (get-tweeds
    [{:keys [database-url auth-token]}]
    (api-get-tweeds {:database-url database-url :auth-token auth-token}))

  (put-tweed!
    [{:keys [database-url auth-token]} tweed]
    (api-put-tweed! {:database-url database-url :auth-token auth-token} (assoc tweed :id (nano-id))))

  (reset-tweeds!
   [{:keys [database-url auth-token]}]
   (api-reset-tweeds! {:database-url database-url :auth-token auth-token}))

  (seed-tweeds!
    [{:keys [database-url auth-token]}]
    (api-seed-tweeds! {:database-url database-url :auth-token auth-token})))

(defn turso-store
  [{:keys [database-url auth-token]}]
  (->TursoStore database-url auth-token))

(comment
  (def database-url (System/getenv "TURSO_DATABASE_URL"))
  (def url (str database-url "/v2/pipeline"))
  (def auth-token (System/getenv "TURSO_AUTH_TOKEN"))

  (api-reset-tweeds! {:database-url database-url :auth-token auth-token})
  (api-seed-tweeds! {:database-url database-url :auth-token auth-token})
  (api-get-tweeds {:database-url database-url :auth-token auth-token})

  (def id (nano-id))
  (api-put-tweed! {:database-url database-url
                   :auth-token auth-token}
                  {:id id
                   :title "Hi there!"
                   :content (str "This is tweed ID " id)})
  (api-get-tweeds {:database-url database-url :auth-token auth-token})

  (def store (turso-store {:database-url database-url :auth-token auth-token}))
  (get-tweeds store)
  (reset-tweeds! store)
  (get-tweeds store)
  (seed-tweeds! store)
  (get-tweeds store)
  (put-tweed! store {:title "Hello" :content "Another tweed"})
  (get-tweeds store)
  )