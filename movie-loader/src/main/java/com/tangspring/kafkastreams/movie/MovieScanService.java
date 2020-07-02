package com.tangspring.kafkastreams.movie;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import java.util.Optional;
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
  private static final String[] apikeys = {"80bb7f52", "f98d8087", "e76a5722", "c8b0a93e"};
  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey={apikey}&t={title}&y={year}";
  private static final String MOVIE_ENRICHED_FILENAME = "movies_enriched.csv";
  private static final String MOVIE_UNFOOUND_FILENAME = "movies_unfound.csv";

  private static final Set<String> MOVIES_KEEP_NUMBER = ImmutableSet.of(
      "Deadpool 2",
      "Taken 2",
      "Iron Man 3",
      "Kung Fu Panda 3",
      "Ted 2",
      "Mesrine Part 2",
      "Ocean's 8",
      "Fantastic 4"
  );

  private final RestTemplate restTemplate;
  private final String outputFolder;

  public List<Movie> scanMovies() {
    List<String> movieLines = readFromFile("/movies.txt");

    Set<String> moviesTitlesHavingImdbId = loadMoviesFromCsv(MOVIE_ENRICHED_FILENAME).stream()
        .filter(this::hasImdbId)
        .map(Movie::getTitle)
        .collect(Collectors.toSet());
    log.info("Found {} movies already enriched.", moviesTitlesHavingImdbId.size());

    Set<String> moviesTitlesUnfound = loadMoviesFromCsv(MOVIE_UNFOOUND_FILENAME).stream()
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
        .filter(m -> StringUtils.isNotBlank(m.getTitle()) && m.getYear() != null) // exclude cn movies (title is null) for now
        .collect(Collectors.toList());
    log.info("Found {} movies to be enriched.", moviesTobeEnriched.size());

    Pair<List<Movie>, List<Movie>> enrichedAndUnfoundMovies = enrichMovies(moviesTobeEnriched);

    createCSVFile(new File(outputFolder, MOVIE_ENRICHED_FILENAME), enrichedAndUnfoundMovies.getLeft());
    createCSVFile(new File(outputFolder, MOVIE_UNFOOUND_FILENAME), enrichedAndUnfoundMovies.getRight());

    log.info("Finished scan {} movies.", enrichedAndUnfoundMovies.getLeft().size());
    return enrichedAndUnfoundMovies.getLeft().stream().limit(10).collect(Collectors.toList());
  }

  private List<Movie> loadMoviesFromCsv(String filename) {
    String filepath = new File(outputFolder, filename).getAbsolutePath();
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

  private Movie extractMovie(String filename) {
    String year = extractMovieYear(filename);

    String title = StringUtils.trim(StringUtils.substringBeforeLast(filename, year));

    String enTitle = title.matches(".*[^\\x00-\\x7F].*") ? StringUtils.trim(StringUtils.substringBeforeLast(title, " ")) : title;
    String cnTitle = StringUtils.trim(StringUtils.substringAfter(title, enTitle));

    if (!MOVIES_KEEP_NUMBER.contains(enTitle) && (enTitle.matches(".*\\s\\d\\w?") || enTitle
        .matches(".*\\s\\d\\.\\d"))) {  // Terminator 2, Terminator 3b, Terminator 7.5
      enTitle = StringUtils.trim(StringUtils.substringBeforeLast(enTitle, " "));
    }

    if (enTitle.length() <= 1) { // cn movies
      enTitle = null;
    }

    if (StringUtils.isAnyBlank(enTitle, year)) {
      log.warn("Missing title or year: {}", filename);
    }

    Matcher matcherRating = PATTERN_RATING.matcher(filename);
    String rating = matcherRating.find() ? matcherRating.group(0) : null;

    String afterRating = StringUtils.trim(StringUtils.substringAfter(filename, rating));
    String genre = StringUtils.substringBefore(afterRating, " ");

    return Movie.builder()
        .title(enTitle)
        .cntitle(cnTitle)
        .genre(genre)
        .year(Optional.ofNullable(year).map(Integer::valueOf).orElse(null))
        .rating(Optional.ofNullable(rating).map(Float::valueOf).orElse(null))
        .build();
  }

  private String extractMovieYear(String l) {
    String year = null;
    String[] parts = l.split(" ");
    for (int i = 0; i < parts.length; i++) {
      if (parts[i].matches("\\d{4}")) {
        year = parts[i];
      }
    }
    return year;
  }

  private Pair<List<Movie>, List<Movie>> enrichMovies(List<Movie> movies) {
    List<Movie> enrichedMovies = new ArrayList<>();
    List<Movie> unfoundMovies = new ArrayList<>();

    int tries = 0;

    for (Movie m : movies) {
      Map<String, Object> uriVars = getUriVars(m, apikeys[tries]);
      try {
        OmdbMovie omdbMovie = restTemplate.getForObject(omdbBaseUrl, OmdbMovie.class, uriVars);

        Integer runtime = getRuntime(omdbMovie);
        Float rating = getRating(m, omdbMovie);

        if (omdbMovie == null || omdbMovie.getTitle() == null || runtime == null || rating == null) {
          unfoundMovies.add(m);
          continue;
        }

        Movie enrichedMovie = m.toBuilder()
            .title(omdbMovie.getTitle())
            .runtime(runtime)
            .rating(rating)
            .genre(omdbMovie.getGenre())
            .actors(omdbMovie.getActors())
            .plot(omdbMovie.getPlot())
            .poster(omdbMovie.getPoster())
            .country(omdbMovie.getCountry())
            .awards(omdbMovie.getAwards())
            .boxoffice(getBoxOffice(omdbMovie))
            .imdbid(omdbMovie.getImdbID())
            .build();

        enrichedMovies.add(enrichedMovie);

      } catch (Exception e) {
        log.error("Problem from OMDB api. Limit exceeded? ", e); // Request limit reached!
        tries++;
      }
    }

    return Pair.of(enrichedMovies, unfoundMovies);

  }

  private Float getBoxOffice(OmdbMovie omdbMovie) {
    try {
      String boxOffice = omdbMovie.getBoxOffice();
      if (boxOffice == null || "N/A".equals(boxOffice)) {
        return null;
      }
      return Float.valueOf(boxOffice.replace("$", "").replace(",", ""));
    } catch (Exception e) {
      log.warn("Failed to extract box office for {}", omdbMovie.getTitle(), e);
      return null;
    }
  }

  private Map<String, Object> getUriVars(Movie m, String apikey) {
    return ImmutableMap.of(
        "apikey", apikey,
        "title", m.getTitle(),
        "year", m.getYear()
    );
  }

  private Float getRating(Movie m, OmdbMovie omdbMovie) {
    try {
      String rating = omdbMovie.getImdbRating();
      if (rating == null || "N/A".equals(rating)) {
        return null;
      }
      return Float.valueOf(rating);
    } catch (Exception e) {
      log.warn("Failed to extract rating for {}", omdbMovie.getTitle(), e);
      return m.getRating();
    }
  }

  private Integer getRuntime(OmdbMovie omdbMovie) {
    try {
      return Integer.valueOf(StringUtils.substringBefore(omdbMovie.getRuntime(), " min"));
    } catch (Exception e) {
      log.warn("No runtime available for {}: {}", omdbMovie.getTitle(), omdbMovie.getRuntime());
      return null;
    }
  }

  private void createCSVFile(File csvFile, List<Movie> movies) {
    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(csvFile, true))) {
      SequenceWriter writer = JacksonUtil.getCsvWriter(Movie.class, true).writeValues(fos);
      writer.writeAll(movies);
    } catch (IOException ie) {
      log.error("Failed to write movies to csv file");
    }
  }
}
