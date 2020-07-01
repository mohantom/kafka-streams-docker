package com.tangspring.kafkastreams.movie;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.google.common.collect.ImmutableMap;
import com.tangspring.kafkastreams.shared.models.Movie;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@Produces(APPLICATION_JSON)
public class MovieEsRestController {

  private final MovieEsService movieEsService;
  private final MovieEsRestService movieEsRestService;

  @GetMapping("/movie/info")
  public String info() {
    return "Hello from Loader movie app";
  }

  @GetMapping("/movie/query")
  public List<Movie> findMovies(@RequestParam String title) throws IOException {
    return movieEsRestService.searchMovie(title);
  }

  @GetMapping("/movie/count-by-year")
  public List<? extends Terms.Bucket> countByYear() throws IOException {
    return movieEsRestService.countByYear();
  }

  @DeleteMapping("/movie")
  public Response deleteIndices() throws IOException {
    boolean deleteMovies = movieEsService.deleteIndex("movies");
    boolean deleteMoviesYear = movieEsService.deleteIndex("movies-year");
    return Response.ok().entity(ImmutableMap.of("movies", deleteMovies, "movies-year", deleteMoviesYear)).build();
  }
}
