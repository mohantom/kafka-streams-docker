package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.shared.JacksonUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
@AllArgsConstructor
public class MovieLoaderService {

  private static final Pattern PATTERN = Pattern.compile("(\\d{4})");
  private static final String MOVIE_TOPIC = "movies";

  private KafkaProducer<String, String> kafkaProducer;
  private ObjectMapper objectMapper;

  public void loadMoviesToKafka() {
    log.info("Start sending messages.");


    try {
      List<String> movieLines = readFromFile("/movies.txt");

      int count = movieLines.stream()
          .map(l -> extractMovie(PATTERN, l))
          .map(this::publishToKafka)
          .mapToInt(Integer::valueOf)
          .sum();

      log.info("Published {} out of {} movies to Kafka", count, movieLines.size());
    } catch (Exception e) {
      log.warn("Failed extract movie and send to Kafka.", e);
    }

  }

  private List<String> readFromFile(String filepath) throws IOException {
    List<String> result = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(filepath)))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        result.add(line);
      }
    }
    return result;
  }

  private int publishToKafka(Movie m) {
    return Optional.ofNullable(m).map(
        movie -> {
          kafkaProducer.send(new ProducerRecord<>(MOVIE_TOPIC, movie.getMovieid(), JacksonUtil.toJson(movie)));
          return 1;
        })
        .orElse(0);
  }

  private Movie extractMovie(Pattern pattern, String l) {
    Matcher matcher = pattern.matcher(l);
    String year = matcher.find() ? matcher.group(0) : "0000";
    String title = StringUtils.substringBefore(l, year);
    return StringUtils.isNotBlank(title) ?
        Movie.builder().title(title).year(Integer.parseInt(year)).build() : null;
  }
}
