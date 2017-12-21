package com.weidian.bges.reader;

import com.weidian.bges.base.ESClient;
import com.weidian.bges.base.ESConfiguration;
import com.weidian.bges.model.*;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.DoubleTerms;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by jiang on 17/11/29.
 */
public class AggESClient<T> extends ESClient<T> {

    public AggResponse getSourceMapById(String index, String type, AggBucketRequest aggBucketRequest) throws InterruptedException,
            ExecutionException,IOException{
        Client client = getClient();

        List<AbstractBucketField> aggBucketFields = aggBucketRequest.getAggBucketFields();
        AggResponse aggResponse = new AggResponse();
        List<BucketResponse> bucketResponses = null;
        List<MetricsResponse> metricsResponses = null;
        for(AbstractBucketField aggBucketField : aggBucketFields) {
            AggEnum aggEnum = aggBucketField.getAggEnum();
            String aggName = aggBucketField.getAggName();
            String fieldName = aggBucketField.getFieldName();
            AggTermsEnum aggTermsEnum = aggBucketField.getAggTermsEnum();
            int from = aggBucketField.getFrom();
            int size = aggBucketField.getSize();
            SearchOrder searchOrder = aggBucketField.getSearchOrder();
            AggBucketRequest subAggr = aggBucketField.getAggBucketRequest();
            SearchQueryRequest searchQueryRequest = aggBucketField.getSearchQueryRequest();

            if(aggEnum.getType() == AggEnum.AGG.getType()) {
                AggregationBuilder aggregationBuilder = AggregationBuilders.terms(aggName)
                        .field(fieldName);
                if(subAggr != null) {
                    processSubAggr(subAggr,aggregationBuilder);
                }
                SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
                SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
                ).addAggregation(aggregationBuilder).setExplain(true).execute().get();

                SearchHits hits = searchResponse.getHits();
                List<Terms.Bucket> bucketList = null;
                if(aggTermsEnum.getTerm() == AggTermsEnum.STRING.getTerm()) {
                    StringTerms agg = searchResponse.getAggregations().get(aggName);
                    bucketList = agg.getBuckets();
                } else if(aggTermsEnum.getTerm() == AggTermsEnum.LONG.getTerm()) {
                    LongTerms agg = searchResponse.getAggregations().get(aggName);
                    bucketList = agg.getBuckets();
                } else if(aggTermsEnum.getTerm() == AggTermsEnum.DOUBLE.getTerm()) {
                    DoubleTerms agg = searchResponse.getAggregations().get(aggName);
                    bucketList = agg.getBuckets();
                } else if(aggTermsEnum.getTerm() == AggTermsEnum.INTERNAL.getTerm()) {

                }

                Map<String,Long> bucketData = new HashMap<String,Long>();
                for(Terms.Bucket bucket : bucketList) {
                    bucketData.put(bucket.getKeyAsString(),bucket.getDocCount());
                }
                BucketResponse bucketResponse = new BucketResponse(bucketData,aggName);
                bucketResponse.setSize(bucketList.size());
                if(bucketResponses == null) {
                    bucketResponses = new ArrayList<BucketResponse>(aggBucketFields.size());
                }
                bucketResponses.add(bucketResponse);
            } else if(aggEnum.getType() == AggEnum.METRICS.getType()) {
                AggMetricEnum aggMetricEnum = aggBucketField.getAggMetricEnum();
                if(aggMetricEnum.getType() == AggMetricEnum.TOP.getType()) {
                    TopHitsAggregationBuilder aggregationBuilder = AggregationBuilders.topHits(aggMetricEnum.getName());
                    if(from > 0) {
                        aggregationBuilder.from(from);
                    }
                    if(size > 0) {
                        aggregationBuilder.size(size);
                    }
                    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
                    SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
                    ).addAggregation(aggregationBuilder).setExplain(true).execute().get();

                    SearchHits hits = searchResponse.getHits();
                    List<Terms.Bucket> bucketList = null;
                    if(aggTermsEnum.getTerm() == AggTermsEnum.STRING.getTerm()) {
                        StringTerms agg = searchResponse.getAggregations().get(aggName);
                        bucketList = agg.getBuckets();
                    } else if(aggTermsEnum.getTerm() == AggTermsEnum.LONG.getTerm()) {
                        LongTerms agg = searchResponse.getAggregations().get(aggName);
                        bucketList = agg.getBuckets();
                    } else if(aggTermsEnum.getTerm() == AggTermsEnum.DOUBLE.getTerm()) {
                        DoubleTerms agg = searchResponse.getAggregations().get(aggName);
                        bucketList = agg.getBuckets();
                    } else if(aggTermsEnum.getTerm() == AggTermsEnum.INTERNAL.getTerm()) {

                    }

                    Map<String,Long> bucketData = new HashMap<String,Long>();
                    for(Terms.Bucket bucket : bucketList) {
                        bucketData.put(bucket.getKeyAsString(),bucket.getDocCount());
                    }
                    BucketResponse bucketResponse = new BucketResponse(bucketData,aggName);
                    bucketResponse.setSize(bucketList.size());
                    if(bucketResponses == null) {
                        bucketResponses = new ArrayList<BucketResponse>(aggBucketFields.size());
                    }
                    bucketResponses.add(bucketResponse);
                } else if(aggMetricEnum.getType() == AggMetricEnum.MIN.getType()) {
                    MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min(aggName).field(fieldName);
                    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
                    SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
                    ).addSort("create_time", SortOrder.DESC)
                            .addAggregation(minAggregationBuilder).setExplain(true).execute().get();

                    SearchHits hits = searchResponse.getHits();

                    Min agg = searchResponse.getAggregations().get(aggName);
                    MetricsResponse metricsResponse = new MetricsResponse(agg.getMetaData(),agg.getValue());
                    metricsResponse.setAggName(aggName);
                    if(metricsResponses == null) {
                        metricsResponses = new ArrayList<MetricsResponse>(aggBucketFields.size());
                    }
                    metricsResponses.add(metricsResponse);
                } else if(aggMetricEnum.getType() == AggMetricEnum.MAX.getType()) {
                    MaxAggregationBuilder minAggregationBuilder = AggregationBuilders.max(aggName).field(fieldName);
                    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
                    SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
                    ).addSort("create_time", SortOrder.DESC)
                            .addAggregation(minAggregationBuilder).setExplain(true).execute().get();

