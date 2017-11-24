package com.weidian.bges.model;

/**
 * Created by jiang on 17/11/24.
 */
public enum QueryPhraseEnum {
    DEFAULT(0),
    BOOL(1),
    DIS_MAX(2);

    private int type;

    QueryPhraseEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
