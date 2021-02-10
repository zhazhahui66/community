package com.xxxx.controller;

import com.google.code.kaptcha.Producer;
import com.sun.org.apache.xpath.internal.operations.Mod;
import com.xxxx.entity.User;
import org.apache.commons.lang3.StringUtils;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.UserServiceImpl;
import com.xxxx.util.CommunityConstant;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class LoginController implements CommunityConstant {
    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserServiceImpl userService;


    @Autowired
    private RedisTemplate redisTemplate;
    @GetMapping("/toRegister")
    public String toRegister(){
        return "/site/register";
    }
    @RequestMapping(path = "/login",method = {RequestMethod.GET})
    public String toLogin(){
        return "/site/login";
    }
    @PostMapping("/register")
    public String register(Model model, User user,String confirm_password){
        Map<String, Object> map = userService.register(user);

        if(!user.getPassword().equals(confirm_password)){
            model.addAttribute("rePasswordMsg", "密码不一致！");
            return "/site/register";
        }

        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,你得账号已经可以正常使用了!");
            model.addAttribute("target","/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //session.setAttribute("kaptcha",text);
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/");
        response.addCookie(cookie);

        //将验证码C存入Redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);

        //设置返回类型
        response.setContentType("image/png");
        try {
            //把图片写入流中
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("响应验证码失败 "+e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username,String password,String code,boolean rememberme,
                    Model model,HttpSession session,HttpServletResponse response,@CookieValue(value = "kaptchaOwner",required = false) String kaptchaOwner){
        //检查验证码
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if(StringUtils.isEmpty(kaptcha)|| StringUtils.isEmpty(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确!");
            return  "/site/login";
        }

        //检查账号,密码
        int expiredSeconds = rememberme ?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath("/");
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }
    @RequestMapping(path = "/logout",method = {RequestMethod.GET,RequestMethod.POST})
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
