package com.tangspring.kafkastreams.wordcount.output.service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WordcountOutputConsumer {

  @Autowired
  private KafkaConsumer<String, Long> kafkaConsumer;

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
          records.forEach(r -> log.info("key: {}, value: {}", r.key(), r.value()));
        }

      } catch (Exception e) {
        log.error("Error while processing record.", e);
        throw new RuntimeException(e.getMessage());
      }
    }
  }
}
