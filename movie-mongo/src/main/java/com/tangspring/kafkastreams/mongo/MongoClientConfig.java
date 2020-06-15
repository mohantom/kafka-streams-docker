package com.tangspring.kafkastreams.mongo;

import static java.util.Collections.singletonList;

import com.mongodb.MongoClientSettings.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoClientConfig extends AbstractMongoClientConfiguration {

  @Value("${spring.mongo.host}")
  private String mongoHost;

  @Value("${spring.mongo.port}")
  private int mongoPort;

  @Override
  public String getDatabaseName() {
    return "demo";
  }

  @Override
  protected void configureClientSettings(Builder builder) {

    builder
        .credential(MongoCredential.createCredential("root", "admin", "example".toCharArray()))
        .applyToClusterSettings(settings -> settings.hosts(singletonList(new ServerAddress(mongoHost, mongoPort))));
  }
}
