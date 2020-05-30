# Kafka Streams on Docker

Adapted from Udemy course: Apache Kafka Series - Kafka Streams for Data Processing

```
cd src
mvn install

cd infrastructure/docker
docker-compose -f common.yml up --build -d

docker logs wordcount
docker logs wordcountinput
docker logs wordcountoutput -f

docker-compose -f common.yml down
```
