FROM clojure:latest

LABEL maintainer="jackdebidda@gmail.com"

ENV APP_DIR=/usr/src/app \
    PORT=3000 \
    JVM_OPTS=-Dclojure.main.report=stderr

# RUN apt-get -y update
# RUN apt-get -y install tree

RUN mkdir -p ${APP_DIR}

# The WORKDIR instruction sets the working directory for any RUN, CMD,
# ENTRYPOINT, COPY and ADD instructions that follow it in the Dockerfile.
WORKDIR ${APP_DIR}

EXPOSE ${PORT}

COPY project.clj ${APP_DIR}/
COPY resources ${APP_DIR}/resources
COPY src ${APP_DIR}/src

RUN lein ring uberjar
# RUN tree -L 3

CMD java -jar ${JVM_OPTS} ${APP_DIR}/target/uberjar/tweedler-standalone.jar
# CMD lein ring server-headless
