package com.tangspring.kafkastreams.movie;

import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieCountYear {
  private String year;
  private Long count;

  public static MovieCountYear from(String json) {
    return JacksonUtil.fromJson(json, MovieCountYear.class);
  }
}
