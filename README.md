# Kafka Streams on Docker

Adapted from Udemy course: Apache Kafka Series - Kafka Streams for Data Processing

[Kafka-streams-examples](https://github.com/confluentinc/kafka-streams-examples)

```
cd src
mvn install

cd infrastructure/docker
docker-compose up --build -d

docker logs wordcount
docker logs wordcountinput
docker logs wordcountoutput -f

docker-compose down
docker system prune --volumes
```
