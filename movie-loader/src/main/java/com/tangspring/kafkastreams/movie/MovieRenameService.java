package com.tangspring.kafkastreams.movie;

import com.google.common.collect.ImmutableMap;
import com.tangspring.kafkastreams.shared.models.OmdbMovie;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
@AllArgsConstructor
public class MovieRenameService {

  private static final Pattern PATTERN = Pattern.compile("(\\d{4})");
  private static final String[] apikeys = {"80bb7f52", "f98d8087", "e76a5722", "c8b0a93e"};
  private static final String omdbBaseUrl = "http://www.omdbapi.com/?apikey={apikey}&t={title}&y={year}";
  public static final String VIDEO_FOLDER = "D:\\Movie";

  private final RestTemplate restTemplate;

  public void renameMovies() {
    File folder = new File(VIDEO_FOLDER);
    File[] files = folder.listFiles(f -> f.getName().endsWith(".mp4") || f.getName().endsWith(".mkv"));
    Arrays.stream(files)
        .forEach(file -> {
          String filename = file.getName();

          String year = null;
          Matcher matcher = PATTERN.matcher(filename);
          if (matcher.find()) {
            year = matcher.group();
          }

          String title = StringUtils.substringBefore(filename, year)
              .replace(".", " ").replace("(", "").trim();

          Map<String, Object> uriVars = ImmutableMap.of(
              "apikey", apikeys[0],
              "title", title,
              "year", year
          );

          try {
            OmdbMovie omdbMovie = restTemplate.getForObject(omdbBaseUrl, OmdbMovie.class, uriVars);
            if (omdbMovie == null || omdbMovie.getImdbID() == null) {
              return;
            }

            String newBasename = getNewFileBasename(omdbMovie);

            String parent = file.getParent();
            FileUtils.moveFile(file, new File(parent, newBasename + ".mp4")); // rename

            File posterFile = findFile(file, parent, ".jpg");
            if (posterFile.exists()) {
              FileUtils.moveFile(posterFile, new File(parent, newBasename + ".jpg")); // rename
            } else {
              byte[] posterBytes = restTemplate.getForObject(omdbMovie.getPoster(), byte[].class);
              FileUtils.writeByteArrayToFile(new File(parent, newBasename + ".jpg"), posterBytes);
            }

            File subFile = findFile(file, parent, ".srt");
            if (subFile.exists()) {
              FileUtils.moveFile(subFile, new File(parent, newBasename + ".srt")); // rename
            }

          } catch (Exception e) {
            log.info("Problem calling OMDB");
          }
        });

  }

  private String getNewFileBasename(OmdbMovie omdbMovie) {
    return String.format("%s %s %s %s",
                  omdbMovie.getTitle().replace(":", ""),
                  omdbMovie.getYear(),
                  omdbMovie.getImdbRating(),
                  StringUtils.split(omdbMovie.getGenre(), ",")[0]);
  }

  private File findFile(File file, String parent, String ext) {
    return new File(parent, FilenameUtils.getBaseName(file.getName()) + ext);
  }
}
