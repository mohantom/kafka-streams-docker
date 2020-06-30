package com.tangspring.kafkastreams.movie;

import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.io.File;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
@AllArgsConstructor
public class MovieLoaderService {

  private static final String MOVIE_TOPIC = "movies";
  private static final String MOVIE_ENRICHED_FILENAME = "movies_enriched.csv";

  private MovieScanService movieScanService;
  private KafkaProducer<String, String> kafkaProducer;
  private final String outputFolder;

  public void loadMoviesToKafka() {
    log.info("Start sending messages.");

    try {
//      List<Movie> movies = movieScanService.loadMoviesFromCsv(null);
//      List<Movie> movies = JacksonUtil.readFromInputStream(this.getClass().getResourceAsStream("/movies_enriched.csv"), Movie.class);
      String filepath = new File(outputFolder, MOVIE_ENRICHED_FILENAME).getAbsolutePath();
      List<Movie> movies = JacksonUtil.readCsvFile(filepath, Movie.class);

      movies.forEach(this::publishToKafka);
      log.info("Loaded {} movies to Kafka", movies.size());
    } catch (Exception e) {
      log.warn("Failed extract movie and send to Kafka.", e);
    }
  }

  private int publishToKafka(Movie m) {
    return Optional.ofNullable(m).map(
        movie -> {
          kafkaProducer.send(new ProducerRecord<>(MOVIE_TOPIC, movie.getMovieid(), JacksonUtil.toJson(movie)));
          return 1;
        })
        .orElse(0);
  }

}
