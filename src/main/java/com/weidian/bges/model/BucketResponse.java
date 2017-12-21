package com.weidian.bges.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by jiang on 17/12/21.
 */
public class BucketResponse implements Serializable {

    private Map<String,Long> datas;
    private String name;
    private int size;

    public BucketResponse() {
    }

    public BucketResponse(Map<String, Long> datas, String name) {
        this.datas = datas;
        this.name = name;
    }

    public Map<String, Long> getDatas() {
        return datas;
    }

    public void setDatas(Map<String, Long> datas) {
        this.datas = datas;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
