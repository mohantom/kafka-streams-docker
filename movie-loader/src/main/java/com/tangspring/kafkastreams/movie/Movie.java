package com.tangspring.kafkastreams.movie;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
  private String movieid;
  private String title;
  private String cntitle;
  private int year;
  private double rating;
  private String genre;
  private int runtime;    // min
  private String country;
  private String source;
  private String filetype;
  private String poster;
  private String fileurl;
  private String actors;
  private String plot;
  private LocalDateTime timein;
  private String doubanId;
  private String imdbid;
  private String tmdbid;
  private String omdbid;
}
