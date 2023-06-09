package com.elastic.demoelasticSearch.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchRequestDTO {
    private List<String> fields;
    private String searchTerm;
}
