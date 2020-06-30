package com.tangspring.kafkastreams.shared.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JacksonUtil {

  private static final ObjectMapper objectMapper;
  private static final CsvMapper csvMapper;

  static {
    objectMapper = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        .registerModule(new JavaTimeModule());

    csvMapper = new CsvMapper();
    csvMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
        .disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
        .registerModule(new JavaTimeModule());
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public CsvMapper getCsvMapper() {
    return csvMapper;
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

  public <T> List<T> readCsvFile(String filepath, Class<T> clazz) {
    if (!(new File(filepath)).exists()) {
      return ImmutableList.of();
    }

    try (InputStream is = new FileInputStream(filepath)) {
      return readFromInputStream(is, clazz);
    } catch (Exception e) {
      log.warn("Could not read from file {}", filepath, e);
      return ImmutableList.of();
    }
  }

  public <T> List<T> readFromInputStream(InputStream inputStream, Class<T> clazz) throws IOException {
    ObjectReader csvReader = getCsvReader(clazz);
    MappingIterator<T> it = csvReader.readValues(inputStream);
    return it.readAll();
  }

  private <T> ObjectReader getCsvReader(Class<T> clazz) {
    return csvMapper.readerFor(clazz).with(getSchemaWithHeader(clazz));
  }

  public <T> ObjectWriter getCsvWriter(Class<T> clazz, boolean append) {
    CsvSchema schema = append ? getSchema(clazz) : getSchemaWithHeader(clazz);
    return csvMapper.writer(schema);
  }

  private <T> CsvSchema getSchemaWithHeader(Class<T> clazz) {
    return getSchema(clazz).withHeader();
  }

  private <T> CsvSchema getSchema(Class<T> clazz) {
    return csvMapper.schemaFor(clazz);
  }
}
