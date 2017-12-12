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
    private SearchQueryRequest[] searchQueryRequests;
    private QueryPhraseEnum queryPhraseEnum = QueryPhraseEnum.DEFAULT;

    public SearchQueryRequest(List<QueryData> queryDataList, SearchOperatorEnum searchOperatorEnum,SearchQueryEnum searchQueryEnum,
                              SearchQueryRequest... searchQueryRequests) {
        this(queryDataList,searchOperatorEnum,searchQueryEnum,QueryPhraseEnum.DEFAULT,searchQueryRequests);
    }

    public SearchQueryRequest(List<QueryData> queryDataList, SearchOperatorEnum searchOperatorEnum,SearchQueryEnum searchQueryEnum) {
        this(queryDataList,searchOperatorEnum,searchQueryEnum,QueryPhraseEnum.DEFAULT);
    }

    public SearchQueryRequest(List<QueryData> queryDataList, SearchOperatorEnum searchOperatorEnum, SearchQueryEnum searchQueryEnum, QueryPhraseEnum queryPhraseEnum) {
        this(queryDataList,searchOperatorEnum,searchQueryEnum,queryPhraseEnum,null);
    }

    public SearchQueryRequest(List<QueryData> queryDataList, SearchOperatorEnum searchOperatorEnum, SearchQueryEnum searchQueryEnum,
                              QueryPhraseEnum queryPhraseEnum,SearchQueryRequest... searchQueryRequests) {
        this.queryDataList = queryDataList;
        this.searchOperatorEnum = searchOperatorEnum;
        this.searchQueryEnum = searchQueryEnum;
        this.queryPhraseEnum = queryPhraseEnum;
        this.searchQueryRequests = searchQueryRequests;
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

    public SearchQueryRequest[] getSearchQueryRequests() {
        return searchQueryRequests;
    }

    public void setSearchQueryRequests(SearchQueryRequest[] searchQueryRequests) {
        this.searchQueryRequests = searchQueryRequests;
    }

    public QueryPhraseEnum getQueryPhraseEnum() {
        return queryPhraseEnum;
    }

    public void setQueryPhraseEnum(QueryPhraseEnum queryPhraseEnum) {
        this.queryPhraseEnum = queryPhraseEnum;
    }

    public static class QueryData {
        private String name;
        private Object[] values;
        private float boost = 1.0f;
        private boolean isRnage = false;
        private QueryRange start;
        private QueryRange end;
        private int mimShouldMatch = -1;
        private String operator;
        private float tieBreaker = 0.0f;  //用于dis_max

        public QueryData(String name, Object... values) {
            this.name = name;
            this.values = values;
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

        public Object[] getValues() {
            return values;
        }

        public void setValues(Object[] values) {
            this.values = values;
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

        public int getMimShouldMatch() {
            return mimShouldMatch;
        }

        public void setMimShouldMatch(int mimShouldMatch) {
            this.mimShouldMatch = mimShouldMatch;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public float getTieBreaker() {
            return tieBreaker;
        }

        public void setTieBreaker(float tieBreaker) {
            this.tieBreaker = tieBreaker;
        }

    }

    public static class MultiQueryData extends QueryData {

        private String value;
        private String[] fields;
        private String type;

        public MultiQueryData(String value, String... fields) {
            super(value,fields);
            this.value = value;
            this.fields = fields;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String[] getFields() {
            return fields;
        }

        public void setFields(String[] fields) {
            this.fields = fields;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
