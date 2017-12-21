package com.weidian.bges.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang on 17/12/21.
 */
public class AggResponse implements Serializable {

    private List<MetricsResponse> metricsResponses;
    private List<BucketResponse> bucketResponses;

    public List<MetricsResponse> getMetricsResponses() {
        return metricsResponses;
    }

    public void setMetricsResponses(List<MetricsResponse> metricsResponses) {
        this.metricsResponses = metricsResponses;
    }

    public List<BucketResponse> getBucketResponses() {
        return bucketResponses;
    }

    public void setBucketResponses(List<BucketResponse> bucketResponses) {
        this.bucketResponses = bucketResponses;
    }
}
