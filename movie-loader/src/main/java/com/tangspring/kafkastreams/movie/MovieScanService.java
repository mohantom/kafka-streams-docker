package com.tangspring.kafkastreams.movie;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.SequenceWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.models.OmdbMovie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class MovieScanService {

  private static final String MOVIE_DRIVE = "Y:\\";
  private static final Pattern PATTERN = Pattern.compile("(\\d{4})");
  private static final Pattern PATTERN_RATING = Pattern.compile("(\\d\\.\\d)");
  private static final String[] apikeys = {"80bb7f52", "f98d8087", "e76a5722", "c8b0a93e"};
  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey={apikey}&t={title}&y={year}";
  private static final String MOVIES_FILENAME = "movies.txt";
  private static final String MOVIE_ENRICHED_FILENAME = "movies_enriched.csv";
  private static final String MOVIE_UNFOOUND_FILENAME = "movies_unfound.csv";

  public static final List<String> MOVIE_FOLDERS = ImmutableList.of(
      "2020 New",
      "Action 动作战争",
      "Animation 动画家庭",
      "Comedy 喜剧爱情",
      "Drama 剧情惊悚"
  );

  private static final Set<String> MOVIES_KEEP_NUMBER = ImmutableSet.of(
      "Deadpool 2",
      "Taken 2",
      "Iron Man 3",
      "Kung Fu Panda 3",
      "Ted 2",
      "Mesrine Part 2",
      "Ocean's 8",
      "Fantastic 4",
      "The 33"
  );
  public static final String[] EXTENSIONS = {"mkv", "mp4", "avi"};

  private final RestTemplate restTemplate;
  private final String outputFolder;


  public String scanNasMovies() {
    List<String> movies = MOVIE_FOLDERS.stream()
        .map(folder -> new File(MOVIE_DRIVE, folder))
        .map(f -> FileUtils.listFiles(f, EXTENSIONS, false))
        .flatMap(Collection::stream)
        .map(File::getAbsolutePath)
        .collect(Collectors.toList());

    File moviesFile = new File(outputFolder, MOVIES_FILENAME);

    try {
      FileUtils.writeLines(moviesFile, movies);
    } catch (IOException ie) {
      log.error("Failed to write to file", ie);
    }

    return "Finished collecting movie files from NAS";
  }

  public List<Movie> enrichMovies() {
    List<String> movieLines = readFromFile();

    Set<String> moviesTitlesHavingImdbId = getAlreadyEnrichedMovies();
    Set<String> moviesTitlesUnfound = getUnfoundMovies();
    List<Movie> allMovies = getAllMovies(movieLines);

    List<Movie> moviesTobeEnriched = allMovies.stream()
        .filter(m -> !moviesTitlesHavingImdbId.contains(m.getTitle()))  // already enriched
        .filter(m -> !moviesTitlesUnfound.contains(m.getTitle()))       // already checked it does not exist on OMDB
        .filter(m -> StringUtils.isNotBlank(m.getTitle()) && m.getYear() != null) // exclude cn movies (title is null) for now
        .collect(Collectors.toList());
    log.info("Found {} movies to be enriched.", moviesTobeEnriched.size());

    Pair<List<Movie>, List<Movie>> enrichedAndUnfoundMovies = enrichMovies(moviesTobeEnriched);

    createCSVFile(new File(outputFolder, MOVIE_ENRICHED_FILENAME), enrichedAndUnfoundMovies.getLeft());
    createCSVFile(new File(outputFolder, MOVIE_UNFOOUND_FILENAME), enrichedAndUnfoundMovies.getRight());

    log.info("Finished scanning {} movies.", enrichedAndUnfoundMovies.getLeft().size());
    return enrichedAndUnfoundMovies.getLeft().stream().limit(10).collect(Collectors.toList());
  }

  private List<Movie> getAllMovies(List<String> movieLines) {
    List<Movie> allMovies = movieLines.stream()
        .map(this::extractMovie)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    log.info("Total {} movies.", allMovies.size());
    return allMovies;
  }

  private Set<String> getUnfoundMovies() {
    Set<String> moviesTitlesUnfound = loadMoviesFromCsv(MOVIE_UNFOOUND_FILENAME).stream()
        .map(Movie::getTitle)
        .collect(Collectors.toSet());
    log.info("There are {} movies unfound.", moviesTitlesUnfound.size());
    return moviesTitlesUnfound;
  }

  private Set<String> getAlreadyEnrichedMovies() {
    Set<String> moviesTitlesHavingImdbId = loadMoviesFromCsv(MOVIE_ENRICHED_FILENAME).stream()
        .filter(this::hasImdbId)
        .map(Movie::getTitle)
        .collect(Collectors.toSet());
    log.info("Found {} movies already enriched.", moviesTitlesHavingImdbId.size());
    return moviesTitlesHavingImdbId;
  }

  private List<Movie> loadMoviesFromCsv(String filename) {
    File file = new File(outputFolder, filename);
    if (!file.exists()) {
      return ImmutableList.of();
    }
    String filepath = new File(outputFolder, filename).getAbsolutePath();
    return JacksonUtil.readCsvFile(filepath, Movie.class);
  }

  private boolean hasImdbId(Movie m) {
    return StringUtils.isNotBlank(m.getImdbid());
  }

  private List<String> readFromFile() {
    File moviesFile = new File(outputFolder, MOVIES_FILENAME);

    try {
      return FileUtils.readLines(moviesFile, UTF_8);
    } catch (Exception e) {
      log.error("Failed to read file.");
      throw new RuntimeException("Failed to read movie file.");
    }
  }

  private Movie extractMovie(String filepath) {
    String filename = FilenameUtils.getName(filepath);
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
        .fileurl(filepath)
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
