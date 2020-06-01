package com.tangspring.kafkastreams.wordcount.output.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FruitCount {
  private String id;
  private String name;
  private Long count;
}
