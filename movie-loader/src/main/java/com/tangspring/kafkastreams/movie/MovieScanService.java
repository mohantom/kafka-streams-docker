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
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class MovieScanService {
  private static final Pattern PATTERN = Pattern.compile("(\\d{4})");
  private static final Pattern PATTERN_RATING = Pattern.compile("(\\d\\.\\d)");
  private static final String DEFAULT_YEAR_RATING = "0000";
  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey=80bb7f52&t={title}&y={year}";
  private static final String MOVIE_ENRICHED_FILENAME = "movies_enriched.csv";

  private final RestTemplate restTemplate;
  private final String outputFolder;

  public List<Movie> scanMovies() {
//    List<String> movieFiles = scanFolder("Y:\\Action 动作战争");

    List<String> movieLines = readFromFile("/movies.txt");

    Set<String> moviesTitlesHavingImdbId = loadEnrichedMovies().stream()
        .filter(this::hasImdbId)
        .map(Movie::getTitle)
        .collect(Collectors.toSet());

    List<Movie> movies = movieLines.stream()
        .map(this::extractMovie)
        .filter(Objects::nonNull)
        .filter(m -> !moviesTitlesHavingImdbId.contains(m.getTitle()))
        .limit(500)
        .map(this::enrichMovie)
        .filter(this::hasImdbId)
        .collect(Collectors.toList());

    int size = movies.size();
    log.info("Parsed {} movies.", size);

    createCSVFile(new File(outputFolder, MOVIE_ENRICHED_FILENAME), movies);

    return movies.stream().limit(10).collect(Collectors.toList());
  }

  public List<Movie> loadEnrichedMovies() {
    return JacksonUtil.readCsvFile(Paths.get(outputFolder, MOVIE_ENRICHED_FILENAME).toString(), Movie.class);
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

  private Movie enrichMovie(Movie m) {
    Map<String, String> uriVars = ImmutableMap.of(
        "title", m.getTitle(),
        "year", m.getYear()
    );
    OmdbMovie omdbMovie = restTemplate.getForObject(omdbBaseUrl, OmdbMovie.class, uriVars);

    return m.toBuilder()
        .runtime(getRuntime(omdbMovie))
        .rating(omdbMovie.getImdbRating())
        .genre(omdbMovie.getGenre())
        .actors(omdbMovie.getActors())
        .plot(omdbMovie.getPlot())
        .poster(omdbMovie.getPoster())
        .country(omdbMovie.getCountry())
        .imdbid(omdbMovie.getImdbID())
        .build();
  }

  private int getRuntime(OmdbMovie omdbMovie) {
    try {
      return Integer.parseInt(StringUtils.substringBefore(omdbMovie.getRuntime(), " min"));
    } catch (Exception e) {
      log.warn("No runtime available: {}", omdbMovie.getRuntime());
      return 0;
    }
  }

  private void createCSVFile(File csvFile, List<Movie> movies) {
    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(csvFile, true))) {
      SequenceWriter writer = JacksonUtil.getCsvWriter(Movie.class).writeValues(fos);
      writer.writeAll(movies);
    } catch (IOException ie) {
      log.error("Failed to write movies to csv file");
    }
  }
}
