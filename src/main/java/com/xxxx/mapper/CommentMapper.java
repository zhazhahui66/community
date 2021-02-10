package com.xxxx.mapper;

import com.xxxx.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
@Mapper
@Repository
public interface CommentMapper {

    Comment selectCommentById(int id);

    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);
    int selectCountByEntity(@Param("entityType")int entityType,@Param("entityId") int entityId);

    int insertComment(Comment comment);
}
