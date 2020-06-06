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
public class MovieEsApp implements CommandLineRunner {

  private MovieEsService movieEsService;

  public static void main(String[] args) {
    SpringApplication.run(MovieEsApp.class, args);
  }

  @Override
  public void run(String... args) {
    Executors.newSingleThreadExecutor().execute(() -> movieEsService.publishToEs());
    log.info("Movie elasticsearch app started.");
  }
}
