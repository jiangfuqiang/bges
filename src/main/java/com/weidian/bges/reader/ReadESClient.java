package com.weidian.bges.reader;

import com.weidian.bges.base.ESClient;
import com.weidian.bges.base.ESConfiguration;
import com.weidian.bges.model.*;
import com.weidian.bges.reflect.ReflectValue;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiang on 17/11/21.
 */
public class ReadESClient<T> extends ESClient<T> {
    /**
     * 通过索引id查询数据
     * @param clazz
     * @param index
     * @param type
     * @param id
     * @return
     * @throws InterruptedException
     */
    public T getDataById(Class clazz, String index, String type,String id) throws InterruptedException{
        Client client = getClient();
        GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);
        this.releaseClient(client);
        if (getRequestBuilder != null) {
            GetResponse getResponse = getRequestBuilder.get();
            return convertSourceToEntity(getResponse,clazz);
        }
        return null;
    }

    /**
     * 批量获取索引数据
     * @param clazz
     * @param index
     * @param type
     * @param ids
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<T> multiGetDataByIds(Class clazz, String index, String type, String... ids) throws InterruptedException,
    ExecutionException{
        Client client = getClient();

        MultiGetRequestBuilder multiGetRequestBuilder = client.prepareMultiGet();
        multiGetRequestBuilder.add(index,type,ids);
        MultiGetRequest multiGetRequest = multiGetRequestBuilder.request();
        ActionFuture<MultiGetResponse> actionFuture = client.multiGet(multiGetRequest);
        MultiGetResponse multiGetResponse = actionFuture.get();
        List<T> datas = new ArrayList<T>(ids.length);
        MultiGetItemResponse[] multiGetItemResponses = multiGetResponse.getResponses();
        for(MultiGetItemResponse multiGetItemResponse : multiGetItemResponses) {
            GetResponse getResponse = multiGetItemResponse.getResponse();
            datas.add(convertSourceToEntity(getResponse,clazz));
        }
        return datas;
    }

    /**
     * 通过索引id查询原始数据
     * @param index
     * @param type
     * @param id
     * @return
     */
    public Map<String,Object> getSourceMapById(String index, String type,String id) throws InterruptedException{
        Client client = getClient();
        GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);
        this.releaseClient(client);
        if (getRequestBuilder != null) {
            GetResponse getResponse = getRequestBuilder.get();
            if (getResponse != null && getResponse.isExists()) {
                Map<String, Object> getFieldMap = getResponse.getSourceAsMap();
                return getFieldMap;
            }
        }
        return null;
    }


    /**
     * 根据查询条件分页查询数据
     * @param index
     * @param type
     * @param from
     * @param size
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type,
                                             int from, int size, SearchOrder searchOrder, SearchQueryRequest... searchQueryRequests) throws InterruptedException,
            ExecutionException {
        SearchResult<T> searchResult = new SearchResult<T>();
        if(size < 1) {
            size = 10;
        }
        List<T> datas = new ArrayList<T>(size);
        QueryBuilder booleanQueryBuilder = getBoolQueryWithTerms(searchQueryRequests);

        ListenableActionFuture<SearchResponse> searchResponseResult = getSearchRequest(booleanQueryBuilder,index,from,size,searchOrder);

        SearchResponse searchResponse = searchResponseResult.get();
        SearchHits searchHits = searchResponse.getHits();
        searchResult.setTotal(searchHits.getTotalHits());

        SearchHit[] searchHit1 = searchHits.getHits();
        for(SearchHit searchHit : searchHit1) {
            Map<String,Object> dataMap = searchHit.getSourceAsMap();
            ReflectValue reflectValue = new ReflectValue();
            Object instance = reflectValue.convertToEntity(clazz, dataMap);
            if(instance != null) {
                datas.add((T)instance);
            } else {
                throw new IllegalArgumentException("object is null for dataMap=" + dataMap.toString());
            }
        }
        searchResult.setListData(datas);
        return searchResult;
    }

    /**
     * 根据查询条件分页查询数据
     * @param index
     * @param type
     * @param from
     * @param size
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type,
                                     int from, int size, SearchOrder searchOrder, Map<String, Object> queryData) throws InterruptedException,
            ExecutionException {
        SearchResult<T> searchResult = new SearchResult<T>();
        if(size < 1) {
            size = 10;
        }

        SearchQueryRequest searchQueryRequest = generateQuery(queryData);


        return queryData(clazz, index, type, from, size,searchOrder,searchQueryRequest);
    }

    /**
     * 根据查询条件查询数据
     * @param index
     * @param type
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type, SearchQueryRequest... searchQueryRequests) throws InterruptedException,
            ExecutionException {
       return this.queryData(clazz,index,type,-1,-1,null,searchQueryRequests);
    }

    /**
     * 根据查询条件查询数据
     * @param index
     * @param type
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type, Map<String, Object> queryData) throws InterruptedException,
            ExecutionException {
       return this.queryData(clazz,index,type,null,queryData);
    }

    /**
     * 根据查询条件查询数据
     * @param index
     * @param type
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type, SearchOrder searchOrder, Map<String, Object> queryData) throws InterruptedException,
            ExecutionException {
       return this.queryData(clazz,index,type,-1,-1,searchOrder,queryData);
    }

    /**
     * 根据查询条件查询数据
     * @param index
     * @param type
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type,SearchOrder searchOrder, Map<String, Object> queryData,
                                     SearchOperatorEnum searchOperatorEnum,
                                     SearchQueryEnum searchQueryEnum) throws InterruptedException,
            ExecutionException {
        SearchQueryRequest searchQueryRequest = generateQuery(queryData);
        searchQueryRequest.setSearchOperatorEnum(searchOperatorEnum);
        searchQueryRequest.setSearchQueryEnum(searchQueryEnum);
       return this.queryData(clazz,index,type,-1,-1,searchOrder,searchQueryRequest);
    }

    /**
     * 根据查询条件查询原始数据
     * @param index
     * @param type
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type,
                                                     SearchQueryRequest... searchQueryRequests) throws InterruptedException,
            ExecutionException{
        return queryDataForSourceData(index,type,-1,-1,null,searchQueryRequests);
    }

    /**
     * 根据查询条件查询原始数据
     * @param index
     * @param type
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type,
                                                     Map<String, Object> queryData) throws InterruptedException,
            ExecutionException{
        return queryDataForSourceData(index,type,null,queryData);
    }

    /**
     * 根据查询条件查询原始数据
     * @param index
     * @param type
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type, SearchOrder searchOrder,
                                                     Map<String, Object> queryData) throws InterruptedException,
            ExecutionException{
        return queryDataForSourceData(index,type,-1,-1,searchOrder,queryData);
    }

    /**
     * 根据查询条件查询原始数据
     * @param index
     * @param type
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type,
                                                     SearchOrder searchOrder,
                                                     Map<String, Object> queryData,
                                                     SearchOperatorEnum searchOperatorEnum,
                                                     SearchQueryEnum searchQueryEnum) throws InterruptedException,
            ExecutionException{
        SearchQueryRequest searchQueryRequest = generateQuery(queryData);
        searchQueryRequest.setSearchOperatorEnum(searchOperatorEnum);
        searchQueryRequest.setSearchQueryEnum(searchQueryEnum);
        return queryDataForSourceData(index,type,-1,-1,searchOrder,searchQueryRequest);
    }

    /**
     * 根据查询条件分页查询原始数据
     * @param index
     * @param type
     * @param from
     * @param size
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type,
                                                     int from, int size, SearchOrder searchOrder,
                                                     SearchQueryRequest... searchQueryRequests) throws InterruptedException,
            ExecutionException{
        SourceSearchResult searchResult = new SourceSearchResult();
        if(size < 1) {
            size = 10;
        }

        List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>(size);
        QueryBuilder queryBuilder = getBoolQueryWithTerms(searchQueryRequests);

        ListenableActionFuture<SearchResponse> searchResponseResult = getSearchRequest(queryBuilder,index,from,size,searchOrder);

        SearchResponse searchResponse = searchResponseResult.get();
        SearchHits searchHits = searchResponse.getHits();
        searchResult.setTotal(searchHits.getTotalHits());

        SearchHit[] searchHit1 = searchHits.getHits();
        for(SearchHit searchHit : searchHit1) {
            Map<String,Object> source = searchHit.getSourceAsMap();
            datas.add(source);
        }
        searchResult.setListData(datas);
        return searchResult;
    }


    /**
     * 根据查询条件分页查询原始数据
     * @param index
     * @param type
     * @param from
     * @param size
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type,
                                                     int from, int size, SearchOrder searchOrder,
                                                     Map<String, Object> queryData) throws InterruptedException,
            ExecutionException{
        SourceSearchResult searchResult = new SourceSearchResult();
        if(size < 1) {
            size = 10;
        }

        SearchQueryRequest searchQueryRequest = generateQuery(queryData);


        return queryDataForSourceData(index,type,from,size,searchOrder,searchQueryRequest);
    }

    /**
     * 索引的source转为实体类
     * @param getResponse
     * @param clazz
     * @return
     */
    private T convertSourceToEntity(GetResponse getResponse,Class clazz) {
        if (getResponse != null && getResponse.isExists()) {
            Map<String, Object> getFieldMap = getResponse.getSourceAsMap();
            if(getFieldMap == null) {
                return null;
            }
            ReflectValue reflectValue = new ReflectValue();
            Object instance = reflectValue.convertToEntity(clazz, getFieldMap);
            if(instance != null) {
                return (T)instance;
            }
        }
        return null;
    }

    private SearchQueryRequest generateQuery(Map<String,Object> queryData) {
        List<SearchQueryRequest.QueryData> queryDataList = new ArrayList<SearchQueryRequest.QueryData>();
        for(Map.Entry<String,Object> entry : queryData.entrySet()) {
            SearchQueryRequest.QueryData queryData1 = new SearchQueryRequest.QueryData(entry.getKey(), entry.getValue());
            queryDataList.add(queryData1);
        }
        SearchQueryRequest searchQueryRequest = new SearchQueryRequest(queryDataList, SearchQueryEnum.TERM);
        return searchQueryRequest;
    }

    /**
     * 获取排序值
     * @param searchOrder
     * @return
     */
    private SortOrder getSortOrder(SearchOrder searchOrder) {
        SortOrder sortOrder = null;
        if(searchOrder.getOrder().getType() == SearchOrder.Order.ASC.getType()) {
            sortOrder = SortOrder.ASC;
        } else {
            sortOrder = SortOrder.DESC;
        }
        return sortOrder;
    }

    /**
     * 构建 query
     * @return
     */
    private QueryBuilder getBoolQueryWithTerms(SearchQueryRequest... searchQueryRequests) {
        QueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if(searchQueryRequests.length == 1) {
            SearchQueryRequest searchQueryRequest = searchQueryRequests[0];
            queryBuilder = getBoolQueryWithTerm(searchQueryRequest);

        } else {
            for(SearchQueryRequest queryRequest : searchQueryRequests) {
                SearchOperatorEnum queryRequestOperatorEnum = queryRequest.getQueryRequestOpatorEnum();
                if(queryRequest != null) {

                    queryBuilder = concustorQueryBuilder(queryRequest,true);
                }
            }
        }
        return queryBuilder;
    }

    /**
     * 构建 query
     * @return
     */
    private QueryBuilder getBoolQueryWithTerm(SearchQueryRequest searchQueryRequest) {
        QueryBuilder queryBuilder = null;

        boolean isBool = searchQueryRequest.isBool();  //是否是bool查询

        SearchQueryRequest queryRequest = searchQueryRequest.getSearchQueryRequest();
        SearchOperatorEnum queryRequestOperatorEnum = searchQueryRequest.getQueryRequestOpatorEnum();
        if(queryRequest != null) {
            queryBuilder = concustorQueryBuilder(queryRequest,false);
        }

        BoolQueryBuilder booleanQueryBuilder = null;
        if(isBool) {
            booleanQueryBuilder = QueryBuilders.boolQuery();
        }
        SearchQueryEnum searchQueryEnum = searchQueryRequest.getSearchQueryEnum();
        SearchOperatorEnum searchOperatorEnum = searchQueryRequest.getSearchOperatorEnum();

        List<SearchQueryRequest.QueryData> queryDataList = searchQueryRequest.getQueryDataList();
        for(SearchQueryRequest.QueryData queryData : queryDataList) {
            String key = queryData.getName();
            Object[] values = queryData.getValues();
            Object value = null;
            if(values != null && values.length > 0) {
                value = values[0];
            }
            //范围查询
            if(queryData.isRnage()) {
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(key);
                QueryRange start = queryData.getStart();
                QueryRange end = queryData.getEnd();
                if(start != null) {
                    QueryRange.RangeOp rangeOp = start.getRangeOp();
                    Object startValue = start.getValue();
                    if(rangeOp.getType() == QueryRange.RangeOp.GT.getType()) {
                        rangeQueryBuilder.gt(startValue);
                    } else if(rangeOp.getType() == QueryRange.RangeOp.GTE.getType()) {
                        rangeQueryBuilder.gte(startValue);
                    }
                }

                if(end != null) {
                    QueryRange.RangeOp rangeOp = end.getRangeOp();
                    Object endValue = end.getValue();
                     if(rangeOp.getType() == QueryRange.RangeOp.LT.getType()) {
                        rangeQueryBuilder.lt(endValue);
                    } else if(rangeOp.getType() == QueryRange.RangeOp.LTE.getType()) {
                        rangeQueryBuilder.lte(endValue);
                    }
                }
                if(isBool) {
                    if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST.getType()) {
                        booleanQueryBuilder.must(rangeQueryBuilder);
                    } else if (searchOperatorEnum.getType() == SearchOperatorEnum.SHOULD.getType()) {
                        booleanQueryBuilder.should(rangeQueryBuilder);
                    } else if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST_NOT.getType()) {
                        booleanQueryBuilder.mustNot(rangeQueryBuilder);
                    } else if (searchOperatorEnum.getType() == SearchOperatorEnum.FILTER.getType()) {
                        booleanQueryBuilder.filter(rangeQueryBuilder);
                    }

                    queryBuilder = booleanQueryBuilder;
                }
            } else {

                //设置匹配符
                if(searchQueryEnum.getType() == SearchQueryEnum.TERM.getType()) {
                    if(values.length > 1) {

                        queryBuilder = QueryBuilders.termsQuery(key, values);
                    } else {
                        queryBuilder = QueryBuilders.termQuery(key, value);
                    }
                } else if(searchQueryEnum.getType() == SearchQueryEnum.MATCH.getType()) {
                    Operator operator = Operator.OR;
                    if(queryData.getOperator() != null) {
                        if("and".equals(queryData.getOperator())) {
                            operator = Operator.AND;
                        }
                    }

                    queryBuilder = QueryBuilders.matchQuery(key, value).operator(operator).analyzer("ik_smart");
                } else if(searchQueryEnum.getType() == SearchQueryEnum.MATCH_PHRASE.getType()) {
                    queryBuilder = QueryBuilders.matchPhraseQuery(key, value);
                } else if(searchQueryEnum.getType() == SearchQueryEnum.EXISTS.getType()) {
                    queryBuilder = QueryBuilders.existsQuery(key);
                } else if(searchQueryEnum.getType() == SearchQueryEnum.FUZZY.getType()) {
                    queryBuilder = QueryBuilders.fuzzyQuery(key, value);
                } else if(searchQueryEnum.getType() == SearchQueryEnum.MATCH_PHRASE_PREFIX.getType()) {
                    queryBuilder = QueryBuilders.matchPhraseQuery(key, value);
                } else if(searchQueryEnum.getType() == searchQueryEnum.WILDCARD.getType()) {
                    queryBuilder = QueryBuilders.wildcardQuery(key, value.toString());
                }
                queryBuilder.boost(queryData.getBoost());

                if(isBool) {
                    //设置查询符
                    if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST.getType()) {
                        booleanQueryBuilder.must(queryBuilder);
                    } else if (searchOperatorEnum.getType() == SearchOperatorEnum.SHOULD.getType()) {
                        booleanQueryBuilder.should(queryBuilder);
                    } else if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST_NOT.getType()) {
                        booleanQueryBuilder.mustNot(queryBuilder);
                    } else if (searchOperatorEnum.getType() == SearchOperatorEnum.FILTER.getType()) {
                        booleanQueryBuilder.filter(queryBuilder);
                    }
                    if (queryData.getMimShouldMatch() != -1) {
                        booleanQueryBuilder.minimumShouldMatch(queryData.getMimShouldMatch());
                    }
                    queryBuilder = booleanQueryBuilder;
                }

            }
        }
        return queryBuilder;
    }

    private QueryBuilder concustorQueryBuilder(SearchQueryRequest queryRequest, boolean isRoot) {
        QueryBuilder queryBuilder = null;
        boolean isBool = queryRequest.isBool();

        SearchOperatorEnum queryRequestOperatorEnum = queryRequest.getSearchOperatorEnum();
        if(!isRoot) {  //如果是子查询
            queryRequestOperatorEnum = queryRequest.getQueryRequestOpatorEnum();
        }

        if(isBool) {
            queryBuilder = QueryBuilders.boolQuery();
        }

        if(queryRequest != null) {
            BoolQueryBuilder booleanQueryBuilder = (BoolQueryBuilder)queryBuilder;
            if(queryRequestOperatorEnum.getType() == SearchOperatorEnum.MUST.getType()) {
                booleanQueryBuilder.must(getBoolQueryWithTerm(queryRequest));
            } else if(queryRequestOperatorEnum.getType() == SearchOperatorEnum.SHOULD.getType()) {
                booleanQueryBuilder.should(getBoolQueryWithTerm(queryRequest));
            } else if(queryRequestOperatorEnum.getType() == SearchOperatorEnum.MUST_NOT.getType()) {
                booleanQueryBuilder.should(getBoolQueryWithTerm(queryRequest));
            } else if(queryRequestOperatorEnum.getType() == SearchOperatorEnum.FILTER.getType()) {
                booleanQueryBuilder.filter(getBoolQueryWithTerm(queryRequest));
            }
        }
        return queryBuilder;
    }

    /**
     * 获取搜索结果
     * @param queryBuilder
     * @param index
     * @param from
     * @param size
     * @param searchOrder
     * @return
     * @throws InterruptedException
     */
    private ListenableActionFuture<SearchResponse> getSearchRequest(QueryBuilder queryBuilder, String index, int from, int size,
                                                                    SearchOrder searchOrder) throws InterruptedException{

        Client client = getClient();

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
        searchRequestBuilder.setQuery(queryBuilder);
        if(from >= 0) {
            searchRequestBuilder.setFrom(from);
        }
        if(size > 0) {
            searchRequestBuilder.setSize(size);
        }

        if(searchOrder != null) {

            searchRequestBuilder.addSort(searchOrder.getName(),getSortOrder(searchOrder));
        }
        ListenableActionFuture<SearchResponse> searchResponseResult = searchRequestBuilder.execute();
        releaseClient(client);

        return searchResponseResult;
    }



    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    public static void main(String[] args) throws Exception{
        ESConfiguration esConfiguration = new ESConfiguration("localhost", 9300, "elasticsearch");
//        ESConfiguration esConfiguration = new ESConfiguration("10.1.23.154", 9300, "elasticsearch_ssd");
        new ESClient(esConfiguration);

        for(int i = 0; i < 1; i++) {
            new Thread(){
                public void run(){
                    try {
                        ReadESClient<TestModel> esClient = new ReadESClient<TestModel>();
//                        TestModel testModel = esClient.getDataById(TestModel.class, "ds_mesa_item_metric_db", "item", "462848335654243064266");
//                        TestModel testModel = esClient.getDataById(TestModel.class, "test_index1", "test", "1");
//                        System.out.println(Thread.currentThread().getName()+" "+testModel.toString());
//                        Map<String,Object> queryData = new HashMap<String,Object>();
//                        queryData.put("content","阿里也是一个有尊严有骄傲的人");
//
//                        SearchOrder searchOrder = new SearchOrder("star", SearchOrder.Order.DESC);
//                        SourceSearchResult sourceSearchResult = esClient.queryDataForSourceData("comment","book_comment",searchOrder,queryData,SearchOperatorEnum.SHOULD,SearchQueryEnum.MATCH);
//
//                        System.out.println(esClient.getSourceMapById("comment","book_comment","1"));
                        List<SearchQueryRequest.QueryData> queryDataList = new ArrayList<SearchQueryRequest.QueryData>();
//                        List<SearchQueryRequest.QueryData> rangeQueryDataList = new ArrayList<SearchQueryRequest.QueryData>();
                        SearchQueryRequest.QueryData queryData1 = new SearchQueryRequest.QueryData("content","追风筝人的看");
//                        queryData1.setMimShouldMatch(1);
                        queryDataList.add(queryData1);
//
//                        SearchQueryRequest.QueryData rangeQueryData = new SearchQueryRequest.QueryData("views",
//                                new QueryRange("1000", QueryRange.RangeOp.GTE),new QueryRange("2000", QueryRange.RangeOp.LTE));

//                        rangeQueryDataList.add(rangeQueryData);
//
                        SearchQueryRequest searchQueryRequest = new SearchQueryRequest(queryDataList,SearchQueryEnum.MATCH);
//
//                        SearchQueryRequest rangeSearchQueryRequest = new SearchQueryRequest(rangeQueryDataList,SearchOperatorEnum.FILTER,SearchQueryEnum.TERM);
//
                        SearchResult<TestModel> searchResult = esClient.queryData(TestModel.class,
                                "comment","book_comment",0,20,null,
                                searchQueryRequest);
                        System.out.println(searchResult.getTotal()+"------" + searchResult.getListData().toString());
//                        List<TestModel> datas = esClient.multiGetDataByIds(TestModel.class,"test_index1","test","2","3","4","1");
//                        System.out.println(datas.toString());

//                        Map<String, Object> dataMap = esClient.getSourceMapById("ds_mesa_item_metric_db", "item", "462848335654243064266");
//                        System.out.println(Thread.currentThread().getName() + "   " + dataMap.toString());
//                        atomicInteger.incrementAndGet();
//                        System.out.println(Thread.currentThread().getName() + "   " + atomicInteger.get() + "===================");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }



    }
}
