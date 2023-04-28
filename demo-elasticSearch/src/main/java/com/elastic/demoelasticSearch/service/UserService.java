package com.elastic.demoelasticSearch.service;

import com.elastic.demoelasticSearch.model.User;
import com.elastic.demoelasticSearch.search.SearchRequestDTO;
import com.elastic.demoelasticSearch.search.util.SearchUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class UserService {

    private ObjectMapper objectMapper;
    private RestHighLevelClient highLevelClient;

    @Value("${elasticsearch.index.name}")
    private String index;
    @Autowired
    public UserService(ObjectMapper objectMapper, RestHighLevelClient highLevelClient) {
        this.objectMapper = objectMapper;
        this.highLevelClient = highLevelClient;
    }

    //CREATE
    public String createUser(User user) throws IOException {
  Map<String,Object> userMapper = objectMapper.convertValue(user,Map.class);
        IndexRequest indexRequest=new IndexRequest(index).id(user.getId()).source(userMapper);
        IndexResponse indexResponse=highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.getResult().name();
    }

    //GET_BY_ID
    public User findById(String id) throws IOException {
        GetRequest request=new GetRequest(index,id);
        GetResponse response=highLevelClient.get(request,RequestOptions.DEFAULT);
        Map<String,Object> result=response.getSource();
        return objectMapper.convertValue(result, User.class);
    }

    //UPDATE
    public String updateUser(User user,String id) throws IOException {
        User user1=findById(id);
        UpdateRequest updateRequest=new UpdateRequest(index,user1.getId());
        Map<String,Object> objectMap=objectMapper.convertValue(user, Map.class);
        updateRequest.doc(objectMap);
        UpdateResponse updateResponse=highLevelClient.update(updateRequest,RequestOptions.DEFAULT);
        return updateResponse.getResult().name();
    }

    //DELETE
    public String deleteUser(String id) throws IOException {
        User user=findById(id);
        DeleteRequest deleteRequest = new DeleteRequest(index,id);
        DeleteResponse deleteResponse=highLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
        return deleteResponse.getResult().name();
    }

    //GET_ALL
    public List<User> getAllUser() {
       SearchRequest searchRequest=new SearchRequest();
       searchRequest.indices(index);
       SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
       sourceBuilder.query(QueryBuilders.matchAllQuery());
       searchRequest.source(sourceBuilder);
       List<User> userList = new ArrayList<>();
       SearchResponse searchResponse=null;
       try {
       searchResponse=highLevelClient.search(searchRequest,RequestOptions.DEFAULT);
       if(searchResponse.getHits().getTotalHits().value>0){
           SearchHit[] searchHit=searchResponse.getHits().getHits();
           for (SearchHit hit:searchHit){
               Map<String,Object> map=hit.getSourceAsMap();
               userList.add(objectMapper.convertValue(map,User.class));

           }
       }
       }catch (IOException e){
           e.printStackTrace();
       }
        return userList;
    }



    public List<User> getSearchResult(SearchResponse searchResponse)
    {
        SearchHit[] searchHit = searchResponse.getHits().getHits();
        List<User> users=new ArrayList<>();
        if(searchHit.length>0){
            Arrays.stream(searchHit).forEach(hit->users.add(objectMapper.convertValue(hit.getSourceAsMap(),User.class)));
        }
        return users;
    }

 public List<User> search(SearchRequestDTO dto){
        SearchRequest request = SearchUtil.buildSearchRequest(index,dto);
        if (request==null){
            log.error("failed to build search request");
            return Collections.emptyList();
        }

        try {
            SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHit[] searchHits=response.getHits().getHits();
            List<User> userList=new ArrayList<>(searchHits.length);
            for (SearchHit hit:searchHits){
                userList.add(
                        objectMapper.readValue(hit.getSourceAsString(), User.class)
                );
            }
            return userList;
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return Collections.emptyList();
        }
    }


    public List<User> getUserByName(String firstName) {
        SearchRequest searchRequest=new SearchRequest();
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("firstName.keyword",firstName)));
        searchRequest.source(searchSourceBuilder);
        List<User> userList=new ArrayList<>();
        SearchResponse searchResponse=null;
        try {
            searchResponse=highLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            if(searchResponse.getHits().getTotalHits().value>0){
                SearchHit[] searchHit=searchResponse.getHits().getHits();
                for (SearchHit hit:searchHit){
                    Map<String,Object> map=hit.getSourceAsMap();
                    userList.add(objectMapper.convertValue(map, User.class));
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return userList;
    }
}
