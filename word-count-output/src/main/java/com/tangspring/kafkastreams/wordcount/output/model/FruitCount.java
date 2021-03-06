package com.tangspring.kafkastreams.wordcount.output.model;

import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FruitCount {
  private String name;
  private Long count;

  public static FruitCount from(String json) {
    return JacksonUtil.fromJson(json, FruitCount.class);
  }
}
