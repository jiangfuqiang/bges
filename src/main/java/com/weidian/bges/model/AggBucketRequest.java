package com.weidian.bges.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang on 17/12/20.
 */
public class AggBucketRequest implements Serializable {

    private List<AbstractBucketField> aggBucketFields;


    public List<AbstractBucketField> getAggBucketFields() {
        return aggBucketFields;
    }

    public void setAggBucketFields(List<AbstractBucketField> aggBucketFields) {
        this.aggBucketFields = aggBucketFields;
    }
}
