# === STAGE 1 ================================================================ #
# Build a standalone .jar file with Leiningen
# ============================================================================ #
FROM clojure:latest AS CLOJURE_BUILD

LABEL maintainer="jackdebidda@gmail.com"

ENV APP_DIR=/usr/src/app \
    JAR_FILE=tweedler-standalone.jar

# RUN apt-get -y update
# RUN apt-get -y install tree

RUN mkdir -p ${APP_DIR}
# The WORKDIR instruction sets the working directory for any RUN, CMD,
# ENTRYPOINT, COPY and ADD instructions that follow it in the Dockerfile.
WORKDIR ${APP_DIR}

COPY project.clj ${APP_DIR}/
COPY resources ${APP_DIR}/resources
COPY src ${APP_DIR}/src

# The Clojure project.clj includes the lein-ring plugin, so we can use this
# command to build the .jar
# https://github.com/weavejester/lein-ring
RUN lein ring uberjar
# or simply use lein uberjar as usual
# RUN lein uberjar

# === STAGE 2 ================================================================ #
# Copy the .jar built at stage 1 and execute it
# ============================================================================ #

FROM openjdk:8-jre-alpine3.9

ENV APP_DIR=/usr/src/app \
    JAR_FILE=tweedler-standalone.jar \
    PORT=3000 \
    JVM_OPTS=-Dclojure.main.report=stderr

# RUN apk add tree

RUN mkdir -p ${APP_DIR}
WORKDIR ${APP_DIR}

# RUN tree -L 3
COPY --from=CLOJURE_BUILD ${APP_DIR}/target/uberjar/${JAR_FILE} ${APP_DIR}/${JAR_FILE}

EXPOSE ${PORT}

# Check every 5 minutes that a web-server is able to serve the site's main page
# within 3 seconds.
# https://docs.docker.com/engine/reference/builder/#healthcheck
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl --fail https://localhost/ || exit 1
 
CMD java -jar ${JVM_OPTS} ${APP_DIR}/${JAR_FILE}