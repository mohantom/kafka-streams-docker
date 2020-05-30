package com.tangspring.kafkastreams.join;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;


// Run the Kafka Streams application before running the producer.
// This will be best for your learning
@Slf4j
public class UserDataProducer {

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    Producer<String, String> producer = new KafkaProducer<>(getProperties());

    // we are going to test different scenarios to illustrate the join

    // 1 - we create a new user, then we send some data to Kafka
    log.info("Example 1 - new user");
    producer.send(userRecord("john", "First=John,Last=Doe,Email=john.doe@gmail.com")).get();
    producer.send(purchaseRecord("john", "Apples and Bananas (1)")).get();

    Thread.sleep(10000);

    // 2 - we receive user purchase, but it doesn't exist in Kafka
    log.info("Example 2 - non existing user");
    producer.send(purchaseRecord("bob", "Kafka Udemy Course (2)")).get();

    Thread.sleep(10000);

    // 3 - we update user "john", and send a new transaction
    log.info("Example 3 - update to user");
    producer.send(userRecord("john", "First=Johnny,Last=Doe,Email=johnny.doe@gmail.com")).get();
    producer.send(purchaseRecord("john", "Oranges (3)")).get();

    Thread.sleep(10000);

    // 4 - we send a user purchase for stephane, but it exists in Kafka later
    log.info("Example 4 - non existing user then user");
    producer.send(purchaseRecord("stephane", "Computer (4)")).get();
    producer.send(userRecord("stephane", "First=Stephane,Last=Maarek,GitHub=simplesteph")).get();
    producer.send(purchaseRecord("stephane", "Books (4)")).get();
    producer.send(userRecord("stephane", null)).get(); // delete for cleanup

    Thread.sleep(10000);

    // 5 - we create a user, but it gets deleted before any purchase comes through
    log.info("Example 5 - user then delete then data");
    producer.send(userRecord("alice", "First=Alice")).get();
    producer.send(userRecord("alice", null)).get(); // that's the delete record
    producer.send(purchaseRecord("alice", "Apache Kafka Series (5)")).get();

    Thread.sleep(10000);

    log.info("End of demo");
    producer.close();
  }

  private static Properties getProperties() {
    Properties properties = new Properties();

    // kafka bootstrap server
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // producer acks
    properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
    properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
    properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");
    // leverage idempotent producer from Kafka 0.11 !
    properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // ensure we don't push duplicates
    return properties;
  }

  private static ProducerRecord<String, String> userRecord(String key, String value) {
    return new ProducerRecord<>("user-table", key, value);
  }

  private static ProducerRecord<String, String> purchaseRecord(String key, String value) {
    return new ProducerRecord<>("user-purchases", key, value);
  }
}
