package com.weidian.bges.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang on 17/11/21.
 */
public class SourceSearchResult implements Serializable {

    private long total;
    private List<String> listData;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<String> getListData() {
        return listData;
    }

    public void setListData(List<String> listData) {
        this.listData = listData;
    }
}
