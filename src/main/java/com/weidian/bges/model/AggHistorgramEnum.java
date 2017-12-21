package com.weidian.bges.model;

/**
 * Created by jiang on 17/12/20.
 */
public enum AggHistorgramEnum {

    DATE_HIS(1);

    private int his;
    AggHistorgramEnum(int his) {
        this.his = his;
    }

    public int getHis() {
        return his;
    }

    public void setHis(int his) {
        this.his = his;
    }
}
