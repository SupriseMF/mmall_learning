package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by SupriseMF
 * Date: 2018-07-17
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {

        //用户名存在性验证
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在！");
        }
        //密码登录MD5，加密明文密码，“不可逆”
        String md5Password = MD5Util.MD5EncodeUtf8(password);

        //当业务执行到此即通过用户名验证，再验证用户名和密码是否对照
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误！");
        }

        //此处处理返回值的密码
        //此处IDEA自动将org.apach.commons.lang3.StringUtils.EMPTY简为StringUtils.EMPTY
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    public ServerResponse<String> register(User user) {
        //防止因自动调用接口进入，此处仍需验证用户名是否已存在
        //原来的实现：判断用户名
//        int resultCount = userMapper.checkUsername(user.getUsername());
//        if (resultCount > 0) {
//            return ServerResponse.createByErrorMessage("用户名已存在！");
//        }
        //将checkValid()与register复用
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }

        //原来的实现：判断邮箱
//        resultCount = userMapper.checkEmail(user.getEmail());
//        if (resultCount > 0) {
//            return ServerResponse.createByErrorMessage("此邮箱已存在！");
//        }

        user.setRole(Const.Role.ROLE_CUSTOMER);

        //下面是MD5加密明文密码->完成一个MD5的Util类
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //将用户加入至数据库
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败！");
        }
        return ServerResponse.createBySuccessMessage("注册成功！");
    }


    public ServerResponse<String> checkValid(String str, String type) {
        //根据type判断注册类型
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在！");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("此邮箱已存在！");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误！");
        }
        return ServerResponse.createBySuccessMessage("校验成功！");
    }

    public ServerResponse selectQuestion(String username) {
        //原来的实现：
//        int resultCount = userMapper.checkUsername(username);
        //代码复用
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //通过上层校验，此时用户为不存在
            return ServerResponse.createByErrorMessage("用户不存在！");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题为空！");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            /*
            说明查到相对应的数据，正确符合该用户
            TODO:详情自查，UUID重复概率几乎为零
            */
            String forgetToken = UUID.randomUUID().toString();
            //将forgetToken放入本地cache中，并设置有效期

            //因已配置好LocalCache,可调用TokenCache
            //token_做前缀/namespace以作区分,并放入本地缓存，此前缀可做成常量，提高效率
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误！");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        //校验参数
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，Token需要传递！");
        }
        //由于"token_" + username有前缀，故而username不能为空
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在！");
        }
        //从cache中获取token，token_前缀可做成常量，提高效率（此处仅为加深印象）
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        //对cache中token做校验
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或已过期！");
        }
        //TODO:StringUtils.equals()判断更加安全，其中的两参数进行“或”判断防止空指针情况，且避免报异常
        if (StringUtils.equals(forgetToken, token)) {
            //校验通过，重置密码
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            //写入新密码,使用MD5密码写入数据库，不可以是明文密码
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            //做生效行数的判断
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功！");
            }
        } else {
            //else即返回的token虽然在有效期，但未校验对应成功，返回错误
            return ServerResponse.createByErrorMessage("token错误，请再次输入或获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败！");
    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权，因会查count(1)故而需指定该用户，再去校验密码
        //其中传入密码也需为MD5密码
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("输入的旧密码错误！");
        }
        //通过校验，设置新密码
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        //为了提高拓展性，只根据主键去更新->有选择性地更新选项
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功！");
        }
        return ServerResponse.createByErrorMessage("密码更新失败！");
    }

    public ServerResponse<User> updateInformation(User user) {
        //当更新时，username是不能被更新的
        //email校验：校验其是否存在，若存在且不能对应当前用户
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            //说明邮箱已经存在,且不是该用户
            return ServerResponse.createByErrorMessage("该邮箱已经存在，请尝试更换邮箱进行更新!");
        }
        //通过验证，声明一个updateUser仅用于更新
        //其中update_time已经设置为内置函数now()，此处不用多虑
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功！", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败！");

    }

    public ServerResponse<User> getInformation(Integer userId) {
        //通过userId找到user
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户！");
        }
        //找到则将密码置空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    //后端backend
    /**
     * 登录用户校验是否为管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


}
