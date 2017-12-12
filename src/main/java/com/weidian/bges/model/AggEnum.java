package com.weidian.bges.model;

/**
 * Created by jiang on 17/11/29.
 */
public enum AggEnum {

    AVG(1),
    COUNT(2);

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