                    SearchHits hits = searchResponse.getHits();

                    Max agg = searchResponse.getAggregations().get(aggName);

                    MetricsResponse metricsResponse = new MetricsResponse(agg.getMetaData(),agg.getValue());
                    metricsResponse.setAggName(aggName);
                    if(metricsResponses == null) {
                        metricsResponses = new ArrayList<MetricsResponse>(aggBucketFields.size());
                    }
                    metricsResponses.add(metricsResponse);

                } else if(aggMetricEnum.getType() == AggMetricEnum.SUM.getType()) {
                    SumAggregationBuilder sumAggregationBuilder = AggregationBuilders.sum(aggName).field(fieldName);
                    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
                    SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
                    ).addSort("create_time", SortOrder.DESC)
                            .addAggregation(sumAggregationBuilder).setExplain(true).execute().get();

                    SearchHits hits = searchResponse.getHits();
                    Sum sum = searchResponse.getAggregations().get(aggName);
                    MetricsResponse metricsResponse = new MetricsResponse(sum.getMetaData(),sum.getValue());
                    metricsResponse.setAggName(aggName);
                    if(metricsResponses == null) {
                        metricsResponses = new ArrayList<MetricsResponse>(aggBucketFields.size());
                    }
                    metricsResponses.add(metricsResponse);
                } else if(aggMetricEnum.getType() == AggMetricEnum.AVG.getType()) {
                    AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg(aggName).field(fieldName);
                    SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
                    SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.matchQuery("movie_title","小王子")
                    ).addAggregation(avgAggregationBuilder).setExplain(true).execute().get();

                    SearchHits hits = searchResponse.getHits();
                    Avg avg = searchResponse.getAggregations().get(aggName);
                    MetricsResponse metricsResponse = new MetricsResponse(avg.getMetaData(),avg.getValue());
                    metricsResponse.setAggName(aggName);
                    if(metricsResponses == null) {
                        metricsResponses = new ArrayList<MetricsResponse>(aggBucketFields.size());
                    }
                    metricsResponses.add(metricsResponse);
                }

            } else if(aggEnum.getType() == AggEnum.HISGORGRAM.getType()) {
                AggHistorgramEnum aggHistorgramEnum = aggBucketField.getAggHistorgramEnum();

            }
        }
        aggResponse.setMetricsResponses(metricsResponses);
        aggResponse.setBucketResponses(bucketResponses);

        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("agg").field("create_time").order(Terms.Order.count(false))
                .subAggregation(AggregationBuilders.topHits("top").from(0).size(10)).size(200);

        AggregationBuilder aggregationBuilder1 = AggregationBuilders.dateHistogram("agg_star").
                field("create_time").dateHistogramInterval(DateHistogramInterval.MONTH);

        MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min("agg").field("star");


        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
        SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
            ).addSort("create_time", SortOrder.DESC)
            .addAggregation(minAggregationBuilder).setExplain(true).execute().get();


