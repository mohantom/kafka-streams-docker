package com.tangspring.kafkastreams.movie;

import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@AllArgsConstructor
public class MovieLoaderApp implements CommandLineRunner {

  private MovieLoaderService movieLoaderService;

  public static void main(String[] args) {
    SpringApplication.run(MovieLoaderApp.class, args);
  }

  @Override
  public void run(String... args) {
//    Executors.newSingleThreadExecutor().execute(() -> movieLoaderService.loadMoviesToKafka());
    log.info("Movie loader app started.");
  }
}

