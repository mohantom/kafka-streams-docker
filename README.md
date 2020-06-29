Kafka Streams on Docker
========================

Adapted from Udemy course: Apache Kafka Series - Kafka Streams for Data Processing

[Kafka-streams-examples](https://github.com/confluentinc/kafka-streams-examples)

- Kafka
- Kafka Streams
- Elasticsearch
- MongoDB
- Spring Boot
- Spring Retry
- React
- Firebase (authentication)

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

## Mongo
try this in browser:
http://localhost:8040/mongo/movie/query?title=Terminator

Or query with [Mongo Compass](https://www.mongodb.com/products/compass)
connection: mongodb://root:example@localhost:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass%20Community&ssl=false

### to load movies to mongo/es7
http://localhost:8040/mongo/movie/load

### go to UI
http://localhost:3000/home


## TODO
- endpoint to drop es7 index and mongo collection
- React app to display movies
    - [x] maven build ui, copy build to docker
    - [x] query stats
    - [x] query stats from es7
    - [ ] infinite scroll
    - [ ] filters: genre, years, rating, director?
    - [ ] fix movies with rating N/A from code
    - [ ] fix movie stats filter
- deploy to aws
- play from nas

