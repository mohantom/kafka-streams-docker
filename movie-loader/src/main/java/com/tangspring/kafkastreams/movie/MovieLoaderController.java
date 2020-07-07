package com.tangspring.kafkastreams.movie;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.tangspring.kafkastreams.shared.models.Movie;
import java.util.List;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@Produces(APPLICATION_JSON)
public class MovieLoaderController {

  private final MovieScanService movieScanService;
  private final MovieLoaderService movieLoaderService;
  private final MovieRenameService movieRenameService;

  @GetMapping("/movie/info")
  public String info() {
    return "Hello from Loader movie app";
  }

  @GetMapping("/movie/scan")
  public String scanNasMovies() {
    return movieScanService.scanNasMovies();
  }

  @GetMapping("/movie/enrich")
  public List<Movie> enrichAllMovies() {
    return movieScanService.enrichAllMovies();
  }

  @GetMapping("/movie/load")
  public String loadMovies() {
    movieLoaderService.loadMoviesToKafka();
    return "Loaded all movies to Kafka";
  }

  @GetMapping("/movie/rename")
  public String renameMovies() {
    movieRenameService.renameMovies();
    return "Renamed all movies";
  }
}
