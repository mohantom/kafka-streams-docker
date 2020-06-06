package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MovieLoaderService {

  private static final String MOVIE = "movies";

  @Autowired
  private KafkaProducer<String, Movie> kafkaProducer;

  @Autowired
  private ObjectMapper objectMapper;

  public void loadMoviesToKafka() {
    log.info("Start sending messages.");
    Movie movie = Movie.builder().title(UUID.randomUUID().toString()).build();
    kafkaProducer.send(new ProducerRecord<>(MOVIE, movie.getMovieid(), movie));
  }
}
