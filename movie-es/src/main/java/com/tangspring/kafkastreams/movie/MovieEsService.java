package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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

@Slf4j
@AllArgsConstructor
public class MovieEsService {

  private static final String MOVIES = "movies";
  private static final String MOVIES_YEAR = "movies-year";

  private KafkaConsumer<String, Long> kafkaConsumer;
  private RestHighLevelClient esClient;
  private ObjectMapper objectMapper;

  @PostConstruct
  public void init() throws IOException {
    kafkaConsumer.subscribe(Collections.singletonList(MOVIES_YEAR));
    createMovieIndex();
  }

  public void publishToEs() {

    while (true) {
      try {
        ConsumerRecords<String, Long> records = kafkaConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

        if (records.count() <= 0) {
          continue;
        }

        log.info("Received {} record.", records.count());

        BulkRequest bulkRequest = createBulkRequest(records);
        BulkResponse responses = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        log.info("Updated ES7 with {} records with status {}", records.count(), responses.status().getStatus());

        getAllMovieCountsByYearFromEs();

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  private BulkRequest createBulkRequest(ConsumerRecords<String, Long> records) {
    BulkRequest bulkRequest = new BulkRequest();
    for (ConsumerRecord<String, Long> r : records) {
      String year = r.key();
      MovieCountYear movieCountYear = MovieCountYear.builder().year(year).count(r.value()).build();
      String movieCountYearJson = JacksonUtil.toJson(movieCountYear);
      IndexRequest indexRequest = new IndexRequest(MOVIES).id(year).source(movieCountYearJson, XContentType.JSON);
      log.info("key: {}, value: {}", r.key(), r.value());
      bulkRequest.add(indexRequest);
    }
    return bulkRequest;
  }

  private void getAllMovieCountsByYearFromEs() throws IOException {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
    SearchRequest searchRequest = new SearchRequest(MOVIES).source(searchSourceBuilder);

    SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

    List<MovieCountYear> currentFruitCounts = Arrays.stream(searchResponse.getHits().getHits())
        .map(SearchHit::getSourceAsString)
        .map(MovieCountYear::from)
        .collect(Collectors.toList());

    log.info("current movies count by year: \n {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentFruitCounts));
  }


  private void createMovieIndex() throws IOException {
    if (isExists()) {
      return;
    }

    CreateIndexRequest request = new CreateIndexRequest(MOVIES);
    request.settings(Settings.builder()
        .put("index.number_of_shards", 1)
        .put("index.number_of_replicas", 2)
    );

    Map<String, Object> mapping = ImmutableMap.of(
        "properties", ImmutableMap.of(
            "year", ImmutableMap.of("type", "keyword"),
            "count", ImmutableMap.of("type", "integer")
        )
    );
    request.mapping(mapping);
    CreateIndexResponse indexResponse = esClient.indices().create(request, RequestOptions.DEFAULT);
    log.info("Create movie index response id: " + indexResponse.index());
  }

  private boolean isExists() throws IOException {
    GetIndexRequest getRequest = new GetIndexRequest(MOVIES);
    return esClient.indices().exists(getRequest, RequestOptions.DEFAULT);
  }
}
