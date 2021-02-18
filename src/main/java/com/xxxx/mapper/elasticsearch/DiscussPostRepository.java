package com.xxxx.mapper.elasticsearch;

import com.xxxx.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
