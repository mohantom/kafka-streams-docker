package com.tangspring.kafkastreams.wordcount.output;

import com.tangspring.kafkastreams.wordcount.output.service.WordcountOutputService;
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
public class WordcountOutputApp implements CommandLineRunner {

  @Autowired
  private WordcountOutputService wordcountOutputService;

  public static void main(String[] args) {
    SpringApplication.run(WordcountOutputApp.class, args);
  }

  @Override
  public void run(String... args) {
    Executors.newSingleThreadExecutor().execute(() -> wordcountOutputService.processWordcountOutput());
    log.info("Wordcount output app started.");
  }
}

