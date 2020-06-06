# Kafka Streams on Docker

Adapted from Udemy course: Apache Kafka Series - Kafka Streams for Data Processing

[Kafka-streams-examples](https://github.com/confluentinc/kafka-streams-examples)

```
cd src
mvn install

cd infrastructure/docker
docker-compose -f common.yml up --build -d
docker-compose -f common.yml -f movies.yml up --build -d
docker-compose -f common.yml -f words.yml up --build -d

docker logs wordcount
docker logs wordcountinput
docker logs wordcountoutput -f

docker-compose  -f common.yml -f word-count.yml down
docker system prune --volumes
```
