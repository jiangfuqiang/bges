package com.weidian.bges.model;

import java.util.List;
import java.util.Map;

/**
 * Created by jiang on 17/11/21.
 */
public class SearchQueryRequest {

    private List<QueryData> queryDataList;
    private SearchOperatorEnum searchOperatorEnum;  //should,must....
    private SearchQueryEnum searchQueryEnum;   //term,match,matchphrase.....
    private SearchQueryRequest searchQueryRequest;
    private SearchOperatorEnum queryRequestOpatorEnum;   //searchQueryRequest该如何查询

    public SearchQueryRequest(List<QueryData> queryDataList, SearchOperatorEnum searchOperatorEnum, SearchQueryEnum searchQueryEnum) {
        this.queryDataList = queryDataList;
        this.searchOperatorEnum = searchOperatorEnum;
        this.searchQueryEnum = searchQueryEnum;
    }

    public List<QueryData> getQueryDataList() {
        return queryDataList;
    }

    public void setQueryDataList(List<QueryData> queryDataList) {
        this.queryDataList = queryDataList;
    }

    public SearchOperatorEnum getSearchOperatorEnum() {
        return searchOperatorEnum;
    }

    public void setSearchOperatorEnum(SearchOperatorEnum searchOperatorEnum) {
        this.searchOperatorEnum = searchOperatorEnum;
    }

    public SearchQueryEnum getSearchQueryEnum() {
        return searchQueryEnum;
    }

    public void setSearchQueryEnum(SearchQueryEnum searchQueryEnum) {
        this.searchQueryEnum = searchQueryEnum;
    }

    public SearchQueryRequest getSearchQueryRequest() {
        return searchQueryRequest;
    }

    public void setSearchQueryRequest(SearchQueryRequest searchQueryRequest) {
        this.searchQueryRequest = searchQueryRequest;
    }

    public SearchOperatorEnum getQueryRequestOpatorEnum() {
        return queryRequestOpatorEnum;
    }

    public void setQueryRequestOpatorEnum(SearchOperatorEnum queryRequestOpatorEnum) {
        this.queryRequestOpatorEnum = queryRequestOpatorEnum;
    }

    public static class QueryData {
        private String name;
        private Object value;
        private float boost = 1.0f;
        private boolean isRnage = false;
        private QueryRange start;
        private QueryRange end;

        public QueryData(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public QueryData(String name, QueryRange start, QueryRange end) {
            this.name = name;
            this.start = start;
            this.end = end;
            isRnage = true;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public boolean isRnage() {
            return isRnage;
        }

        public void setRnage(boolean rnage) {
            isRnage = rnage;
        }

        public QueryRange getStart() {
            return start;
        }

        public void setStart(QueryRange start) {
            this.start = start;
        }

        public QueryRange getEnd() {
            return end;
        }

        public void setEnd(QueryRange end) {
            this.end = end;
        }

        public float getBoost() {
            return boost;
        }

        public void setBoost(float boost) {
            this.boost = boost;
        }
    }
}
