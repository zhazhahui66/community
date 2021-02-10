package com.xxxx.mapper;

import com.xxxx.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Mapper
@Repository
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    int selectDiscussPostRows(@Param("userId") int userId);

    int publishDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPost(int id);

    int updateCommentCount(@Param("id")int id,@Param("commentCount")int commentCount);

    int updateStatus(@Param("id")int id,@Param("status")int status);
    int updateType(@Param("id")int id,@Param("type")int type);
}
