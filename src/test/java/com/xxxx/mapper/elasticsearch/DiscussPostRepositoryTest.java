package com.xxxx.mapper.elasticsearch;

import com.xxxx.mapper.DiscussPostMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.AbstractHighlighterBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class DiscussPostRepositoryTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Test
    void testAddDocument(){
        discussPostRepository.save(discussPostMapper.selectDiscussPost(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPost(243));
        discussPostRepository.save(discussPostMapper.selectDiscussPost(244));
    }
    @Test
    void testAddAllDocument(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,30));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,30));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,30));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,30));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(149,0,30));
    }
    @Test
    void testQuery() throws IOException {
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("discusspost");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(1);
        sourceBuilder.size(1000);

        //精准匹配
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("计划", "title", "content");
        sourceBuilder.query(multiMatchQuery);
       /* TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", "国");
        sourceBuilder.query(termQueryBuilder);
        TermQueryBuilder termQueryBuilder2 = QueryBuilders.termQuery("content", "国");
        sourceBuilder.query(termQueryBuilder2);*/
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();

        highlightBuilder.field("title").field("content");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        sourceBuilder.highlighter(highlightBuilder);



        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        ArrayList<Map<String,Object>> arrayList = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {

            //解析高亮的字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            HighlightField content = highlightFields.get("content");

            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();

            //解析高亮字段
            if(title!=null){
                Text[] fragments = title.fragments();
                StringBuilder newTitle = new StringBuilder();
                for (Text text : fragments) {
                    newTitle.append(text);
                }
                sourceAsMap.put("title",newTitle);
            }
            if(content!=null){
                Text[] texts = content.fragments();
                StringBuilder newContent = new StringBuilder();
                for (Text text : texts) {
                    newContent.append(text);
                }
                sourceAsMap.put("content",newContent);
            }

            arrayList.add(sourceAsMap);
        }
        for (Map<String, Object> stringObjectMap : arrayList) {
            System.out.println(stringObjectMap);
        }

    }
}