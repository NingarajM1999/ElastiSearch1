package com.elastic.demoelasticSearch.search.util;

import com.elastic.demoelasticSearch.search.SearchRequestDTO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class SearchUtil {

    private SearchUtil(){}

    public static SearchRequest buildSearchRequest(String indexName, SearchRequestDTO dto){
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .postFilter(getQueryBuilder(dto));

            SearchRequest request = new SearchRequest(indexName);
            request.source(builder);
            return request;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static QueryBuilder getQueryBuilder(SearchRequestDTO dto){
        if(dto==null){
            return null;
        }
        final List<String> fields=dto.getFields();
        if (CollectionUtils.isEmpty(fields)){
            return null;
        }
        if (fields.size()>1){
            MultiMatchQueryBuilder queryBuilder= QueryBuilders.multiMatchQuery(dto.getSearchTerm())
                    .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                    .operator(Operator.AND);
            fields.forEach(queryBuilder::field);
            return queryBuilder;
        }
        return fields.stream().findFirst().map(field->QueryBuilders.matchQuery(field,dto.getSearchTerm()).operator(Operator.AND)).orElse(null);
    }

}
