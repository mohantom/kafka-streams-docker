FROM adoptopenjdk/openjdk11:jdk-11.0.2.9-alpine-slim
WORKDIR             /var/lib
COPY word-count-output-jar-with-dependencies.jar /var/lib/app.jar
EXPOSE 8010
ENTRYPOINT java ${JAVA_OPTS} -jar /var/lib/app.jar