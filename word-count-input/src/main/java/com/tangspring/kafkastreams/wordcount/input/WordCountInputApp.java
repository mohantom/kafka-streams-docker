package com.tangspring.kafkastreams.wordcount.input;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class WordCountInputApp {

  private static final Random random = new Random();
  private static final String[] words = {"apple", "banana", "mango", "water mellon", "orange", "pineapple", "grape"};

  public static void main(String[] args) throws InterruptedException {
    Producer<String, String> producer = new KafkaProducer<>(getKafkaProperties());
    log.info("Start sending messages.");

    int size = words.length;
    while(true) {
      int num = random.nextInt(size);
      String word = words[num % size];

      producer.send(sentence("fruit", word));
      Thread.sleep(200);
    }
  }

  private static Properties getKafkaProperties() {
    Properties props = new Properties();

    // kafka bootstrap server
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_SERVER"));
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // producer acks
    props.put(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
    props.put(ProducerConfig.RETRIES_CONFIG, "3");
    props.put(ProducerConfig.LINGER_MS_CONFIG, "1");

    // leverage idempotent producer from Kafka 0.11 !
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // ensure we don't push duplicates
    return props;
  }

  private static ProducerRecord<String, String> sentence(String key, String value) {
    return new ProducerRecord<>("word-count-input", key, value);
  }
}
