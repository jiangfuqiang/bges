package com.weidian.bges.model;

import com.weidian.bges.annotation.ColName;

/**
 * Created by jiang on 17/11/21.
 */
public class TestModel {

    @ColName(name="item_id")
    private String id;

    private String shopId;

    private String date;

    private String title;

    private long views;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String toString() {
        return shopId + "==" + date + " ==" + title + "==" + views;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}
