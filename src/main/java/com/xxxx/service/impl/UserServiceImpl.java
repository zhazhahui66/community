package com.xxxx.service.impl;

import com.xxxx.entity.LoginTicket;
import com.xxxx.entity.User;
import com.xxxx.mapper.LoginTicketMapper;
import com.xxxx.mapper.UserMapper;
import com.xxxx.service.UserService;
import com.xxxx.util.CommunityConstant;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.MailClient;
import com.xxxx.util.RedisKeyUtil;
import org.apache.ibatis.annotations.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService, CommunityConstant {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;
    //@Autowired
    //private LoginTicketMapper loginTicketMapper;
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User selectById(int id) {
        //return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }

    @Override
    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public int updateStatus(int id, int status) {
        return userMapper.updateStatus(id,status);
    }

    @Override
    public int updatePassword(int id, String password) {
        return userMapper.updatePassword(id,password);
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        Assert.notNull(user,"参数不能为空!");
        if(StringUtils.isEmpty(user.getUsername())){
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if(StringUtils.isEmpty(user.getPassword())){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if(StringUtils.isEmpty(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg","该账号已被注册");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format(" http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        Context context = new Context();
        context.setVariable("meail",user.getEmail());
        String path = domain + contextPath + "/activation/" + user.getId() +"/"+ user.getActivationCode();
        context.setVariable("url",path);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)) {
            userMapper.updateStatus(user.getId(), 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        HashMap<String, Object> map = new HashMap<>();

        //空值判断
        if(StringUtils.isEmpty(username)){
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isEmpty(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        //生成登录凭证
        LoginTicket ticket = new LoginTicket();
        ticket.setStatus(0);
        ticket.setUserId(user.getId());
        ticket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds * 1000));
        ticket.setTicket(CommunityUtil.generateUUID());
        //loginTicketMapper.insertLoginTicket(ticket);
        //把凭证存入redis
        String ticketKey = RedisKeyUtil.getTicketKey(ticket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,ticket);


        map.put("ticket",ticket.getTicket());
        return map;
    }

    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1 );
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }
    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);

        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)  redisTemplate.opsForValue().get(ticketKey);

    }
    public int updateHeader(int userId,String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    //优先从缓存中取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //2.取不到是初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    //数据更变是清除缓存
    private void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    //
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.selectById(userId);
        ArrayList<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return CommunityConstant.AUTHORITY_ADMIN;
                    case 2:
                        return CommunityConstant.AUTHORITY_MODERATOR;
                    default:
                        return CommunityConstant.AUTHORITY_USER;
                }

            }
        });
        return list;

    }
}
