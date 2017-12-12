package com.weidian.bges.writer;

import com.weidian.bges.base.ESClient;
import com.weidian.bges.base.ESConfiguration;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by jiang on 17/11/21.
 */
public class WriteESClient<T> extends ESClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteESClient.class);

    /**
     * 索引单个数据，dataMap中的_id是索引ID
     * @param dataMap
     * @param index
     * @param type
     * @return
     * @throws InterruptedException
     */
    public int writeIndex(Map<String, Object> dataMap, String index, String type) throws InterruptedException{
        Client client = getClient();
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index,type);
        IndexRequest indexRequest = indexRequestBuilder.request();
        if(dataMap.containsKey(INDEX_ID_NAME)) {
            indexRequest.id(dataMap.get(INDEX_ID_NAME).toString());
            dataMap.remove(INDEX_ID_NAME);
        }
        indexRequest.source(dataMap);
        ActionFuture<IndexResponse> actionFuture = client.index(indexRequest);

        releaseClient(client);

        indexRequest = null;

        IndexResponse indexResponse = actionFuture.actionGet();

        RestStatus restStatus = indexResponse.status();
        return restStatus.getStatus();
    }

    /**
     * 批量写入索引，dataMap中的_id是索引ID
     * @param dataMapList
     * @param index
     * @param type
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int bulkWriteIndex(List<Map<String,Object>> dataMapList, String index, String type) throws
            InterruptedException,ExecutionException {
        Client client = getClient();

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        for(Map<String, Object> dataMap : dataMapList) {
            IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index,type);
            IndexRequest indexRequest = indexRequestBuilder.request();
            if(dataMap.containsKey(INDEX_ID_NAME)) {
                indexRequest.id(dataMap.get(INDEX_ID_NAME).toString());
                dataMap.remove(INDEX_ID_NAME);
            }
            indexRequest.source(dataMap);
            bulkRequestBuilder.add(indexRequest);
        }

        BulkRequest bulkRequest = bulkRequestBuilder.request();

        ActionFuture<BulkResponse> actionFuture = client.bulk(bulkRequest);

        releaseClient(client);

        bulkRequest = null;

        BulkResponse bulkResponse = actionFuture.get();

        RestStatus restStatus = bulkResponse.status();

        if(restStatus.getStatus() != 200) {
            LOGGER.warn("bulk response status is " +restStatus.getStatus()+" "+bulkResponse.buildFailureMessage());
        }

        return restStatus.getStatus();

    }


    /**
     * 更新id是否存在来插入或者索引
     * @param dataMap
     * @param index
     * @param type
     * @param id
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public int writeOrUpdateIndexById(Map<String, Object> dataMap, String index, String type, String id)
            throws InterruptedException,ExecutionException {
        Client client = getClient();

        if(dataMap.containsKey(INDEX_ID_NAME)) {
            dataMap.remove(INDEX_ID_NAME);
        }
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index,type);

        IndexRequest indexRequest = indexRequestBuilder.request();
        indexRequest.id(id);
        indexRequest.source(dataMap);

        UpdateRequestBuilder updateRequest = client.prepareUpdate();
        updateRequest.setDoc(dataMap);
        updateRequest.setIndex(index);
        updateRequest.setType(type);
        updateRequest.setUpsert(indexRequest);

        UpdateRequest updateRequest1 = updateRequest.request();
        updateRequest1.id(id);

        ActionFuture<UpdateResponse> actionFuture = client.update(updateRequest1);

        releaseClient(client);

        indexRequest = null;

        UpdateResponse updateResponse = actionFuture.get();

        RestStatus restStatus = updateResponse.status();

        if(restStatus.getStatus() != 200) {
            LOGGER.warn("update response status is " +restStatus.getStatus());
        }

        return restStatus.getStatus();
    }

    public static void main(String[] args) throws Exception {
        ESConfiguration esConfiguration = new ESConfiguration("localhost", 9300, "elasticsearch");
        new ESClient(esConfiguration);
        String index = "test_index1";
        String type = "test";

        Map<String,Object> dataMap = new HashMap<String, Object>();
        dataMap.put("title", "蒋富强");
        dataMap.put("views",1000);
        dataMap.put("_id",6);

        WriteESClient writeESClient = new WriteESClient();
        int status = writeESClient.writeIndex(dataMap, index,type);
        System.out.println(status);

    }
}
