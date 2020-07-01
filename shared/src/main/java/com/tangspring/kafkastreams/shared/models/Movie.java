package com.tangspring.kafkastreams.shared.models;

import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
  private String movieid;
  private String title;
  private String cntitle;
  private Integer year;
  private Float rating;
  private String genre;
  private Integer runtime;    // min
  private String country;
  private String source;
  private String filetype;
  private String poster;
  private String fileurl;
  private String actors;
  private String plot;
  private String awards;
  private Float boxoffice;
  private LocalDateTime timein;
  private String doubanId;
  private String imdbid;
  private String tmdbid;
  private String omdbid;

  public static Movie from(String json) {
    return JacksonUtil.fromJson(json, Movie.class);
  }
}
