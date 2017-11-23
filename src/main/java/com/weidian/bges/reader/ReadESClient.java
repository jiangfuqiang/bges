package com.weidian.bges.reader;

import com.weidian.bges.base.ESClient;
import com.weidian.bges.base.ESConfiguration;
import com.weidian.bges.model.*;
import com.weidian.bges.reflect.ReflectValue;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
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
        BoolQueryBuilder booleanQueryBuilder = getBoolQueryWithTerms(searchQueryRequests);

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

        List<T> datas = new ArrayList<T>(size);
        BoolQueryBuilder booleanQueryBuilder = getBoolQueryWithTerms(searchQueryRequest);

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
       return this.queryData(clazz,index,type,-1,-1,null,queryData);
    }

    /**
     * 根据查询条件查询数据
     * @param index
     * @param type
     * @return
     */
    public SearchResult<T> queryData(Class clazz, String index, String type, Map<String, Object> queryData,
                                     SearchOperatorEnum searchOperatorEnum,
                                     SearchQueryEnum searchQueryEnum) throws InterruptedException,
            ExecutionException {
        SearchQueryRequest searchQueryRequest = generateQuery(queryData);
        searchQueryRequest.setSearchOperatorEnum(searchOperatorEnum);
        searchQueryRequest.setSearchQueryEnum(searchQueryEnum);
       return this.queryData(clazz,index,type,-1,-1,null,searchQueryRequest);
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
        return queryDataForSourceData(index,type,-1,-1,null,queryData);
    }

    /**
     * 根据查询条件查询原始数据
     * @param index
     * @param type
     * @return
     */
    public SourceSearchResult queryDataForSourceData(String index, String type,
                                                     Map<String, Object> queryData,
                                                     SearchOperatorEnum searchOperatorEnum,
                                                     SearchQueryEnum searchQueryEnum) throws InterruptedException,
            ExecutionException{
        SearchQueryRequest searchQueryRequest = generateQuery(queryData);
        searchQueryRequest.setSearchOperatorEnum(searchOperatorEnum);
        searchQueryRequest.setSearchQueryEnum(searchQueryEnum);
        return queryDataForSourceData(index,type,-1,-1,null,searchQueryRequest);
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
        BoolQueryBuilder booleanQueryBuilder = getBoolQueryWithTerms(searchQueryRequests);

        ListenableActionFuture<SearchResponse> searchResponseResult = getSearchRequest(booleanQueryBuilder,index,from,size,searchOrder);

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

        List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>(size);
        BoolQueryBuilder booleanQueryBuilder = getBoolQueryWithTerms(searchQueryRequest);

        ListenableActionFuture<SearchResponse> searchResponseResult = getSearchRequest(booleanQueryBuilder,index,from,size,searchOrder);

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
        SearchQueryRequest searchQueryRequest = new SearchQueryRequest(queryDataList, SearchOperatorEnum.MUST, SearchQueryEnum.TERM);
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
    private BoolQueryBuilder getBoolQueryWithTerms(SearchQueryRequest... searchQueryRequests) {
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        if(searchQueryRequests.length == 1) {
            booleanQueryBuilder = getBoolQueryWithTerm(searchQueryRequests[0]);
        } else {
            for(SearchQueryRequest queryRequest : searchQueryRequests) {
                SearchOperatorEnum queryRequestOperatorEnum = queryRequest.getQueryRequestOpatorEnum();
                if(queryRequest != null) {

                    concustorQueryBuilder(queryRequest,booleanQueryBuilder,true);
                }
            }
        }
        return booleanQueryBuilder;
    }

    /**
     * 构建 query
     * @return
     */
    private BoolQueryBuilder getBoolQueryWithTerm(SearchQueryRequest searchQueryRequest) {
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();

        SearchQueryRequest queryRequest = searchQueryRequest.getSearchQueryRequest();
        SearchOperatorEnum queryRequestOperatorEnum = searchQueryRequest.getQueryRequestOpatorEnum();
        if(queryRequest != null) {
            concustorQueryBuilder(queryRequest,booleanQueryBuilder,false);
        }


        SearchQueryEnum searchQueryEnum = searchQueryRequest.getSearchQueryEnum();
        SearchOperatorEnum searchOperatorEnum = searchQueryRequest.getSearchOperatorEnum();

        QueryBuilder queryBuilder = null;
        List<SearchQueryRequest.QueryData> queryDataList = searchQueryRequest.getQueryDataList();
        for(SearchQueryRequest.QueryData queryData : queryDataList) {
            String key = queryData.getName();
            Object value = queryData.getValue();
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
                if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST.getType()) {
                    booleanQueryBuilder.must(rangeQueryBuilder);
                } else if (searchOperatorEnum.getType() == SearchOperatorEnum.SHOULD.getType()) {
                    booleanQueryBuilder.should(rangeQueryBuilder);
                } else if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST_NOT.getType()) {
                    booleanQueryBuilder.mustNot(rangeQueryBuilder);
                } else if(searchOperatorEnum.getType() == SearchOperatorEnum.FILTER.getType()) {
                    booleanQueryBuilder.filter(rangeQueryBuilder);
                }
            } else {

                //设置匹配符
                if(searchQueryEnum.getType() == SearchQueryEnum.TERM.getType()) {
                    queryBuilder = QueryBuilders.termQuery(key, value);
                } else if(searchQueryEnum.getType() == SearchQueryEnum.MATCH.getType()) {
                    queryBuilder = QueryBuilders.matchQuery(key, value);
                } else if(searchQueryEnum.getType() == SearchQueryEnum.MATCH_PHRASE.getType()) {
                    queryBuilder = QueryBuilders.matchPhraseQuery(key, value);
                }

                //设置查询符
                if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST.getType()) {
                    booleanQueryBuilder.must(queryBuilder);
                } else if (searchOperatorEnum.getType() == SearchOperatorEnum.SHOULD.getType()) {
                    booleanQueryBuilder.should(queryBuilder);
                } else if (searchOperatorEnum.getType() == SearchOperatorEnum.MUST_NOT.getType()) {
                    booleanQueryBuilder.mustNot(queryBuilder);
                } else if(searchOperatorEnum.getType() == SearchOperatorEnum.FILTER.getType()) {
                    booleanQueryBuilder.filter(queryBuilder);
                }
            }
        }
        return booleanQueryBuilder;
    }


    private void concustorQueryBuilder(SearchQueryRequest queryRequest,BoolQueryBuilder booleanQueryBuilder, boolean isRoot) {
        SearchOperatorEnum queryRequestOperatorEnum = queryRequest.getSearchOperatorEnum();
        if(!isRoot) {  //如果是子查询
            queryRequestOperatorEnum = queryRequest.getQueryRequestOpatorEnum();
        }
        if(queryRequest != null) {
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
    }

    /**
     * 获取搜索结果
     * @param boolQueryBuilder
     * @param index
     * @param from
     * @param size
     * @param searchOrder
     * @return
     * @throws InterruptedException
     */
    private ListenableActionFuture<SearchResponse> getSearchRequest(BoolQueryBuilder boolQueryBuilder, String index, int from, int size,
                                                                    SearchOrder searchOrder) throws InterruptedException{

        Client client = getClient();

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
        searchRequestBuilder.setQuery(boolQueryBuilder);
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

                        List<SearchQueryRequest.QueryData> queryDataList = new ArrayList<SearchQueryRequest.QueryData>();
                        List<SearchQueryRequest.QueryData> rangeQueryDataList = new ArrayList<SearchQueryRequest.QueryData>();
                        SearchQueryRequest.QueryData queryData = new SearchQueryRequest.QueryData("title","蒋富强");
                        queryDataList.add(queryData);

                        SearchQueryRequest.QueryData rangeQueryData = new SearchQueryRequest.QueryData("views",
                                new QueryRange("1000", QueryRange.RangeOp.GTE),new QueryRange("2000", QueryRange.RangeOp.LTE));

                        rangeQueryDataList.add(rangeQueryData);

                        SearchQueryRequest searchQueryRequest = new SearchQueryRequest(queryDataList,SearchOperatorEnum.MUST,SearchQueryEnum.TERM);

                        SearchQueryRequest rangeSearchQueryRequest = new SearchQueryRequest(rangeQueryDataList,SearchOperatorEnum.FILTER,SearchQueryEnum.TERM);

                        SearchResult<TestModel> searchResult = esClient.queryData(TestModel.class,
                                "test_index1","test",0,20,new SearchOrder("views",SearchOrder.Order.DESC),
                                searchQueryRequest,rangeSearchQueryRequest);
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
