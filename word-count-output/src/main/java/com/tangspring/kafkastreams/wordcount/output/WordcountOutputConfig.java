package com.tangspring.kafkastreams.wordcount.output;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@EnableKafka
@ComponentScan("com.tangspring.kafkastreams.wordcount")
@Configuration
public class WordcountOutputConfig {

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
    return new ObjectMapper();
  }

  @Bean
  public KafkaConsumer<String, Long> kafkaConsumer() {
    Map<String, Object> props = createKafkaProps(bootstrapServers, groupId, maxPollRecords);
    log.info("Created Kafka consumer at {}:{}:{}", bootstrapServers, groupId, maxPollRecords);
    return new KafkaConsumer<>(props);
  }

  @Bean(destroyMethod = "close")
  public RestHighLevelClient restHighLevelClient() {
    // TODO: fix connection to ES in docker
    return new RestHighLevelClient(RestClient.builder(new HttpHost(elasticsearchHost)));
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

