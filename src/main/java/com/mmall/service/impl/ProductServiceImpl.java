package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    //平级调用，Service之间互调
    @Autowired
    private ICategoryService iCategoryService;

    //一个接口实现商品保存和更新功能
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                //当子图不为空，取第一个子图付给主图
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId() != null) {
                //如果是更新，则product.getId() != null，更新整个product
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("更新产品成功！");
                } else {
                    return ServerResponse.createBySuccess("更新产品失败！");
                }
            } else {
                //id为null，则新增产品
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("新增产品成功！");
                } else {
                    return ServerResponse.createBySuccess("新增产品失败！");
                }
            }
        }
        return ServerResponse.createByErrorMessage("新增或保存产品参数错误！");
    }

    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("修改产品销售状态成功！");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败！");
    }

    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已被下架或删除！");
        }
        //简单的业务逻辑POJO--->VO对象=>value object承载对象个个值的作用
        // 复杂的业务逻辑POJO--->BO(Bussiness)---->VO(View Object)
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setMainImage(product.getMainImage());

        //imageHost需要从配置文件中获取：配置与代码分离，减少硬编码
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));

        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            //为空则其为根节点
            productDetailVo.setParentCategoryId(0);
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        //createTime从数据库从拿出来时为毫秒数，不利于阅读，需将时间格式化
        productDetailVo.setCreatedTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        //pagehelper用法：step.1:记录startPage，以作开始；step.2：填充自己的SQL查询逻辑；step.3：pagehelper的收尾
        //step.1:
        //startPage()有许多重载方法
        PageHelper.startPage(pageNum, pageSize);
        //step.2:
        List<Product> productList = productMapper.selectList();

        //此时既不想将Product的全部信息返回前端展示，但还要用到ProductList进行分页，此时将其List重置
        List<ProductListVo> productListVoList = Lists.newArrayList();

        //其中List不需要product的全部详细信息，只需要mainImage、name、id、categoryId、price、status、subtitle、imageHost。因此需重新创建一个productList的VO
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);//实现转换
        }
        //step.3:收尾：根据SQL返回的集合传入参数，自动分分页处理
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }


    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();

        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProuct(String produtName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(produtName)) {
            //SQL查询构建productname
            produtName = new StringBuilder().append("%").append(produtName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(produtName, productId);

        List<ProductListVo> productListVoList = Lists.newArrayList();

        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);//实现转换
        }
        //分页
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }


    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已被下架或删除！");
        }

        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已被下架或删除！");
        }

        //简单的业务逻辑POJO--->VO对象=>value object承载对象个个值的作用
        // 复杂的业务逻辑POJO--->BO(Bussiness)---->VO(View Object)
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }


    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, int pageNum, int pageSize,String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEAGLE_ARGUMENT.getCode(), ResponseCode.ILLEAGLE_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();
        //当查询大分类时，调用递归算法，深度查询所有的后代分类并包括本身，放入categoryIdList

        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                //没有该分类，且还未查到关键词，此时返回空结果集，不必报错！
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                //如果结果集没有变化的话，List直接使用构造器的方法
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            //由于selectCategoryAndChildrenById定义时未声明泛型，无法转为List<Integer>类型，最后加上.getData()即可
            //拿到分类
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        if (StringUtils.isNotBlank(keyword)) {
            //keyword不为空，则拼接关键词
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        //排序处理
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(orderBy)) {
            //动态排序
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                //由于PageHelper.orderBy()中参数以空格做为分割
                PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
            }
        }
        //搜索product，其中keyword就是productName
        //由于上面已经new过不为空，且给categoryIdList赋值，采用三元运算符做判断
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword) ? null:keyword, categoryIdList.size() == 0 ? null:categoryIdList);

        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        //分页；把SQL的执行放入PageInfo
        PageInfo pageInfo = new PageInfo(productList);
        //将其集合置为ListVo
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
