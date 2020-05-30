package com.tangspring.kafkastreams.join;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;

@Slf4j
public class UserEventEnricherApp {

  public static void main(String[] args) {
    StreamsBuilder builder = new StreamsBuilder();

    // we get a global table out of Kafka. This table will be replicated on each Kafka Streams application
    // the key of our globalKTable is the user ID
    GlobalKTable<String, String> usersGlobalTable = builder.globalTable("user-table");

    // we get a stream of user purchases
    KStream<String, String> userPurchases = builder.stream("user-purchases");

    // we want to enrich that stream
    KStream<String, String> userPurchasesEnrichedJoin =
        userPurchases.join(usersGlobalTable,
            (key, value) -> key, /* map from the (key, value) of this stream to the key of the GlobalKTable */
            (userPurchase, userInfo) -> "Purchase=" + userPurchase + ", UserInfo=[" + userInfo + "]"
        );

    userPurchasesEnrichedJoin.to("user-purchases-enriched-inner-join");

    // we want to enrich that stream using a Left Join
    KStream<String, String> userPurchasesEnrichedLeftJoin =
        userPurchases.leftJoin(usersGlobalTable,
            (key, value) -> key, /* map from the (key, value) of this stream to the key of the GlobalKTable */
            (userPurchase, userInfo) -> {
              // as this is a left join, userInfo can be null
              return userInfo == null ? "Purchase=" + userPurchase + ", UserInfo=null" :
                  "Purchase=" + userPurchase + ", UserInfo=[" + userInfo + "]";
            }
        );

    userPurchasesEnrichedLeftJoin.to("user-purchases-enriched-left-join");

    setupStreams(builder.build());
  }

  private static void setupStreams(Topology topology) {
    KafkaStreams streams = new KafkaStreams(topology, getProperties());
    streams.cleanUp(); // only do this in dev - not in prod
    streams.start();

    // print the topology
    streams.localThreadsMetadata().forEach(data -> log.info("topology: {}", data));

    // shutdown hook to correctly close the streams application
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  private static Properties getProperties() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "user-event-enricher-app");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return config;
  }
}
