package com.weidian.bges.model;

import java.io.Serializable;

/**
 * Created by jiang on 17/12/20.
 */
public class AggBucketField extends AbstractBucketField implements Serializable {

    public AggBucketField() {
        this.setAggEnum(AggEnum.AGG);
    }


}
