package com.tangspring.kafkastreams.wordcount.output;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

@Slf4j
public class WordCountOutputApp {

  public static void main(String[] args) {
    KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(getKafkaProperties());
    consumer.subscribe(Collections.singletonList("word-count-output"));

    while (true) {
      try {
        ConsumerRecords<String, Long> records = consumer.poll(Duration.of(1000, ChronoUnit.MILLIS));

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

  private static Properties getKafkaProperties() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_SERVER"));
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "word-count");
    return props;
  }
}
