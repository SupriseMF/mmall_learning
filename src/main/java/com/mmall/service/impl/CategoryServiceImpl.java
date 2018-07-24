package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.finger.FingerClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import sun.text.CollatorUtilities;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * created by SupriseMF
 * date:2018-07-19
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    //LoggerFactory为该类生成日志
    private org.slf4j.Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName, Integer parentId) {
        //参数校验
        //StringUtils.isBlank()详情自查
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加品类参数错误！");
        }
        //添加品类
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//表示可用

        //更新到数据库时用到CategoryMapper，需要自动装载注入进来

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败！");
    }

    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新品类参数错误！");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        //！！！updateByPrimaryKeySelective()只更新上面重新设置的categoryId和categoryName
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败！");
    }

    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        //CollectionUtils.isEmpty()源码-->>collection == null || collection.isEmpty()不仅判断集合存在还判断是否空集合
        if (CollectionUtils.isEmpty(categoryList)) {
            //若为空即category下无内容，不需返回错误提醒了；记录日志即可。
            logger.info("未找到当前分类的子分类！");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        //用到递归函数，在下面方法中实现
        //Sets是Guava提供的强大的集合操作类。Guava详情自查！！！
        Set<Category> categorySet = Sets.newHashSet();//初始化
        //调用递归，结果在categorySet中
        findChildCategory(categorySet, categoryId);
        //Lists是Guava提供的强大的列表操作类。Guava详情自查！！！
        List<Integer> categoryList = Lists.newArrayList();
        if (categoryId != null) {
            //所有节点放入categoryList
            for (Category categoryItem : categorySet) {
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }


    //递归函数
    //由于Categor类型不像java普通Object对象，该Set无内置的hashcode()及equals()，故需重写该两方法
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }
        //遍历查找后代节点categoryItem，并设置递归出口==>>当子节点列表都为空则退出递归
        //categoryList为mybatis返回的集合-->若未查到则也不会返回null对象！！！
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        //当categoryList为空列表时，不会进入for循环，而继续执行return。
        for (Category categoryItem : categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
