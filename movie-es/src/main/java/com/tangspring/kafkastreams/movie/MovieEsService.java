package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class MovieEsService {

  private static final String MOVIES = "movies";
  private static final String MOVIES_YEAR = "movies-year";

  private RestTemplate restTemplate;
  private RetryTemplate retryTemplate;
  private KafkaConsumer<String, Long> moviesYearConsumer;
  private KafkaConsumer<String, String> moviesConsumer;
  private RestHighLevelClient esClient;
  private ObjectMapper objectMapper;
  private String elasticsearchHost;

  @PostConstruct
  public void init() throws IOException {
    moviesYearConsumer.subscribe(Collections.singletonList(MOVIES_YEAR));
    moviesConsumer.subscribe(Collections.singletonList(MOVIES));
    Map<String, Object> moviesYearMapping = ImmutableMap.of(
        "properties", ImmutableMap.of(
            "year", ImmutableMap.of("type", "keyword"),
            "count", ImmutableMap.of("type", "integer")
        )
    );
    createMovieIndex(MOVIES_YEAR, moviesYearMapping);

    Map<String, Object> moviesMapping = ImmutableMap.of(
        "properties", ImmutableMap.of(
            "title", ImmutableMap.of("type", "text"),
            "cnTitle", ImmutableMap.of("type", "text"),
            "genre", ImmutableMap.of("type", "text"),
            "year", ImmutableMap.of("type", "keyword"),
            "rating", ImmutableMap.of("type", "keyword")
        )
    );
    createMovieIndex(MOVIES, moviesMapping);
  }

  public void publishMoviesToEs() {

    while (true) {
      try {
        ConsumerRecords<String, String> records = moviesConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

        if (records.count() <= 0) {
          continue;
        }

        log.info("Received {} record.", records.count());

        BulkRequest bulkRequest = createBulkRequestMovies(records);
        BulkResponse responses = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        log.info("Updated ES7 movies with {} records with status {}", records.count(), responses.status().getStatus());

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  public void publishMoviesCountToEs() {

    while (true) {
      try {
        ConsumerRecords<String, Long> records = moviesYearConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

        if (records.count() <= 0) {
          continue;
        }

        log.info("Received {} record.", records.count());

        BulkRequest bulkRequest = createBulkRequestMoviesYear(records);
        BulkResponse responses = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        log.info("Updated ES7 movies-year with {} records with status {}", records.count(), responses.status().getStatus());

        getAllMovieCountsByYearFromEs();

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  private BulkRequest createBulkRequestMoviesYear(ConsumerRecords<String, Long> records) {
    BulkRequest bulkRequest = new BulkRequest();
    for (ConsumerRecord<String, Long> r : records) {
      String year = r.key();
      MovieCountYear movieCountYear = MovieCountYear.builder().year(year).count(r.value()).build();
      String movieCountYearJson = JacksonUtil.toJson(movieCountYear);
      IndexRequest indexRequest = new IndexRequest(MOVIES_YEAR).id(year).source(movieCountYearJson, XContentType.JSON);
      log.info("key: {}, value: {}", r.key(), r.value());
      bulkRequest.add(indexRequest);
    }
    return bulkRequest;
  }

  private BulkRequest createBulkRequestMovies(ConsumerRecords<String, String> records) {
    BulkRequest bulkRequest = new BulkRequest();
    for (ConsumerRecord<String, String> r : records) {
      Movie movie = JacksonUtil.fromJson(r.value(), Movie.class);
      IndexRequest indexRequest = new IndexRequest(MOVIES).id(movie.getMovieid()).source(r.value(), XContentType.JSON);
      log.info("key: {}, value: {}", r.key(), r.value());
      bulkRequest.add(indexRequest);
    }
    return bulkRequest;
  }

  private void getAllMovieCountsByYearFromEs() throws IOException {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
    SearchRequest searchRequest = new SearchRequest(MOVIES_YEAR).source(searchSourceBuilder);

    SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

    List<MovieCountYear> currentFruitCounts = Arrays.stream(searchResponse.getHits().getHits())
        .map(SearchHit::getSourceAsString)
        .map(MovieCountYear::from)
        .collect(Collectors.toList());

    log.info("current movies count by year: \n {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentFruitCounts));
  }


  private void createMovieIndex(String indexName, Map<String, Object> mapping) throws IOException {
    if (isExists(indexName)) {
      return;
    }

    CreateIndexRequest request = new CreateIndexRequest(indexName)
        .mapping(mapping)
        .settings(Settings.builder()
            .put("index.number_of_shards", 1)
            .put("index.number_of_replicas", 2)
        );

    CreateIndexResponse indexResponse = esClient.indices().create(request, RequestOptions.DEFAULT);
    log.info("Create movie index {} response id: {}", indexName, indexResponse.index());
  }

  private boolean isExists(String indexName) throws IOException {
    log.info("Waiting for ES7 is ready...");
    retryTemplate.execute(retryCtx -> restTemplate.getForEntity(elasticsearchHost, JsonNode.class));

    log.info("ES7 is ready. Creating index...");
    GetIndexRequest getRequest = new GetIndexRequest(indexName);
    return esClient.indices().exists(getRequest, RequestOptions.DEFAULT);
  }
}
