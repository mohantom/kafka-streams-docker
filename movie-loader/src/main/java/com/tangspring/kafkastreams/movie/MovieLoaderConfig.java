package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class MovieLoaderConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${output.folder}")
  private String outputFolder;

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return JacksonUtil.getObjectMapper();
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public KafkaProducer<String, String> kafkaProducer() {
    return new KafkaProducer<>(getKafkaProperties());
  }

  @Bean
  public MovieLoaderService movieLoaderService(KafkaProducer<String, String> kafkaProducer) {
    return new MovieLoaderService(kafkaProducer, outputFolder);
  }

  @Bean
  public MovieScanService movieScanService(RestTemplate restTemplate) {
    return new MovieScanService(restTemplate, outputFolder);
  }

  @Bean
  public MovieRenameService movieRenameService(RestTemplate restTemplate) {
    return new MovieRenameService(restTemplate);
  }


  private Properties getKafkaProperties() {
    Properties props = new Properties();

    // kafka bootstrap server
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
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
}

