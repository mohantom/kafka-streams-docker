package com.tangspring.kafkastreams.shared.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmdbMovie {
  @JsonProperty("Title")
  private String title; // "Now",

  @JsonProperty("Year")
  private String year; // "2016",

  @JsonProperty("Released")
  private String released; // "27 Jun 2016",

  @JsonProperty("Runtime")
  private String runtime; // "10 min",

  @JsonProperty("Genre")
  private String genre; // "Short, Drama",

  @JsonProperty("Director")
  private String director; // "Paul Kelly",

  @JsonProperty("Writer")
  private String writer; // "Paul Kelly",

  @JsonProperty("Actors")
  private String actors; // "Judith Roberts",

  @JsonProperty("Plot")
  private String plot; // "Lucid intervals of existential consciousness occupy a woman's thoughts more than she thinks.",

  @JsonProperty("Language")
  private String language; // "English",

  @JsonProperty("Country")
  private String country; // "USA",

  @JsonProperty("Awards")
  private String awards; // "2 wins.",

  @JsonProperty("Poster")
  private String poster; // "https://m.media-amazon.com/images/M/MV5BYjUwYWQ0ZTMtYWE2MC00YjE1LWJhYzktNjFiOTM3ZTZiYmZkXkEyXkFqcGdeQXVyMjM5MjA0NDQ@._V1_SX300.jpg",

  @JsonProperty("Ratings")
  private List<Rating> ratings; // [ ],

  @JsonProperty("Metascore")
  private String metascore; // "N/A",

  private String imdbRating; // "N/A",
  private String imdbVotes; // "N/A",
  private String imdbID; // "tt5554584",

  @JsonProperty("Type")
  private String type; // "movie",

  @JsonProperty("DVD")
  private String dvd; // "N/A",

  @JsonProperty("BoxOffice")
  private String boxOffice; // "N/A",

  @JsonProperty("Production")
  private String production; // "N/A",

  @JsonProperty("Website")
  private String website; // "N/A",

  @JsonProperty("Response")
  private String response; // "True"

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Rating {
    @JsonProperty("Source")
    private String source;

    @JsonProperty("Value")
    private String value;
  }
}
