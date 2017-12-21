package com.weidian.bges.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by jiang on 17/12/21.
 */
public class MetricsResponse implements Serializable {

    private Map<String,Object> metaData;
    private Double value;
    private String aggName;

    public MetricsResponse() {
    }

    public MetricsResponse(Map<String, Object> metaData, Double value) {
        this.metaData = metaData;
        this.value = value;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getAggName() {
        return aggName;
    }

    public void setAggName(String aggName) {
        this.aggName = aggName;
    }
}
