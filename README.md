# Kafka Streams on Docker

Adapted from Udemy course: Apache Kafka Series - Kafka Streams for Data Processing

[Kafka-streams-examples](https://github.com/confluentinc/kafka-streams-examples)

- Kafka
- Kafka Streams
- Elasticsearch
- MongoDB
- Spring Boot
- Spring Retry

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

try this in browser:
http://localhost:8040/mongo/movie/query?title=Terminator


## TODO
0. python to get a new movies.txt
1. endpoint: rescan movies -> enriched_movies.csv from omdbapi with rating, poster, actor, director
2. endpoint: reload movies to ES7, mongo
3. React app to display movies
