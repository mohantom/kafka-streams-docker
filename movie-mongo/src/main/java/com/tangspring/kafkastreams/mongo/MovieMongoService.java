package com.tangspring.kafkastreams.mongo;

import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Slf4j
@AllArgsConstructor
public class MovieMongoService {

  private static final String MOVIES = "movies";

  private final KafkaConsumer<String, String> moviesConsumer;
  private final MongoTemplate mongoTemplate;

  @PostConstruct
  public void init() {
    moviesConsumer.subscribe(Collections.singletonList(MOVIES));
  }

  public void publishMoviesToMongo() {

    while (true) {
      try {
        ConsumerRecords<String, String> records = moviesConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

        if (records.count() <= 0) {
          continue;
        }

        log.info("Received {} record.", records.count());

        List<Movie> movies = StreamSupport.stream(Spliterators.spliteratorUnknownSize(records.iterator(), Spliterator.ORDERED), false)
            .map(r -> JacksonUtil.fromJson(r.value(), Movie.class))
            .collect(Collectors.toList());

        mongoTemplate.insertAll(movies);

        log.info("Updated Mongo movies with {} records.", records.count());

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  public List<Movie> findMoviesByTitle(String title) {
    Query query = new Query();
    query.addCriteria(Criteria.where("title").regex(String.format(".*%s.*", title)));
    return mongoTemplate.find(query, Movie.class);
  }

  public List<Movie> findAllMovies(int page, int size, String sortField, Direction direction) {
    Query query = new Query();
    Pageable pageable = PageRequest.of(page, size);
    query.with(pageable).with(Sort.by(direction, sortField));
    return mongoTemplate.find(query, Movie.class);
  }
}
