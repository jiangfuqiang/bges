package com.weidian.bges.model;

/**
 * Created by jiang on 17/11/21.
 */
public enum SearchOperatorEnum {
    SHOULD(0),
    MUST(1),
    MUST_NOT(2),
    FILTER(3);
    private int type;
    SearchOperatorEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
