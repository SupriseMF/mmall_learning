package com.mmall.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * created by SupriseMF
 * date:2018-07-20
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    //唯一开放的方法：文件上传是否成功
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPass);
        logger.info("开始连接FTP服务器!");
        //指定img文件路径
        //根据业务层级的安排来进行异常的trycatch或抛出
        boolean result = ftpUtil.uploadFile("img",fileList);
        logger.info("连接FTP服务器，结束上传，上传结果为：{}!",result);
        return result;
    }

    //上传的具体逻辑
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        //remotePath为远程路径，由于ftp服务器在linux上是一个文件夹，当需要上传到ftp文件夹下的文件夹下时，即需要remotePath，以增多上传的路径
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接ftp服务器
        if (connectServer(this.ip, this.port, this.user, this.pwd)) {
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                //设置缓冲区大小1024字节
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                //设置文件类型->二进制，防止乱码
                ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
                //打开本地被动模式
                ftpClient.enterLocalPassiveMode();
                for (File FileItem : fileList) {
                    fis = new FileInputStream(FileItem);
                    //通过ftpClient存储file
                    ftpClient.storeFile(FileItem.getName(), fis);
                }
            } catch (IOException e) {
                uploaded = false;
                logger.error("上传文件异常！", e);
            }finally {
                //无论上传是否成功，最后要关闭流并释放上传连接
                fis.close();//关闭关闭流中的IOException
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    private boolean connectServer(String ip, int port, String user, String pwd) {
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            //连接并登录
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常！", e);
        }
        return isSuccess;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
