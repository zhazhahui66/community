package com.xxxx.controller;

import com.xxxx.annotation.LoginRequired;
import com.xxxx.entity.User;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.FollowService;
import com.xxxx.service.impl.LikeService;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Controller
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;


    @LoginRequired
    @GetMapping("/setting")
    public String setting(){
        return "/site/setting";
    }
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","你还没有选择图片!");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isEmpty(suffix)){
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //确认文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }
        //更新当前用户的头像的路径(web访问路径)
        User user = hostHolder.getUser();
        // http://localhost:8080/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" +filename;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }

    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/"+suffix);

        try (
                FileInputStream fis = new FileInputStream(fileName);
                ServletOutputStream os = response.getOutputStream();
        ){
            byte[] buffer = new byte[1024];
            int len = 0;
            while ( (len = fis.read(buffer)) != -1){
                os.write(buffer,0,len);
            }

        }catch (Exception e) {
           log.error("读取头像失败"+ e.getMessage());
        }
    }
    //个人注页
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, 3);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(3, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() !=null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), 3, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }


}
