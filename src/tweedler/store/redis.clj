(ns tweedler.store.redis
  "Store the application state in Redis."
  (:require [taoensso.carmine :as car]
            [taoensso.timbre :as timbre :refer [debug]]
            [tweedler.store.protocol :refer [IStore get-tweeds put-tweed!]]
            [tweedler.utils :refer [gen-id]]))

; (defn- redis-spec
;   "Get the Redis :spec for the current environment.
;    On Heroku the `REDISTOGO_URL` environment variable is set."
;   []
;   (if (env :redistogo-url)
;     {:uri (env :redistogo-url)}
;     {:db 1 :host (env :redis-host) :port (Integer/valueOf ^String (env :redis-port))}))

; TODO: fix redis-spec
; (def server1-conn {:pool {} :spec (redis-spec)})
(def server1-conn {:pool {} :spec {}})

(defmacro wcar*
  "Connection pool for Redis."
  [& body]
  `(car/wcar server1-conn ~@body))

(defrecord RedisStoreList [redis-key])

(defrecord RedisStoreHashes [redis-key-prefix])

(defn- redis-scan
  [cursor pattern]
  (wcar* (car/scan cursor "MATCH" pattern)))

; TODO: I think the problem is in redis-spec. What to do if environment
; variables are not set?
; (defn- redis-scan
;   [cursor pattern]
;   (prn "=== cursor, pattern ===" cursor pattern))

(defn- new-tweed-key
  [prefix]
  (format "%s%s" prefix (gen-id)))

(defn- redis-keys-by-pattern
  [pattern]
  (loop [[cursor elements] (redis-scan "0" pattern)
         tweeds '()]
    (if (= "0" cursor)
      (concat tweeds elements) ;; or (flatten (conj tweeds (into () elements)))
      (recur (redis-scan cursor pattern)
             (concat tweeds elements)))))

(defn tweed-from-key
  [k]
  (let [[title content timestamp] (wcar* (car/hvals k))]
    {:id k :title title :content content :timestamp timestamp}))

; (defn tweed-from-key
;   [k]
;   {:id k :title "TODO title" :content "TODO content" :timestamp "TODO timestamp"})

(extend-protocol IStore

  RedisStoreHashes
  (get-tweeds
    [this]
    (debug "get-tweeds")
    (let [pattern (format "%s*" (:redis-key-prefix this))
          tweed-keys (redis-keys-by-pattern pattern)
          tweeds (map tweed-from-key tweed-keys)]
     ;; newer tweeds go first
      (sort-by :timestamp #(compare %2 %1) tweeds)))
  (put-tweed!
    [this tweed]
    (let [k (new-tweed-key (:redis-key-prefix this))
          {:keys [title content]} tweed
          [sec ms] (wcar* (car/time))]
      (debug "put-tweed!" (format "[key=%s (title=%s characters=%s)]" k title (count content)))
      (debug "unix timestamp" (format "[seconds=%s elapsed-ms=%s]" sec ms))
      (wcar* (car/hmset* k {:title title :content content :timestamp sec}))))
  (reset-tweeds!
    [this]
    (debug "reset-tweeds!")
    (let [pattern (format "%s*" (:redis-key-prefix this))
          script "return redis.call('del', 'defaultKey', unpack(redis.call('keys', _:pattern)))"]
     ;; We could do this, but it would not atomic
      (comment
        (doseq [k (redis-keys-by-pattern pattern)]
          (wcar* (car/del k))))
     ;; so we use a Lua script
      (wcar* (car/lua script {:pattern pattern} {}))))
  (seed-tweeds!
    [this]
    (debug "seed-tweeds!")
    (doseq [_ (range 3)]
      (let [k (new-tweed-key (:redis-key-prefix this))
            title (format "Fake Title %.3f" (Math/random))
            content "Fake content here..."
            [sec _] (wcar* (car/time))
            m {:title title :content content :timestamp sec}]
        (wcar* (car/hmset* k m)))))

  RedisStoreList
  (get-tweeds
    [this]
    (debug "get-tweeds")
    (wcar* (car/lrange (:redis-key this) 0 -1)))
  (put-tweed!
    [this tweed]
    (let [k (new-tweed-key (:key-prefix this))
          {:keys [title content]} tweed]
      (debug "put-tweed!" (format "[key=%s (title=%s characters=%s)]" k title (count content)))
      (wcar* (car/lpush (:redis-key this) tweed))))
  (reset-tweeds!
    [this]
    (debug "reset-tweeds!")
    (wcar* (car/del (:redis-key this))))
  (seed-tweeds!
    [this]
    (debug "seed-tweeds!")
    (doseq [_ (range 3)]
      (let [m {:title (format "Fake Title %.3f" (Math/random)) :content "Fake content here"}]
        (wcar* (car/lpush (:redis-key this) m))))))

;; (defn make-atom-store
;;   "Instantiate a store that holds some state in an atom."
;;   [name]
;;   ; https://guide.clojure.style/#record-constructors
;;   (->AtomStore name (atom {:tweeds '()})))

;; (defn make-db-store
;;   "Instantiate a store that holds the app's state in a SQLite database."
;;   [datasource]
;;   (->SQLiteStore datasource))

(defn redis-store-hashes
  "Stores the application state in multiple Redis hashes.
   All Redis hashes created by this store will share the same prefix
   `redis-key-prefix` (e.g. my-prefix:, my_prefix_)."
  [{:keys [redis-key-prefix]}]
  (->RedisStoreHashes redis-key-prefix))

(defn redis-store-list
  "Stores the application state in a single Redis list key `redis-key`."
  [{:keys [redis-key]}]
  (->RedisStoreList redis-key))

(comment 
  (def store (redis-store-hashes {:redis-key-prefix "tweed:"}))
  (get-tweeds store)
  (put-tweed! store {:title "Hello" :content "This is my first tweed"})
  (put-tweed! store {:title "Bye" :content "This is my last tweed"})
  (get-tweeds store)

  (def store (redis-store-list {:redis-key "tweeds"}))
  (get-tweeds store)
  (put-tweed! store {:title "Hello" :content "This is my first tweed"})
  (put-tweed! store {:title "Bye" :content "This is my last tweed"})
  (get-tweeds store)
  )