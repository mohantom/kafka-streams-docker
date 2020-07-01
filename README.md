Kafka Streams on Docker
========================

Adapted from Udemy course: Apache Kafka Series - Kafka Streams for Data Processing

[Kafka-streams-examples](https://github.com/confluentinc/kafka-streams-examples)

## Tech stack
- Kafka
- Kafka Streams
- Elasticsearch
- MongoDB
- Spring Boot
- Spring Retry
- React UI
- Firebase (authentication)

## Modules
1. movie-loader: load movies from csv file `movies_enriched.csv` to kafka topic `movies`
2. movie-mongo: subscribe to `movies`, save all to mongo db
3. movie-streams: subscribe to `movies`, count by year, publish it to `movies-eyar`
4. movie-es: subscribe to `movies` and `movies-year`, save them to es7 withe the same index names
5. movie-ui: show latest movies and top 250 movies by rating


## How to launch
```shell script
// build project
cd src
mvn clean install

// start docker desktop

// start application
cd infrastructure/docker
docker-compose -f common.yml up --build -d
docker-compose -f common.yml -f movies.yml up --build -d
// docker-compose -f common.yml -f words.yml up --build -d

// load movies to mongo/es7
http://localhost:8040/mongo/movie/load

// go to UI
http://localhost:3000/home

// to check logs
docker logs wordcount
docker logs wordcountinput
docker logs wordcountoutput -f

// to shutdown and cleanup
docker-compose  -f common.yml -f word-count.yml down
docker system prune --volumes

```


## movie-loader
to rescan (enrich) movies
1. http://localhost:8010/loader/movie/scan?append=false

2. drop mongo `movie` collection, and es7 `movies` index
or `docer system prune --volumes`

3. reload movie data to mongo and es7: http://localhost:8010/loader/movie/load


## Mongo
- [Introduction to Spring Data MongoDB](https://www.baeldung.com/spring-data-mongodb-tutorial)
- [MongoTemplate aggregation](https://www.baeldung.com/spring-data-mongodb-projections-aggregations)

try this in browser:
http://localhost:8040/mongo/movie/query?title=Terminator

find top 250 rated movies:
http://localhost:8040/mongo/movie/all?size=250&sortField=rating&direction=DESC&page=0

Or query with [Mongo Compass](https://www.mongodb.com/products/compass)
connection: mongodb://root:example@localhost:27017/?authSource=admin&readPreference=primary&appname=MongoDB%20Compass%20Community&ssl=false


## ES7
[ES Rest Highlevel API](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)

use `postman`
```shell script
localhost:9200/movies/_search
localhost:9200/movies-year/_search
```


## TODO
- Endpoint to drop es7 index and mongo collection
- React app to display movies
    - [x] maven build ui, copy build to docker
    - [x] query stats
    - [x] query stats from es7
    - [ ] infinite scroll
    - [ ] filters: genre, years, rating, director?
    - [ ] fix movie stats filter
- deploy to aws
- play from nas

