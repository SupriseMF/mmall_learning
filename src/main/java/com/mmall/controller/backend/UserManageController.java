package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * created by SupriseMF
 * date:2018-07-18
 */

//后台管理全用/manage做前缀
@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        //后台管理员登录
        //复用普通用户的一些方法
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            //请求成功
            User user = response.getData();
            //判断是否为管理员
            if (user.getRole() == Const.Role.ROLE_ADMIN) {
                //放入session
                session.setAttribute(Const.CURRENT_USER, user);
                return response;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员，无法登录！");
            }
        }
        //请求失败,不做处理
        return response;
    }
}
