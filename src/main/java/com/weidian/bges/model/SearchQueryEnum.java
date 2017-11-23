package com.weidian.bges.model;

/**
 * Created by jiang on 17/11/21.
 */
public enum SearchQueryEnum {
    TERM(0),
    MATCH(1),
    MATCH_PHRASE(2),
    EXISTS(3),
    FUZZY(4),  //模糊匹配
    MATCH_PHRASE_PREFIX(5),  //前缀匹配
    WILDCARD(6);
    private int type;
    SearchQueryEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
