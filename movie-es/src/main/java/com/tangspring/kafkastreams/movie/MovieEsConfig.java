package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MovieEsConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${spring.kafka.auto-offset-reset}")
  private String autoOffset;

  @Value("${spring.kafka.group-id}")
  private String groupId;

  @Value("${spring.kafka.max-poll-records}")
  private String maxPollRecords;

  @Value("${spring.es.host}")
  private String elasticsearchHost;

  @Bean
  public ObjectMapper objectMapper() {
    return JacksonUtil.getObjectMapper();
  }

  @Bean
  public MovieEsService movieEsService(KafkaConsumer<String, Long> kafkaConsumer, RestHighLevelClient esClient, ObjectMapper objectMapper) {
    return new MovieEsService(kafkaConsumer, esClient, objectMapper);
  }

  @Bean
  public KafkaConsumer<String, Long> kafkaConsumer() {
    Map<String, Object> props = createKafkaProps(bootstrapServers, groupId, maxPollRecords);
    log.info("Created Kafka consumer at {}:{}:{}", bootstrapServers, groupId, maxPollRecords);
    return new KafkaConsumer<>(props);
  }

  @Bean(destroyMethod = "close")
  public RestHighLevelClient esClient() {
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticsearchHost)));
  }

  private Map<String, Object> createKafkaProps(String bootstrapServers, String groupId,
      String maxPollRecords) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffset);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
    return props;
  }

}
