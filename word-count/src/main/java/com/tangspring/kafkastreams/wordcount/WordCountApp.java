package com.tangspring.kafkastreams.wordcount;

import java.util.Arrays;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class WordCountApp {

  private static final String WORD_COUNT_INPUT = "word-count-input";

  public static void main(String[] args) {
    WordCountApp wordCountApp = new WordCountApp();
    KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), wordCountApp.getKafkaProperties());
    streams.start();

    // shutdown hook to correctly close the streams application
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  private Topology createTopology() {
    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> textLines = builder.stream(WORD_COUNT_INPUT);

    Serde<String> stringSerde = Serdes.String();
    Serde<Long> longSerde = Serdes.Long();

    log.info("Start processing stream topic: {}", WORD_COUNT_INPUT);

    textLines
        .mapValues((ValueMapper<String, String>) String::toLowerCase)
        .flatMapValues(textLine -> Arrays.asList(textLine.split("\\W+")))
        .selectKey((key, word) -> word)
        .groupByKey()
        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("Counts").withKeySerde(stringSerde).withValueSerde(longSerde))
        .toStream()
        .to("word-count-output", Produced.with(stringSerde, longSerde));

    return builder.build();
  }

  private Properties getKafkaProperties() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
//        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv("KAFKA_SERVER"));
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    return config;
  }
}
