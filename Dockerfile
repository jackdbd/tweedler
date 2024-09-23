# === STAGE 1 ================================================================ #
# Build a standalone .jar file
# ============================================================================ #
FROM clojure:tools-deps-bullseye-slim as builder

LABEL maintainer="giacomo@giacomodebidda.com"

ENV APP_DIR="/usr/src/app" \
    JAR_FILE="tweedler-1.2.0-standalone.jar"

RUN mkdir -p ${APP_DIR}

# The WORKDIR instruction sets the working directory for any RUN, CMD,
# ENTRYPOINT, COPY and ADD instructions that follow it in the Dockerfile.
WORKDIR ${APP_DIR}

# Put files/directories that change less frequently first
COPY build.clj ${APP_DIR}/
COPY migratus.clj ${APP_DIR}/
COPY deps.edn ${APP_DIR}/
COPY resources ${APP_DIR}/resources
COPY src ${APP_DIR}/src

RUN clj -T:build uber

# === STAGE 2 ================================================================ #
# Copy the .jar built at stage 1 and execute it
# ============================================================================ #

FROM eclipse-temurin:23_37-jre-ubi9-minimal
# Red Hat Universal Base Image 9 Minimal uses microdnf as a package manager.
# https://catalog.redhat.com/software/containers/ubi9/ubi-minimal/615bd9b4075b022acc111bf5

ENV APP_DIR="/usr/src/app" \
    USER_HOME="/home/appuser" \
    JAR_FILE="tweedler-1.2.0-standalone.jar" \
    PORT=8080 \
    JVM_OPTS="-Dclojure.main.report=stderr"

# Install shadow-utils (which contains groupadd and useradd)
RUN microdnf install -y shadow-utils && \
    microdnf clean all

# Create a group and a user
RUN groupadd -r appgroup && \
    useradd -r -g appgroup -d /home/appuser -m -s /bin/bash appuser

USER appuser

WORKDIR ${USER_HOME}

COPY --from=builder ${APP_DIR}/target/${JAR_FILE} ${USER_HOME}/${JAR_FILE}

# This is just for troubleshooting purposes
# RUN microdnf install tree && microdnf clean all
# RUN tree -L 3

EXPOSE ${PORT}

# Check every x minutes that a web-server is able to serve the site's main page
# within y seconds.
# https://docs.docker.com/engine/reference/builder/#healthcheck
# Note: curl is not available in Alpine images! Use wget instead.
HEALTHCHECK --interval=1m --timeout=3s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT}/ || exit 1
 
CMD java -jar ${JVM_OPTS} ${USER_HOME}/${JAR_FILE}
