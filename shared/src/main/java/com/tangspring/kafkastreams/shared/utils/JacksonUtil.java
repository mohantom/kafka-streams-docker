package com.tangspring.kafkastreams.shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JacksonUtil {

  private static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModule(new JavaTimeModule());
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public String toJson(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.warn("Problem to convert to json string: ", e);
      return null;
    }
  }

  public <T> T fromJson(String source, Class<T> clazz) {
    try {
      return objectMapper.readValue(source, clazz);
    } catch (IOException e) {
      log.warn("Problem to convert to object {}: ", clazz.getSimpleName(), e);
      return null;
    }
  }
}
