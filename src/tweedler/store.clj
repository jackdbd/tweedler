(ns tweedler.store
  "This namespace defines the application store and its methods."
  (:require
   [environ.core :refer [env]]
   [nano-id.core :refer [nano-id]]
   [taoensso.carmine :as car]
   [taoensso.timbre :as timbre :refer [debug]]
   [tweedler.db-fns :as db-fns]))

(defn- redis-spec
  "Get the Redis :spec for the current environment.
   On Heroku the `REDISTOGO_URL` environment variable is set."
  []
  (if (env :redistogo-url)
    {:uri (env :redistogo-url)}
    {:db 1 :host (env :redis-host) :port (Integer/valueOf ^String (env :redis-port))}))

(def server1-conn {:pool {} :spec (redis-spec)})

(defmacro wcar*
  "Connection pool for Redis."
  [& body]
  `(car/wcar server1-conn ~@body))

(defprotocol TweedStore
  "An abstraction of a store that holds the application's state."
  (get-tweeds [this] "Retrieve all tweeds from the store.")
  (put-tweed! [this tweed] "Insert a new tweed in the store.")
  (reset-tweeds! [this] "Delete all tweeds from the store.")
  (seed-tweeds! [this] "Seed the store with a few tweeds."))

(defrecord AtomStore [^String name data])

(defrecord SQLiteStore [datasource])

(defrecord RedisStoreList [redis-key])

(defrecord RedisStoreHashes [redis-key-prefix])

(defn- redis-scan
  [cursor pattern]
  (wcar* (car/scan cursor "MATCH" pattern)))

(defn- new-tweed-key
  [prefix]
  (format "%s%s" prefix (nano-id)))

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

(extend-protocol TweedStore

  AtomStore
  (get-tweeds
    [this]
    (debug "get-tweeds")
    (get @(:data this) :tweeds))
  (put-tweed!
    [this tweed]
    (let [{:keys [title content]} tweed]
      (debug "put-tweed!" "[title:" title "; characters:" (count content) "]")
      ;; We use conj so the newest Tweed shows up first.
      (swap! (:data this) update-in [:tweeds] conj tweed)))
  (reset-tweeds!
    [this]
    (debug "reset-tweeds!")
    (swap! (:data this) assoc-in [:tweeds] []))
  (seed-tweeds!
    [this]
    (debug "seed-tweeds!")
    (put-tweed! this {:title "First tweed" :content "test content 1"})
    (put-tweed! this {:title "Second tweed" :content "test content 2"})
    (put-tweed! this {:title "Third tweed" :content "test content 3"}))

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
        (wcar* (car/lpush (:redis-key this) m)))))

  SQLiteStore
  (get-tweeds
    [this]
    (debug "get-tweeds")
    (db-fns/get-tweeds (:datasource this)))
  (put-tweed!
    [this tweed]
    (let [{:keys [title content]} tweed]
      (debug "put-tweed!" title content)
      (db-fns/put-tweed! (:datasource this) {:id (nano-id) :title title :content content})))
  (reset-tweeds!
    [this]
    (debug "reset-tweeds!")
    (db-fns/delete-tweed! (:datasource this)))
  (seed-tweeds!
    [this]
    (debug "seed-tweeds!")
    (def fake-tweeds [[(nano-id) "Fake title 0" "Fake content 0"]
                      [(nano-id) "Fake title 1" "Fake content 1"]])
    (db-fns/seed-tweed! (:datasource this) {:fakes fake-tweeds})))

(defn make-atom-store
  "Instantiate a store that holds some state in an atom."
  [name]
  ; https://guide.clojure.style/#record-constructors
  (->AtomStore name (atom {:tweeds '()})))

(defn make-db-store
  "Instantiate a store that holds the app's state in a SQLite database."
  [datasource]
  (->SQLiteStore datasource))

(defn make-redis-store-hashes
  "Store the app's state in multiple Redis hashes.
   All Redis hashes created by this store will share the same prefix
   `redis-key-prefix` (e.g. my-prefix:, my_prefix_)."
  [redis-key-prefix]
  (->RedisStoreHashes redis-key-prefix))

(defn make-redis-store-list
  "Store the app's state in a single Redis list key `redis-key`."
  [redis-key]
  (->RedisStoreList redis-key))
