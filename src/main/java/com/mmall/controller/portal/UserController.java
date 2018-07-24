package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpSession;

/**
 * Created by SupriseMF
 * Date: 2018-07-17
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;  //iUserService需与UserServiceImpl.java中的注解Service的id对应

    /**
     * 用户登录，用户相关接口全部使用POST提交
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        //流程service->mybatis->dao

        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
        //至此用户登录结束
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> logout(HttpSession session) {

        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }//用户登出结束

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息！");
    }

    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        //其service先用guava，用本地缓存做token，其中利用有效期
        return iUserService.checkAnswer(username, question, answer);
    }

    //FIXME:名字有问题！！！讲师的名字拼错了（-.-！）
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        //判断当前用户
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录！");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }


    //返回值为User，更新完个人信息后，需放入session并返回至前端
    /**
     * 小结：为防止横向越权：1.从session获取当前用户，指定为当前的userId
     *                     2.在updateInformation时，username也不能更新
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session, User user) {
        //在updateinformation时需getinformation
        //判断当前用户
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录！");
        }
        //传进来的user没带userId,需指定为当前用户的userId
        user.setId(currentUser.getId());
        //在updateInformation时，username未更新，故而需要设置为当前用户的username
        user.setUsername(currentUser.getUsername());
        ServerResponse response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            //成功则更新session
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        //失败则不对response做修改
        return response;
    }

    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session) {
        //参数指定为session，需从session判断是否为当前用户

        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            //未登录，需进行强制登录，此时status = 10,与前端对应的
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需进行登录！");
        }
        //从当前用户获得并返回个人信息，并不需要返回个人密码（密码置空）
        return iUserService.getInformation(currentUser.getId());
    }







}
