package com.tangspring.kafkastreams.mongo;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import com.google.common.collect.ImmutableList;
import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.io.File;
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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Slf4j
@AllArgsConstructor
public class MovieMongoService {

  private static final String MOVIES = "movies";

  private final String outputFolder;
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
    query.addCriteria(Criteria.where("title").regex(String.format(".*%s.*", title), "i"))
      .with(Sort.by(Direction.DESC, "year")); // case-insensitive
    return mongoTemplate.find(query, Movie.class);
  }

  public List<Movie> findAllMovies(String genre, int page, int size, String sortField, Direction direction) {
    Query query = new Query();
    if (StringUtils.isNotBlank(genre)) {
      query.addCriteria(Criteria.where("genre").regex(String.format(".*%s.*", genre), "i")); // case-insensitive
    }

    Pageable pageable = PageRequest.of(page, size);
    query.with(pageable).with(Sort.by(direction, sortField));
    return mongoTemplate.find(query, Movie.class);
  }

  public List<CountByYear> countByYear() {
    GroupOperation groupByYear = group("year")
        .count().as("count")
        .sum("boxoffice").as("boxoffice");
    MatchOperation filterCount = match(Criteria.where("count").gt(2));
    SortOperation sortByYear = sort(Sort.by(Direction.DESC, "_id")); // by _id

    Aggregation aggregation = newAggregation(groupByYear, filterCount, sortByYear);
    AggregationResults<CountByYear> result = mongoTemplate.aggregate(aggregation, "movie", CountByYear.class);
    return result.getMappedResults();
  }

  public List<Movie> oscarBestPictures() {
    List<String> oscar = getOscarTitles();

    Query query = new Query();
    query.addCriteria(Criteria.where("title").in(oscar))
        .with(Sort.by(Direction.DESC, "year")); // case-insensitive
    List<Movie> movies = mongoTemplate.find(query, Movie.class)
        .stream().distinct().collect(Collectors.toList());
    return movies;
  }

  private List<String> getOscarTitles() {
    try {
      File oscar = new File(outputFolder, "oscar.txt");
      return FileUtils.readLines(oscar, UTF_8);
    } catch (IOException ie) {
      log.error("Failed to read oscar file", ie);
      return ImmutableList.of();
    }
  }

  public boolean dropCollection(String name) {
    mongoTemplate.dropCollection(name);
    return true;
  }

  @Data
  public static class CountByYear {
    @Id
    private String year;
    private Integer count;
    private Float boxoffice;
  }
}
