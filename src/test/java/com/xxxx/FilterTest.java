package com.xxxx;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FilterTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Test
    public void test1(){
        String s = sensitiveFilter.filter("我赌博赢了七千万天天去嫖娼吸毒哈哈哈哈");
        System.out.println(s);
    }

    @Test
    void test2(){
        Map<String, Object> map = new HashMap<>();
        map.put("username","zhangsan");
        map.put("age",17);

        String jsonString = CommunityUtil.getJSONString(0, "nihc", map);
        JSONObject jsonObject = new JSONObject();
        System.out.println(jsonString);
    }

    @Test
    void test(){

    }
}
