package com.xxxx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket implements Serializable {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;


}
