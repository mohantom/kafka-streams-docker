package com.tangspring.kafkastreams.wordcount.output.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.tangspring.kafkastreams.wordcount.output.model.FruitCount;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WordcountOutputService {

  private static final String FRUITS = "fruits";

  @Autowired
  private KafkaConsumer<String, Long> kafkaConsumer;

  @Autowired
  private RestHighLevelClient esClient;

  @Autowired
  private ObjectMapper objectMapper;

  @PostConstruct
  public void init() throws IOException {
    kafkaConsumer.subscribe(Collections.singletonList("word-count-output"));
    createFruitsIndex();
  }

  public void processWordcountOutput() {

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

        getAllFruitCountsFromEs();

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }

  private BulkRequest createBulkRequest(ConsumerRecords<String, Long> records) throws com.fasterxml.jackson.core.JsonProcessingException {
    BulkRequest bulkRequest = new BulkRequest();
    for (ConsumerRecord<String, Long> r : records) {
      String fruit = r.key();
      FruitCount fruitCount = FruitCount.builder().name(fruit).count(r.value()).build();
      String fruitJson = objectMapper.writeValueAsString(fruitCount);
      IndexRequest indexRequest = new IndexRequest(FRUITS).id(fruit).source(fruitJson, XContentType.JSON);
      log.info("key: {}, value: {}", r.key(), r.value());
      bulkRequest.add(indexRequest);
    }
    return bulkRequest;
  }

  private void getAllFruitCountsFromEs() throws IOException {
//    QueryBuilder queryBuilder = QueryBuilders
//        .boolQuery()
//        .must(QueryBuilders.matchQuery("name", "orange"));
//    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(queryBuilder);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
    SearchRequest searchRequest = new SearchRequest(FRUITS).source(searchSourceBuilder);

    SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

    List<FruitCount> currentFruitCounts = Arrays.stream(searchResponse.getHits().getHits())
        .map(SearchHit::getSourceAsString)
        .map(FruitCount::from)
        .collect(Collectors.toList());

    log.info("currentFruitCounts: \n {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentFruitCounts));
  }


  private void createFruitsIndex() throws IOException {
    GetIndexRequest getRequest = new GetIndexRequest(FRUITS);
    boolean exists = esClient.indices().exists(getRequest, RequestOptions.DEFAULT);
    if (exists) {
      return;
    }

    CreateIndexRequest request = new CreateIndexRequest(FRUITS);
    request.settings(Settings.builder()
        .put("index.number_of_shards", 1)
        .put("index.number_of_replicas", 2)
    );

    Map<String, Object> mapping = ImmutableMap.of(
        "properties", ImmutableMap.of(
            "name", ImmutableMap.of("type", "keyword"),
            "count", ImmutableMap.of("type", "integer")
        )
    );
    request.mapping(mapping);
    CreateIndexResponse indexResponse = esClient.indices().create(request, RequestOptions.DEFAULT);
    log.info("Create fruits index response id: " + indexResponse.index());
  }
}
