package com.tangspring.kafkastreams.wordcount.output.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.wordcount.output.model.FruitCount;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WordcountOutputConsumer {

  @Autowired
  private KafkaConsumer<String, Long> kafkaConsumer;

  @Autowired
  private RestHighLevelClient esClient;

  @Autowired
  private ObjectMapper objectMapper;

  @PostConstruct
  public void init() {
    kafkaConsumer.subscribe(Collections.singletonList("word-count-output"));
  }

  public void processWordcountOutput() {

    while (true) {
      try {
        ConsumerRecords<String, Long> records = kafkaConsumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

        BulkRequest bulkRequest = new BulkRequest();
        if (records.count() > 0) {
          log.info("Received {} record.", records.count());
          records.forEach(r -> {
            FruitCount fruitCount = FruitCount.builder().id(UUID.randomUUID().toString()).name(r.key()).count(r.value()).build();

            try {
              String fruitJson = objectMapper.writeValueAsString(fruitCount);

              IndexRequest indexRequest = new IndexRequest("fruits")
                  .id(fruitCount.getId())
                  .source(fruitJson, XContentType.JSON);

              log.info("key: {}, value: {}", r.key(), r.value());
              bulkRequest.add(indexRequest);

            } catch (IOException e) {
              log.error("Failed to convert fruitCount: {}", fruitCount, e);
            }
          });

          BulkResponse responses = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
          log.info("Updated ES7 with {} records.", records.count());
        }

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }
}
