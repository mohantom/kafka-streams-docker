package com.tangspring.kafkastreams.wordcount.output.model;

import com.tangspring.kafkastreams.wordcount.output.JacksonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FruitCount {
  private String name;
  private Long count;

  @SneakyThrows
  public static FruitCount from(String json) {
    return JacksonUtil.getObjectMapper().readValue(json, FruitCount.class);
  }
}
