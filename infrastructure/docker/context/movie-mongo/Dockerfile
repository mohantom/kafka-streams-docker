FROM adoptopenjdk/openjdk11:jdk-11.0.2.9-alpine-slim
WORKDIR             /var/lib
COPY movie-mongo-jar-with-dependencies.jar /var/lib/app.jar
EXPOSE 8040
ENTRYPOINT java ${JAVA_OPTS} -jar /var/lib/app.jar