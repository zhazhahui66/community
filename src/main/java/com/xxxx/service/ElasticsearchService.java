package com.xxxx.service;

import com.xxxx.entity.DiscussPost;
import com.xxxx.mapper.DiscussPostMapper;
import com.xxxx.mapper.elasticsearch.DiscussPostRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticsearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    public void saveDiscussPost(DiscussPost discussPost){
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    public List<Map<String, Object>> searchDiscussPost(String keyword,int pageNo,int pageSize) throws IOException {

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("discusspost");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(keyword, "title", "content");
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

//            Date createTime = new Date((Integer) sourceAsMap.get("createTime"));
//            sourceAsMap.put("createTime",createTime);

//            String createTime = sourceAsMap.get("createTime").toString();
//            sourceAsMap.put("createTime",new Date(Long.valueOf(createTime)));

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
       return arrayList;
    }
}
