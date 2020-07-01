package com.tangspring.kafkastreams.mongo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.google.common.collect.ImmutableMap;
import com.tangspring.kafkastreams.mongo.MovieMongoService.CountByYear;
import com.tangspring.kafkastreams.shared.models.Movie;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@Produces(APPLICATION_JSON)
public class MovieMongoController {

  private final MovieMongoService movieMongoService;

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
  public Response deleteIndices() throws IOException {
    boolean movieDropped = movieMongoService.dropCollection("movie");
    return Response.ok().entity(ImmutableMap.of("movie", movieDropped)).build();
  }
}
