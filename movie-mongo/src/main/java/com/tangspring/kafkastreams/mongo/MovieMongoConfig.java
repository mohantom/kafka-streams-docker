package com.tangspring.kafkastreams.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
@Configuration
public class MovieMongoConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.auto-offset-reset}")
  private String autoOffset;

  @Value("${spring.kafka.group-id}")
  private String groupId;

  @Value("${spring.kafka.max-poll-records}")
  private String maxPollRecords;

  @Value("${output.folder}")
  private String outputFolder;

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return JacksonUtil.getObjectMapper();
  }

  @Bean
  @Qualifier("movies")
  public KafkaConsumer<String, String> moviesConsumer() {
    Map<String, Object> props = createKafkaProps(bootstrapServers, "movies", maxPollRecords, StringDeserializer.class);
    log.info("Created Kafka consumer at {}:{}:{}", bootstrapServers, groupId, maxPollRecords);
    return new KafkaConsumer<>(props);
  }

  @Bean
  public MovieMongoService movieMongoService(KafkaConsumer kafkaConsumer, MongoTemplate mongoTemplate) {
    return new MovieMongoService(outputFolder, kafkaConsumer, mongoTemplate);
  }

  @Bean
  public MoviePlayService moviePlayService() {
    return new MoviePlayService();
  }

  private Map<String, Object> createKafkaProps(String bootstrapServers, String groupId,
      String maxPollRecords, Class valueDeserializer) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
    return props;
  }

}
