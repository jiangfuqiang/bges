package com.weidian.bges.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang on 17/11/21.
 */
public class SearchResult<T> implements Serializable{

    private long total;
    private List<T> listData;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }


    public List<T> getListData() {
        return listData;
    }

    public void setListData(List<T> listData) {
        this.listData = listData;
    }
}
