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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

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
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(5000L);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(100);
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }

  @Bean
  public MovieEsService movieEsService(
      RestTemplate restTemplate,
      RetryTemplate retryTemplate,
      @Qualifier("movies-year") KafkaConsumer<String, Long> moviesYearConsumer,
      @Qualifier("movies") KafkaConsumer<String, String> moviesConsumer,
      RestHighLevelClient esClient,
      ObjectMapper objectMapper) {
    return new MovieEsService(restTemplate, retryTemplate, moviesYearConsumer, moviesConsumer, esClient, objectMapper, elasticsearchHost);
  }

  @Bean
  @Qualifier("movies-year")
  public KafkaConsumer<String, Long> moviesYearConsumer() {
    Map<String, Object> props = createKafkaProps(bootstrapServers, "movies-year", maxPollRecords, LongDeserializer.class);
    log.info("Created Kafka consumer at {}:{}:{}", bootstrapServers, "movieses-year", maxPollRecords);
    return new KafkaConsumer<>(props);
  }

  @Bean
  @Qualifier("movies")
  public KafkaConsumer<String, String> moviesConsumer() {
    Map<String, Object> props = createKafkaProps(bootstrapServers, "movieses", maxPollRecords, StringDeserializer.class);
    log.info("Created Kafka consumer at {}:{}:{}", bootstrapServers, "movieses", maxPollRecords);
    return new KafkaConsumer<>(props);
  }

  @Bean(destroyMethod = "close")
  public RestHighLevelClient esClient() {
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticsearchHost)));
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
