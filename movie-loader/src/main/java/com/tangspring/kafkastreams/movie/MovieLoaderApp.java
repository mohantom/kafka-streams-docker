package com.tangspring.kafkastreams.movie;

import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@ComponentScan("com.tangspring.kafkastreams.wordcount")
@SpringBootApplication
public class MovieLoaderApp implements CommandLineRunner {

  @Autowired
  private MovieLoaderService movieLoaderService;

  public static void main(String[] args) {
    SpringApplication.run(MovieLoaderApp.class, args);
  }

  @Override
  public void run(String... args) {
    Executors.newSingleThreadExecutor().execute(() -> movieLoaderService.loadMoviesToKafka());
    log.info("Movie loader output app started.");
  }
}

