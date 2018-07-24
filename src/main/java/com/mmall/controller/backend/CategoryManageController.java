package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * created by SupriseMF
 * date:2018-07-19
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {


    //将IUserService自动装载，并导入相应package
    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId",defaultValue = "0") int parentId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员->增加处理分类的逻辑
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限!");
        }
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //设置更新categoryName的业务逻辑
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限!");
        }
    }

    //根据传参数categoryId获取当前category下子节点信息，平级，无递归
    //默认value为0的为根节点
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //查询子节点的category信息，不递归，保持平级。
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限!");
        }

    }

    //根据传参数categoryId获取当前category下子节点信息，递归查询！
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请登录！");
        }
        //校验是否为管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //查询当前节点并递归查询所有**后代节点**的category信息
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无权限操作，需要管理员权限!");
        }
    }

}
