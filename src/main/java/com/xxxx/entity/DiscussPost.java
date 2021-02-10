package com.xxxx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscussPost implements Serializable {

    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private int commentCount;
    private Date  createTime;
    private double score;
}
