package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * created by SupriseMF
 * date:2018-07-20
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //返回上传的文件名
    public String upload(MultipartFile file,String path) {
        //获取原始文件名
        String fileName = file.getOriginalFilename();
        //获取扩展名，不带“.”
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        //因不能出现图片名重复
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        //logback允许使用占位符{}输出信息
        logger.info("开始上传文件，上传文件的文件名：{}，上传的路径为：{}，新文件名：{}",fileName,path,uploadFileName);
        //file的文件夹
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            //目录不存在则创建
            //赋予“写”权限
            fileDir.setWritable(true);
            //mkdirs()与mkdir()区别：前者可以直接创多级深层次目录，后者只能在当前目录下创建新目录
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);

        //使用springMVC封装的file调用
        try {
            file.transferTo(targetFile);
            //此时文件上传成功，接下需将targetFile上传到FTP服务器
            //使用guava将targetfile转换为List类型
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传至ftp服务器
            //上传后将upload中tomcat中的临时文件删除
            targetFile.delete();


        } catch (IOException e) {
            logger.error("上传文件异常！", e);
            return null;
        }
        return targetFile.getName();

    }
}
