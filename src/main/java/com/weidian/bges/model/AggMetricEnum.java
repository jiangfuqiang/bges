package com.weidian.bges.model;

/**
 * Created by jiang on 17/11/29.
 */
public enum AggMetricEnum {

    AVG(1,"avg"),
    COUNT(2,"count"),
    SUM(3,"sum"),
    CARDINALITY(4,"cardinality"),
    EXTENDED_STATS(5,"extendedstats"),
    GEO_BOUNDS(6,"geobounds"),
    GEO_CENTROID(7,"geocentroid"),
    MAX(8,"max"),
    MIN(9,"min"),
    PERCENTILES(10,"percentiles"),
    PERCENTILE_RANK(11,"percentilerank"),
    SCRIPTED_METRIC(12,"scriptedmetric"),
    STATS(13,"stats"),
    TOP(15,"top"),
    VALUE_COUNT(16,"valuecount");

    private int type;
    private String name;

    AggMetricEnum(int type,String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
