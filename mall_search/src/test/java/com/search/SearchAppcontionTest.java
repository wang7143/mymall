package com.search;


import com.alibaba.fastjson.JSON;
import com.search.config.ElasticConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class SearchAppcontionTest {


    @Autowired
    private RestHighLevelClient client;


    @Test
    public void contextLoads(){
        System.out.println(client);
    }


    @Test
    public void serach() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        //指定检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //构建检索条件
        sourceBuilder.query(QueryBuilders.matchQuery("@timestamp","131883573237140000"));

        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("entity_id").size(2);
        sourceBuilder.aggregation(ageAgg);

        
        System.out.println(sourceBuilder.toString());
        searchRequest.source(sourceBuilder);


        SearchResponse search = client.search(searchRequest, ElasticConfig.COMMON_OPTIONS);

        System.out.println(search);

    }

    @Test
    public void indexData() throws IOException {
        IndexRequest users = new IndexRequest("users");
        users.id("1");
//        users.source("userName","zhangsan","age",18,"gender","男");
        User user = new User("zhangsan","男",18);
        String s = JSON.toJSONString(user);
        users.source(s, XContentType.JSON);

        IndexResponse index = client.index(users, ElasticConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    @AllArgsConstructor
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }
}