package com.tangspring.kafkastreams.movie;

import com.tangspring.kafkastreams.shared.models.Movie;
import com.tangspring.kafkastreams.shared.utils.JacksonUtil;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
@AllArgsConstructor
public class MovieStreamsService {

  private static final String MOVIE_TOPIC = "movies";

  private final Properties kafkaProperties;

  public void processMovies() {
    KafkaStreams streams = new KafkaStreams(createTopology(), kafkaProperties);
    streams.start();

    // shutdown hook to correctly close the streams application
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  private Topology createTopology() {
    StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> textLines = builder.stream(MOVIE_TOPIC);

    Serde<String> stringSerde = Serdes.String();
    Serde<Long> longSerde = Serdes.Long();

    log.info("Start processing stream topic: {}", MOVIE_TOPIC);

    textLines
        .mapValues(movieStr -> JacksonUtil.fromJson(movieStr, Movie.class))
        .selectKey((key, movie) -> String.valueOf(movie.getYear()))
        .mapValues(JacksonUtil::toJson)
        .groupByKey()
        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("Counts").withKeySerde(stringSerde).withValueSerde(longSerde))
        .toStream()
        .to("movies-year", Produced.with(stringSerde, longSerde));

    return builder.build();
  }

}
