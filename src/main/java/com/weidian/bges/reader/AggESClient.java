package com.weidian.bges.reader;

import com.weidian.bges.base.ESClient;
import com.weidian.bges.base.ESConfiguration;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by jiang on 17/11/29.
 */
public class AggESClient<T> extends ESClient<T> {

    public Map<String,Object> getSourceMapById(String index, String type) throws InterruptedException,
            ExecutionException,IOException{
        Client client = getClient();
        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("agg").field("create_time").order(Terms.Order.count(false))
                .subAggregation(AggregationBuilders.topHits("top").from(0).size(10)).size(200);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type);
        SearchResponse searchResponse = searchRequestBuilder.setQuery(QueryBuilders.boolQuery()
            ).addSort("create_time", SortOrder.DESC)
            .addAggregation(aggregationBuilder).setExplain(true).execute().get();

        SearchHits hits = searchResponse.getHits();

        StringTerms agg = searchResponse.getAggregations().get("agg");
        System.out.println(agg.getBuckets().size()+"");
        List<Terms.Bucket> bucketList = agg.getBuckets();
        for(Terms.Bucket bucket : bucketList) {
            System.out.println(bucket.getDocCount()+" " + bucket.getKey().toString());
        }
        return null;
    }

    public static void main(String[] args) throws Exception{
        ESConfiguration esConfiguration = new ESConfiguration("localhost", 9300, "elasticsearch");
//        ESConfiguration esConfiguration = new ESConfiguration("10.1.23.154", 9300, "elasticsearch_ssd");
        new ESClient(esConfiguration);

        AggESClient aggESClient = new AggESClient();
        aggESClient.getSourceMapById("comment", "book_comment");
    }
}