//        StringTerms agg = searchResponse.getAggregations().get("agg");
//        System.out.println(agg.getBuckets().size()+"");
//        List<Terms.Bucket> bucketList = agg.getBuckets();
//        for(Terms.Bucket bucket : bucketList) {
//            System.out.println(bucket.getDocCount()+" " + bucket.getKey().toString());
//        }
        return aggResponse;
    }

    private void processSubAggr(AggBucketRequest aggBucketRequest,AggregationBuilder aggregationBuilder) {
        List<AbstractBucketField> aggBucketFields = aggBucketRequest.getAggBucketFields();
        for(AbstractBucketField aggBucketField : aggBucketFields) {
            AggEnum aggEnum = aggBucketField.getAggEnum();
            String aggName = aggBucketField.getAggName();
            String fieldName = aggBucketField.getFieldName();
            int from = aggBucketField.getFrom();
            int size = aggBucketField.getSize();
            SearchOrder searchOrder = aggBucketField.getSearchOrder();
            AggBucketRequest subAggr = aggBucketField.getAggBucketRequest();

            if(aggEnum.getType() == AggEnum.AGG.getType()) {
                AggregationBuilder ab = AggregationBuilders.terms(aggName)
                        .field(fieldName);
                if(subAggr != null) {
                    processSubAggr(subAggr,ab);
                }
                aggregationBuilder.subAggregation(aggregationBuilder);
            } else if(aggEnum.getType() == AggEnum.METRICS.getType()) {
                AggMetricEnum aggMetricEnum = aggBucketField.getAggMetricEnum();
                if(aggMetricEnum.getType() == AggMetricEnum.TOP.getType()) {
                    TopHitsAggregationBuilder aggregationBuilder1 = AggregationBuilders.topHits(aggMetricEnum.getName());
                    if(from > 0) {
                        aggregationBuilder1.from(from);
                    }
                    if(size > 0) {
                        aggregationBuilder1.size(size);
                    }
                    aggregationBuilder1.subAggregation(aggregationBuilder1);
                }
            } else if(aggEnum.getType() == AggEnum.HISGORGRAM.getType()) {
                AggHistorgramEnum aggHistorgramEnum = aggBucketField.getAggHistorgramEnum();

            }
        }
    }

    public static void main(String[] args) throws Exception{
        ESConfiguration esConfiguration = new ESConfiguration("localhost", 9300, "elasticsearch");
//        ESConfiguration esConfiguration = new ESConfiguration("10.1.23.154", 9300, "elasticsearch_ssd");
        new ESClient(esConfiguration);

        AggESClient aggESClient = new AggESClient();
        AggBucketRequest aggBucketRequest = new AggBucketRequest();
        List<AbstractBucketField> aggBucketFields = new ArrayList<AbstractBucketField>();
        AbstractBucketField aggBucketField = new AggBucketField();
        aggBucketField.setAggName("agg");
        aggBucketField.setFieldName("create_time");
        aggBucketField.setAggTermsEnum(AggTermsEnum.STRING);
        AggBucketRequest subRequest = new AggBucketRequest();

        List<AbstractBucketField> subBucketFields = new ArrayList<AbstractBucketField>();
        AbstractBucketField abstractBucketField = new AggMetricField();
        abstractBucketField.setAggMetricEnum(AggMetricEnum.AVG);
        abstractBucketField.setAggName("agg");
        abstractBucketField.setFieldName("star");
        abstractBucketField.setAggTermsEnum(AggTermsEnum.STRING);
        subBucketFields.add(abstractBucketField);
        subRequest.setAggBucketFields(subBucketFields);

        aggBucketField.setAggBucketRequest(subRequest);

        aggBucketFields.add(aggBucketField);
        aggBucketRequest.setAggBucketFields(aggBucketFields);

        AggResponse aggResponse = aggESClient.getSourceMapById("comment", "book_comment",subRequest);
        List<MetricsResponse> metricsResponse = aggResponse.getMetricsResponses();
        List<BucketResponse> bucketResponses = aggResponse.getBucketResponses();

    }
}
