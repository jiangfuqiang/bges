package com.weidian.bges.model;

import java.io.Serializable;

/**
 * Created by jiang on 17/12/20.
 */
public abstract class AbstractBucketField implements Serializable {
    private AggEnum aggEnum = AggEnum.AGG;
    //response返回的terms类型
    private AggTermsEnum aggTermsEnum;
    private String fieldName;
    private String aggName;
    private int from = -1;
    private int size = -1;
    private SearchOrder searchOrder;
    private SearchQueryRequest searchQueryRequest;

    private AggMetricEnum aggMetricEnum;

    private AggHistorgramEnum aggHistorgramEnum;

    //子查询
    private AggBucketRequest aggBucketRequest;


    public AggTermsEnum getAggTermsEnum() {
        return aggTermsEnum;
    }

    public void setAggTermsEnum(AggTermsEnum aggTermsEnum) {
        this.aggTermsEnum = aggTermsEnum;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SearchOrder getSearchOrder() {
        return searchOrder;
    }

    public void setSearchOrder(SearchOrder searchOrder) {
        this.searchOrder = searchOrder;
    }

    public AggBucketRequest getAggBucketRequest() {
        return aggBucketRequest;
    }

    public void setAggBucketRequest(AggBucketRequest aggBucketRequest) {
        this.aggBucketRequest = aggBucketRequest;
    }

    public AggEnum getAggEnum() {
        return aggEnum;
    }

    public void setAggEnum(AggEnum aggEnum) {
        this.aggEnum = aggEnum;
    }

    public AggMetricEnum getAggMetricEnum() {
        return aggMetricEnum;
    }

    public void setAggMetricEnum(AggMetricEnum aggMetricEnum) {
        this.aggMetricEnum = aggMetricEnum;
    }

    public AggHistorgramEnum getAggHistorgramEnum() {
        return aggHistorgramEnum;
    }

    public void setAggHistorgramEnum(AggHistorgramEnum aggHistorgramEnum) {
        this.aggHistorgramEnum = aggHistorgramEnum;
    }

    public String getAggName() {
        return aggName;
    }

    public void setAggName(String aggName) {
        this.aggName = aggName;
    }

    public SearchQueryRequest getSearchQueryRequest() {
        return searchQueryRequest;
    }

    public void setSearchQueryRequest(SearchQueryRequest searchQueryRequest) {
        this.searchQueryRequest = searchQueryRequest;
    }
}
