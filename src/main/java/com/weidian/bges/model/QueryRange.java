package com.weidian.bges.model;

/**
 * Created by jiang on 17/11/21.
 */
public class QueryRange {
    private Object value;
    private RangeOp rangeOp;

    public QueryRange(Object value, RangeOp rangeOp) {
        this.value = value;
        this.rangeOp = rangeOp;
    }

    public RangeOp getRangeOp() {
        return rangeOp;
    }

    public void setRangeOp(RangeOp rangeOp) {
        this.rangeOp = rangeOp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public enum RangeOp {
        GT(0),
        GTE(1),
        LT(2),
        LTE(3);

        private int type;

        RangeOp(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
