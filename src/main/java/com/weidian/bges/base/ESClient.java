package com.weidian.bges.base;

import com.weidian.bges.model.SearchOrder;
import com.weidian.bges.model.SearchResult;
import com.weidian.bges.model.SourceSearchResult;
import com.weidian.bges.model.TestModel;
import com.weidian.bges.pool.ClientPool;
import com.weidian.bges.reflect.ReflectValue;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.elasticsearch.index.query.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiang on 17/11/21.
 */
public class ESClient<T> {

    private static final Logger logger = LoggerFactory.getLogger(ESClient.class);

    protected static final String INDEX_ID_NAME = "_id";

    private volatile static ClientPool clientPool = null;

    protected ESConfiguration esConfiguration;

    public ESClient(){}
    public ESClient(ESConfiguration esConfiguration) {
        this.esConfiguration = esConfiguration;
        try {
            init(esConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(ESConfiguration esConfiguration) throws Exception{
        if(clientPool == null) {
            synchronized (logger) {
                if(clientPool == null) {
                    clientPool = new ClientPool(esConfiguration);
                }
            }
        }
    }

    /**
     * 调用该方法之后，必须release client
     * @return
     * @throws InterruptedException
     */
    public Client getClient() throws InterruptedException{
        return clientPool.leaseClient();
    }

    /**
     * 调用该方法之后，必须release client
     * @param timeout  毫秒
     * @return
     * @throws InterruptedException
     */
    public Client getClient(long timeout) throws InterruptedException{
        return clientPool.leaseClient(timeout);
    }

    public void releaseClient(Client client) {
        try {
            clientPool.releaseClient(client);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() throws InterruptedException{
        clientPool.close();
    }


    public ESConfiguration getEsConfiguration() {
        return esConfiguration;
    }

    public void setEsConfiguration(ESConfiguration esConfiguration) {
        this.esConfiguration = esConfiguration;
    }

}
