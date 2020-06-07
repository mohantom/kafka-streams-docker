package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import com.tangspring.kafkastreams.shared.models.Movie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
@AllArgsConstructor
public class MovieLoaderService {

  private static final Pattern PATTERN = Pattern.compile("(\\d{4})");
  private static final Pattern PATTERN_RATING = Pattern.compile("(\\d\\.\\d)");
  private static final Random random = new Random();
  private static final String MOVIE_TOPIC = "movies";
  public static final String DEFAULT_YEAR_RATING = "0000";

  private KafkaProducer<String, String> kafkaProducer;

  public void loadMoviesToKafka() {
    log.info("Start sending messages.");

    try {
      List<String> movieLines = readFromFile("/movies.txt");

      List<Movie> movies = movieLines.stream()
          .map(this::extractMovie)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      int size = movies.size();
      log.info("Parsed {} movies.", size);

      movies.forEach(this::publishToKafka);

//      while(true) {
//        int num = random.nextInt(size);
//        Movie movie = movies.get(num % size);
//
//        publishToKafka(movie);
//        Thread.sleep(100);
//      }
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

  private Movie extractMovie(String l) {
    Matcher matcher = PATTERN.matcher(l);
    String year = matcher.find() ? matcher.group(0) : DEFAULT_YEAR_RATING;

    String title = StringUtils.trim(StringUtils.substringBefore(l, year));
    String enTitle = StringUtils.trim(title.replaceAll("[^\\x00-\\x7F]", "")); // replace non-latin symbols
    String cnTitle = StringUtils.trim(StringUtils.substringAfter(title, enTitle));

    Matcher matcherRating = PATTERN_RATING.matcher(l);
    String rating = matcherRating.find() ? matcherRating.group(0) : DEFAULT_YEAR_RATING;

    String afterRating = StringUtils.trim(StringUtils.substringAfter(l, rating));
    String genre = StringUtils.substringBefore(afterRating, " ");

    return StringUtils.isAnyBlank(enTitle, year, rating, genre) || DEFAULT_YEAR_RATING.equals(year) || DEFAULT_YEAR_RATING.equals(rating)? null :
        Movie.builder()
            .title(enTitle)
            .cntitle(cnTitle)
            .genre(genre)
            .year(year)
            .rating(rating)
            .build();
  }
}
