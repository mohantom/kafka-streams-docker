package com.tangspring.kafkastreams.wordcount.output.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.wordcount.output.model.FruitCount;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
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

        if (records.count() > 0) {
          log.info("Received {} record.", records.count());
          records.forEach(r -> {
            FruitCount fruitCount = FruitCount.builder().id(UUID.randomUUID().toString()).name(r.key()).count(r.value()).build();

            Map<String, Object> fruitCountMap = objectMapper.convertValue(fruitCount, Map.class);

            IndexRequest indexRequest = new IndexRequest("fruits")
                .id(fruitCount.getId())
                .source(fruitCountMap);

            log.info("key: {}, value: {}", r.key(), r.value());

            try {
              IndexResponse indexResponse = esClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
              log.error("Failed to update index for fruit count {}", fruitCount, e);
              throw new RuntimeException("Failed to update index for fruit count");
            }
          });
        }

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }
}
