package com.weidian.bges.model;

/**
 * Created by jiang on 17/12/20.
 */
public enum AggEnum {

    AGG(1),
    HISGORGRAM(2),
    METRICS(3);
    private int type;
    AggEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
