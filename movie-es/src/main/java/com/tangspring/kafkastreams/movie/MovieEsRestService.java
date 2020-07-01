package com.tangspring.kafkastreams.movie;

import com.tangspring.kafkastreams.shared.models.Movie;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

@Slf4j
@AllArgsConstructor
public class MovieEsRestService {
  private final RestHighLevelClient esClient;

  public List<Movie> searchMovie(String title) throws IOException {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
        .query(QueryBuilders.matchQuery("title", title))
        .from(0)
        .size(20)
        .sort(new FieldSortBuilder("year").order(SortOrder.DESC))
        .timeout(new TimeValue(60, TimeUnit.SECONDS));
    SearchRequest searchRequest = new SearchRequest("movies").source(searchSourceBuilder);

    SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

    List<Movie> movies = Arrays.stream(searchResponse.getHits().getHits())
        .map(SearchHit::getSourceAsString)
        .map(Movie::from)
        .collect(Collectors.toList());
    return movies;
  }

  public List<? extends Terms.Bucket> countByYear() throws IOException {
    TermsAggregationBuilder aggregationBuilder = AggregationBuilders
        .terms("countByYear").field("year")
        .order(BucketOrder.key(false))
        .subAggregation(AggregationBuilders.sum("boxoffice").field("boxoffice"))
        .subAggregation(AggregationBuilders.count("count").field("year"));

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
        .aggregation(aggregationBuilder)
        .from(0)
        .size(20);

    SearchRequest searchRequest = new SearchRequest("movies").source(searchSourceBuilder);
    SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
    Terms byYear = searchResponse.getAggregations().get("countByYear");

    return byYear.getBuckets();
  }

}
