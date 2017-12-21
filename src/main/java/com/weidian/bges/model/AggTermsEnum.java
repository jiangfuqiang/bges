package com.weidian.bges.model;

/**
 * Created by jiang on 17/12/20.
 */
public enum AggTermsEnum {
    STRING(1),
    LONG(2),
    DOUBLE(3),
    INTERNAL(4),
    GLOBAL_ORIDINAL(5),
    INTERNAL_MAPED(6);
    private int term;
    AggTermsEnum(int term) {
        this.term = term;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
