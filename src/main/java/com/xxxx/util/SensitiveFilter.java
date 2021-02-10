package com.xxxx.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensitiveFilter {
    //替换符
    private static final String REPLACEMENT = "***";
    //根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while ((keyword = reader.readLine()) != null){
                //添加到前缀树
                this.addKeyword(keyword);
            }

             } catch (Exception e) {
            log.error("加载敏感词文件失败: " + e.getMessage());
        }
    }
    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = this.rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指向子节点,进入下一轮循环
            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length() -1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /*
    * 过滤敏感词
    * @param text 代过滤的文本
    * @return 过滤后的文本
    * */
    public String filter(String text){
        if(StringUtils.isEmpty(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();
        //跳过符号
        while (position <text.length()){
            char c = text.charAt(position);

            if(isSymbol(c)){
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            //检查下一级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                sb.append(text.charAt(begin));

                position = ++begin;

                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin =++position;
                tempNode = rootNode;
            }else {
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    private class TrieNode{
        private boolean isKeywordEnd = false;

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

        public void addSubNode(Character c,TrieNode node) {
             subNodes.put(c,node);
        }
    }
}
