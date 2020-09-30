# tweedler

[![Build Status](https://travis-ci.com/jackdbd/tweedler.svg?branch=master)](https://travis-ci.org/jackdbd/tweedler)

A simple app just to start practicing Clojure.

![Dependency hierarchy graph](https://raw.githubusercontent.com/jackdbd/tweedler/master/images/ns-hierarchy.png "Dependency hierarchy graph generated with lein-hiera")

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
tweedler.core=> (start-server 3000)
tweedler.core=> (stop-server)
```

## Test

Run all tests in "watch mode" with [lein-test-refresh](https://github.com/jakemcc/lein-test-refresh):

```sh
lein test-refresh
```

## Build

Create a standalone, compiled ahead-of-time `.jar` file with:

```sh
lein uberjar
# or
lein ring uberjar
```

then run it with:

```sh
java -jar target/uberjar/tweedler-standalone.jar
# or
lein ring server-headless
```

## Dockerized app

Build the Docker image and give it a name and a version tag:

```shell
docker build -t tweedler:v0.1.0 .
```

Run the Docker container:

```shell
docker run -p 3001:3000 tweedler:v0.1.0
```

Deploy the dockerized app on CapRover (running on my DigitalOcean Droplet):

```shell
./deploy.sh
```

## Other

The dependency hierarchy graph was generated with the Leiningen plugin [lein-hiera](https://github.com/greglook/lein-hiera). If you want to recreate it, run the following command (please note that you will need both lein-hiera and [Graphviz](https://graphviz.org/) installed).

```sh
lein hiera
```
