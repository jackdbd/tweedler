{:store :database
 :db {:jdbcUrl (get (System/getenv) "JDBC_DATABASE_URL")}
 :migration-dir "migrations"}