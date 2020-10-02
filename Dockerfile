# === STAGE 1 ================================================================ #
# Build a standalone .jar file with Leiningen
# ============================================================================ #
FROM clojure:latest AS builder

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

# I think we can safely run the generated .jar on a JVM which runs on a
# different Linux distro. For this second stage we use Alpine Linux instead of
# clojure:latest (which is based on Debian). This way we save a few MB on
# the Docker image.
FROM openjdk:8-jre-alpine3.9

ENV SRC_DIR=/usr/src/app \
    USER_HOME=/home/appuser \
    JAR_FILE=tweedler-standalone.jar \
    PORT=3000 \
    JVM_OPTS=-Dclojure.main.report=stderr

# RUN apk add tree

# Create a group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

USER appuser

WORKDIR ${USER_HOME}

COPY --from=builder ${SRC_DIR}/target/uberjar/${JAR_FILE} ${USER_HOME}/${JAR_FILE}
# RUN tree -L 3

EXPOSE ${PORT}

# Check every x minutes that a web-server is able to serve the site's main page
# within y seconds.
# https://docs.docker.com/engine/reference/builder/#healthcheck
# Note: curl is not available in Alpine images! Use wget instead.
HEALTHCHECK --interval=1m --timeout=3s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/ || exit 1
 
CMD java -jar ${JVM_OPTS} ${USER_HOME}/${JAR_FILE}
