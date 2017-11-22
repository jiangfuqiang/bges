package com.weidian.bges.pool;

import com.weidian.bges.base.ESConfiguration;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiang on 17/11/21.
 */
public class ClientPool {

    private static Logger LOGGER = LoggerFactory.getLogger(ClientPool.class);

    private static Object lock = new Object();

    private static AtomicInteger INCR_MAXSIZE = new AtomicInteger(0);  //记录队列中存储的数量

    private static BlockingQueue<Client> POOLS = null;
    private static ESConfiguration configuration = null;
    private static Settings settings = null;
    private static InetSocketTransportAddress[] inetSocketTransportAddresses = null;

    public ClientPool(ESConfiguration esConfiguration) {
        POOLS = new ArrayBlockingQueue<Client>(esConfiguration.getDefaultMaxsize());
        configuration = esConfiguration;
        try {
            initClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initClient() throws Exception{
        settings = Settings.builder()
                .put("transport.type", configuration.getTransportType())
                .put("http.type", configuration.getHttpType())
                .put("cluster.name", configuration.getClusterName()).build();
        String hosts = configuration.getHosts();
        String[] hs = hosts.split(",");
        inetSocketTransportAddresses = new InetSocketTransportAddress[hs.length];
        int index = 0;
        for(String h : hs) {
            inetSocketTransportAddresses[index++] = new InetSocketTransportAddress(InetAddress.getByName(h),configuration.getPort());
        }

        for(int i = 0; i < configuration.getDefaultPoolsize(); i++) {
            createClient();
        }
    }

    /**
     * client入队列
     * @param client
     * @return
     * @throws InterruptedException
     */
    private boolean putClient(Client client) throws InterruptedException{
        if (INCR_MAXSIZE.get() >= configuration.getDefaultMaxsize()) {
            LOGGER.error("pool size exceeds max pool size");
            System.out.println("pool size exceeds max pool size");
            return false;
        }
        POOLS.offer(client);
        INCR_MAXSIZE.incrementAndGet();
        return true;
    }

    /**
     * 创建客户端
     * @return
     * @throws InterruptedException
     */
    public Client createClient() throws InterruptedException{
        synchronized (lock) {
            if (INCR_MAXSIZE.get() >= configuration.getDefaultMaxsize()) {
                LOGGER.error("pool size exceeds max pool size");
                System.out.println("pool size exceeds max pool size");
                return null;
            }
            Client client = new PreBuiltTransportClient(settings).addTransportAddresses(inetSocketTransportAddresses);
            putClient(client);
            return client;
        }
    }

    /**
     * 获取client，如果没有，返回null
     * @return
     * @throws InterruptedException
     */
    public Client leaseClient() throws InterruptedException{
        Client  client = POOLS.poll();
        return checkOrCreateClient(client,3000);
    }

    /**
     * 获取client，等待timeout毫秒时间
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public Client leaseClient(long timeout) throws InterruptedException{
        Client client = POOLS.poll(timeout, TimeUnit.MILLISECONDS);
        return checkOrCreateClient(client,timeout);
    }

    /**
     * 检查client是否为空，如果为空，则在合理的条件下创建
     * @param client
     * @return
     * @throws InterruptedException
     */
    private Client checkOrCreateClient(Client client,long timeout) throws InterruptedException{
        if (client != null) {
            return client;
        }
        synchronized (lock) {
            if (client == null) {
                if (INCR_MAXSIZE.get() < configuration.getDefaultMaxsize()) {
                    client = createClient();
                } else {
                    client = POOLS.poll(timeout,TimeUnit.MILLISECONDS);
                }
            }
        }
        return client;
    }

    /**
     * 回收连接
     * @param client
     * @return
     * @throws InterruptedException
     */
    public boolean releaseClient(Client client) throws InterruptedException{
        POOLS.offer(client);
        return true;
    }

    public void close() throws InterruptedException{
        synchronized (lock) {
            while (true) {
                if(POOLS.size() > 0) {
                    Client client = POOLS.take();
                    client.close();
                    INCR_MAXSIZE.decrementAndGet();
                }
                //等待还在执行中的client被回收并且被关闭
                if(POOLS.isEmpty() && INCR_MAXSIZE.get() == 0) {
                    break;
                }
            }
        }
    }

    public int remainCount() {
        return INCR_MAXSIZE.get();
    }
}
