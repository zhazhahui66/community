package com.xxxx.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }

    //MD5加密
    public static String md5(String key){
        if(StringUtils.isEmpty(key)){
            return null;
        }
        //返回十六进制的MD5加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code , String msg, Map<String, Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("msg",msg);
        if (map != null) {
            map.forEach(jsonObject::put);
        }
        return jsonObject.toJSONString();
    }
    public static String getJSONString(int code,String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }
}
