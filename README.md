# tweedler

A simple app just to start practicing Clojure.

## Development

This project uses [Leiningen 2.0](https://github.com/technomancy/leiningen).

Code formatting with [cljfmt](https://github.com/weavejester/cljfmt) and [lein-cljfmt](https://clojars.org/lein-cljfmt) Leiningen plugin.

```sh
lein cljfmt check
lein cljfmt fix
```

Linting with [eastwood](https://github.com/jonase/eastwood).

```sh
lein eastwood
```

Run either with:

```sh
lein run
```

or with [Lein-Ring](https://github.com/weavejester/lein-ring) Leiningen plugin:

```sh
lein ring server
```

Otherwise, run it in the REPL, so you can start and stop the server:

```sh
lein repl

# In the REPL
tweedler.core=> (run-server 3000)
tweedler.core=> (stop-server)
```

## Test

Run all tests in "watch mode" with [lein-test-refresh](https://github.com/jakemcc/lein-test-refresh):

```sh
lein test-refresh
```

To check test coverage, I use [cloverage](https://github.com/cloverage/cloverage):

```sh
lein cloverage
```

## Build

```sh
lein uberjar
java -jar target/uberjar/tweedler-standalone.jar
```
