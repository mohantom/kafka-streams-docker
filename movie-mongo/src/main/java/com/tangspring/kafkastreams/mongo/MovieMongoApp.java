package com.tangspring.kafkastreams.mongo;

import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@Slf4j
@EnableRetry
@SpringBootApplication
@AllArgsConstructor
public class MovieMongoApp implements CommandLineRunner {

  private MovieMongoService movieMongoService;

  public static void main(String[] args) {
    SpringApplication.run(MovieMongoApp.class, args);
  }

  @Override
  public void run(String... args) {
    Executors.newSingleThreadExecutor().execute(() -> movieMongoService.publishMoviesToMongo());
    log.info("Movie Mongo app started.");
  }
}
