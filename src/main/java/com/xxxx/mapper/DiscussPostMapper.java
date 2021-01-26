package com.xxxx.mapper;

import com.xxxx.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    int selectDiscussPostRows(@Param("userId") int userId);



}
