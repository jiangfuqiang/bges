package com.weidian.bges.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by jiang on 17/11/21.
 */
public class SourceSearchResult implements Serializable {

    private long total;
    private List<Map<String,Object>> listData;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<Map<String, Object>> getListData() {
        return listData;
    }

    public void setListData(List<Map<String, Object>> listData) {
        this.listData = listData;
    }
}
