package com.weidian.bges.base;

/**
 * Created by jiang on 17/11/21.
 */
public class ESConfiguration {

    private String hosts;
    private int port;
    private String clusterName;

    private String transportType = "netty3";
    private String httpType = "netty3";

    private static int DEFAULT_POOLSIZE = 10;
    private static int DEFAULT_MAXSIZE = 30;

    public ESConfiguration(){}

    public ESConfiguration(String hosts, int port, String clusterName) {
        this.port = port;
        this.clusterName = clusterName;
        this.hosts = hosts;
    }

    public ESConfiguration(String hosts, int port, String clusterName, String transportType,String httpType) {
        this.port = port;
        this.clusterName = clusterName;
        this.hosts = hosts;
        this.transportType = transportType;
        this.httpType = httpType;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getHttpType() {
        return httpType;
    }

    public void setHttpType(String httpType) {
        this.httpType = httpType;
    }

    public  int getDefaultPoolsize() {
        return DEFAULT_POOLSIZE;
    }

    public  void setDefaultPoolsize(int defaultPoolsize) {
        DEFAULT_POOLSIZE = defaultPoolsize;
    }

    public  int getDefaultMaxsize() {
        return DEFAULT_MAXSIZE;
    }

    public  void setDefaultMaxsize(int defaultMaxsize) {
        DEFAULT_MAXSIZE = defaultMaxsize;
    }
}
