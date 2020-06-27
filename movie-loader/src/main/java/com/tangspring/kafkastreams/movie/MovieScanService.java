package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.models.OmdbMovie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class MovieScanService {
  private static final Pattern PATTERN = Pattern.compile("(\\d{4})");
  private static final Pattern PATTERN_RATING = Pattern.compile("(\\d\\.\\d)");
  private static final String DEFAULT_YEAR_RATING = "0000";
//  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey=80bb7f52&t={title}&y={year}";
//  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey=f98d8087&t={title}&y={year}";
//  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey=e76a5722&t={title}&y={year}";
  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey=c8b0a93e&t={title}&y={year}";
  private static final String MOVIE_ENRICHED_FILENAME = "movies_enriched.csv";
  private static final String MOVIE_UNFOOUND_FILENAME = "movies_unfound.csv";

  private final RestTemplate restTemplate;
  private final String outputFolder;

  public List<Movie> scanMovies(boolean append) {
    List<String> movieLines = readFromFile("/movies.txt");

    Set<String> moviesTitlesHavingImdbId = loadMoviesFromCsv(null).stream()
        .filter(this::hasImdbId)
        .map(Movie::getTitle)
        .collect(Collectors.toSet());
    log.info("Found {} movies already enriched.", moviesTitlesHavingImdbId.size());

    Set<String> moviesTitlesUnfound = loadMoviesFromCsv(Paths.get(outputFolder, MOVIE_UNFOOUND_FILENAME).toString()).stream()
        .map(Movie::getTitle)
        .collect(Collectors.toSet());
    log.info("There are {} movies unfound.", moviesTitlesUnfound.size());

    List<Movie> allMovies = movieLines.stream()
        .map(this::extractMovie)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    log.info("Total {} movies.", allMovies.size());

    List<Movie> moviesTobeEnriched = allMovies.stream()
        .filter(m -> !moviesTitlesHavingImdbId.contains(m.getTitle()))  // already enriched
        .filter(m -> !moviesTitlesUnfound.contains(m.getTitle()))       // already checked it does not exist on OMDB
        .filter(m -> StringUtils.isNoneBlank(m.getTitle(), m.getYear()))
        .collect(Collectors.toList());
    log.info("Found {} movies to be enriched.", moviesTobeEnriched.size());

    Pair<List<Movie>, List<Movie>> enrichedAndUnfoundMovies = enrichMovies(moviesTobeEnriched);


    createCSVFile(new File(outputFolder, MOVIE_ENRICHED_FILENAME), enrichedAndUnfoundMovies.getLeft(), append);
    createCSVFile(new File(outputFolder, MOVIE_UNFOOUND_FILENAME), enrichedAndUnfoundMovies.getRight(), append);

    return enrichedAndUnfoundMovies.getLeft().stream().limit(10).collect(Collectors.toList());
  }

  private List<Movie> loadMoviesFromCsv(String filepath) {
    if (filepath == null) {
      filepath = Paths.get(outputFolder, MOVIE_ENRICHED_FILENAME).toString();
    }
    return JacksonUtil.readCsvFile(filepath, Movie.class);
  }

  private boolean hasImdbId(Movie m) {
    return StringUtils.isNotBlank(m.getImdbid());
  }

  private List<String> scanFolder(String folderpath) {
    try {
      return Files.walk(Paths.get(folderpath))
          .filter(Files::isRegularFile)
          .map(p -> p.toFile().getName())
          .filter(n -> StringUtils.endsWithAny(".mkv", ".mp4", ".avi"))
          .collect(Collectors.toList());
    } catch (IOException ie) {
      log.error("Failed to scan folder.", ie);
      return ImmutableList.of();
    }
  }

  private List<String> readFromFile(String filepath) {
    List<String> result = new ArrayList<>();
    // had to use for docker: getResourceAsStream()
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(filepath)))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        result.add(line);
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to read file: {}", filepath);
      throw new RuntimeException("Failed to read movie file.");
    }
  }

  private Movie extractMovie(String l) {
    Matcher matcher = PATTERN.matcher(l);
    String year = matcher.find(4) ? matcher.group(0) : DEFAULT_YEAR_RATING;

    String title = StringUtils.trim(StringUtils.substringBeforeLast(l, year));

    String enTitle = title.matches(".*[^\\x00-\\x7F].*") ? StringUtils.trim(StringUtils.substringBeforeLast(title, " ")) : title;
    String cnTitle = StringUtils.trim(StringUtils.substringAfter(title, enTitle));

    if (enTitle.matches(".*\\s\\d\\w?") || enTitle.matches(".*\\s\\d\\.\\d")) {  // Terminator 2, Terminator 3b, Terminator 7.5
      enTitle = StringUtils.trim(StringUtils.substringBeforeLast(enTitle, " "));
    }

    if (enTitle.length() <= 1) { // cn movies
      enTitle = null;
    }

    if (StringUtils.isAnyBlank(enTitle, year)) {
      log.warn("Missing title or year: {}", l);
    }

    Matcher matcherRating = PATTERN_RATING.matcher(l);
    String rating = matcherRating.find() ? matcherRating.group(0) : DEFAULT_YEAR_RATING;

    String afterRating = StringUtils.trim(StringUtils.substringAfter(l, rating));
    String genre = StringUtils.substringBefore(afterRating, " ");

    return  Movie.builder()
            .title(enTitle)
            .cntitle(cnTitle)
            .genre(genre)
            .year(year)
            .rating(rating)
            .build();
  }

  private Pair<List<Movie>, List<Movie>> enrichMovies(List<Movie> movies) {
    List<Movie> enrichedMovies = new ArrayList<>();
    List<Movie> unfoundMovies = new ArrayList<>();
    for (Movie m : movies) {
      Map<String, String> uriVars = ImmutableMap.of("title", m.getTitle(), "year", m.getYear());
      try {
        OmdbMovie omdbMovie = restTemplate.getForObject(omdbBaseUrl, OmdbMovie.class, uriVars);

        if (omdbMovie == null || omdbMovie.getTitle() == null) {
          unfoundMovies.add(m);
          continue;
        }

        Movie enrichedMovie = m.toBuilder()
            .runtime(getRuntime(omdbMovie))
            .rating(omdbMovie.getImdbRating())
            .genre(omdbMovie.getGenre())
            .actors(omdbMovie.getActors())
            .plot(omdbMovie.getPlot())
            .poster(omdbMovie.getPoster())
            .country(omdbMovie.getCountry())
            .imdbid(omdbMovie.getImdbID())
            .build();

        enrichedMovies.add(enrichedMovie);

      } catch (Exception e) {
        log.error("Problem from OMDB api.", e); // Request limit reached!
        break;
      }
    }

    return Pair.of(enrichedMovies, unfoundMovies);

  }

  private int getRuntime(OmdbMovie omdbMovie) {
    try {
      return Integer.parseInt(StringUtils.substringBefore(omdbMovie.getRuntime(), " min"));
    } catch (Exception e) {
      log.warn("No runtime available for {}: {}", omdbMovie.getTitle(), omdbMovie.getRuntime());
      return 0;
    }
  }

  private void createCSVFile(File csvFile, List<Movie> movies, boolean append) {
    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(csvFile, append))) {
      SequenceWriter writer = JacksonUtil.getCsvWriter(Movie.class, append).writeValues(fos);
      writer.writeAll(movies);
    } catch (IOException ie) {
      log.error("Failed to write movies to csv file");
    }
  }
}
