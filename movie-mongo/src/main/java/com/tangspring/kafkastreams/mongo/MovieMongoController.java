package com.tangspring.kafkastreams.mongo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.tangspring.kafkastreams.shared.models.Movie;
import java.util.List;
import javax.ws.rs.Produces;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
