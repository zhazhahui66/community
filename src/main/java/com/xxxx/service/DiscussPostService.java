package com.xxxx.service;

import com.xxxx.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface DiscussPostService  {
    List<DiscussPost> findDiscussPosts(int userId,int offset,int limit);

    int findDiscussPostRows(int userId);

    int addDiscussPost(DiscussPost post);

    DiscussPost findDiscussPostsById(int id);

    int updateCommentCount(int id, int commentCount);
    int updateStatus(int id,int status);
    int updateType(int id,int type);
}
