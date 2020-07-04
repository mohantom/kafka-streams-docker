package com.tangspring.kafkastreams.mongo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.tangspring.kafkastreams.mongo.MovieMongoService.CountByYear;
import com.tangspring.kafkastreams.shared.models.Movie;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@Produces(APPLICATION_JSON)
public class MovieMongoController {

  private final MovieMongoService movieMongoService;
  private final MoviePlayService moviePlayService;

  @GetMapping("/movie/info")
  public String info() {
    return "Hello from Mongo movie app";
  }

  @GetMapping("/movie/query")
  public List<Movie> findMoviesByTitle(@RequestParam String title) {
    return movieMongoService.findMoviesByTitle(title);
  }

  @GetMapping("/movie/all")
  public List<Movie> findAllMovies(
      @RequestParam(required = false) String genre,
      @RequestParam int page,
      @RequestParam int size,
      @RequestParam String sortField,
      @RequestParam Direction direction
  ) {
    return movieMongoService.findAllMovies(genre, page, size, sortField, direction);
  }

  @GetMapping("/movie/count-by-year")
  public List<CountByYear> findMovieCountByYear() {
    return movieMongoService.countByYear();
  }

  @DeleteMapping("/movie")
  public String deleteIndices() throws IOException {
    movieMongoService.dropCollection("movie");
    return "Mongo collection movie is dropped.";
  }

  @CrossOrigin
  @PostMapping("/movie/play")
  public String playMovie(@RequestBody PlayMovieRequest request) throws Exception {
    return moviePlayService.playMovie(request.getFileurl());
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  private static class PlayMovieRequest {
    private String fileurl;
  }
}
