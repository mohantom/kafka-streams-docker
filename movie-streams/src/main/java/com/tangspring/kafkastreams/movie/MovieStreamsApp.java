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
public class MovieStreamsApp implements CommandLineRunner {

  private MovieStreamsService movieStreamsService;

  public static void main(String[] args) {
    SpringApplication.run(MovieStreamsApp.class, args);
  }

  @Override
  public void run(String... args) {
    Executors.newSingleThreadExecutor().execute(() -> movieStreamsService.processMovies());
    log.info("Movie streams app started.");
  }
}
