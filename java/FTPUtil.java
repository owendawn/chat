package com.zone.test.test;

/**
 * Created by Owen Pan on 2016/11/28.
 */

import com.alibaba.fastjson.util.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FTPUtil {
    private static FTPClient ftpClient;

    /**
     * 连接ftp服务器
     *
     * @param ip       FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @return boolean
     */
    private static boolean connnectFTPClient(String ip, int port, String username, String password) {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("GBK");
        int reply;
        try {
            ftpClient.connect(ip, port);
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 断开ftp服务器
     */
    private static void disConnectFTPClient() {
        try {
            ftpClient.logout();
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在ftp服务器保存文件
     *
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return boolean
     */
    private static boolean saveFile(String path, String filename, InputStream input) {
        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.makeDirectory(path);
            ftpClient.changeWorkingDirectory(path);
            ftpClient.storeFile(filename, input);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 通过输入流在ftp服务器上保存文件
     *
     * @param ip       FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param path     FTP服务器保存目录
     * @param filename 上传到FTP服务器上的文件名
     * @param in       输入流
     */
    public static void uploadByStream(String ip, int port, String username, String password, String path, String filename, InputStream in) {
        try {
            if (!connnectFTPClient(ip, port, username, password)) {
                return;
            }
            boolean flag = saveFile(path, filename, in);
            System.out.println("上传到FTP服务器成功？" + flag);
            disConnectFTPClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将本地文件上传到FTP服务器上
     *
     * @param ip            FTP服务器hostname
     * @param port          FTP服务器端口
     * @param username      FTP登录账号
     * @param password      FTP登录密码
     * @param path          FTP服务器保存目录
     * @param filename      上传到FTP服务器上的文件名
     * @param orginfilename 输入流文件名
     */
    public static void upLoadByFile(String ip, int port, String username, String password, String path, String filename, String orginfilename) {
        FileInputStream in = null;
        try {
            System.out.println("FTP要上传文件存在？" + new File(orginfilename).exists());
            in = new FileInputStream(new File(orginfilename));
            uploadByStream(ip, port, username, password, path, filename, in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(in);
        }
    }

    /**
     * 获取ftp服务器上文件
     *
     * @param remotePath     远程文件路径
     * @param remoteFileName 远程文件名
     */
    private static FTPFile getRemoteFile( String remotePath, String remoteFileName) {
        try {
            ftpClient.changeWorkingDirectory(remotePath);//转移到FTP服务器目录
            FTPFile[] fs = ftpClient.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(remoteFileName)) {
                    return ff;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载ftp服务器上文件流
     *
     * @param ip             ftp连接ip地址
     * @param port           ftp连接端口
     * @param username       ftp连接用户名
     * @param password       ftp连接密码
     * @param remotePath     远程文件路径
     * @param remoteFileName 远程文件名
     */
    public static InputStream downloadFileStream(String ip, int port, String username, String password, String remotePath, String remoteFileName) {
        try {
            if (!connnectFTPClient(ip, port, username, password)) {
                throw new RuntimeException("ftp连接异常");
            }
            FTPFile ff = getRemoteFile(remotePath, remoteFileName);
            if(ff!=null) {
                return ftpClient.retrieveFileStream(ff.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disConnectFTPClient();
        }
        return null;
    }

    /**
     * 下载ftp服务器上文件
     *
     * @param ip             ftp连接ip地址
     * @param port           ftp连接端口
     * @param username       ftp连接用户名
     * @param password       ftp连接密码
     * @param remotePath     远程文件路径
     * @param remoteFileName 远程文件名
     * @param localPath      本地文件路径
     * @param localFileName  本地文件名
     */
    public static void downloadFile(String ip, int port, String username, String password, String remotePath, String remoteFileName, String localPath, String localFileName) {
        try {
            if (!connnectFTPClient(ip, port, username, password)) {
                throw new RuntimeException("ftp连接异常");
            }
            FTPFile ff = getRemoteFile(remotePath, remoteFileName);
            if(ff!=null) {
                String fn = ff.getName();
                if (localFileName != null) {
                    fn = localFileName;
                }
                File localFile = new File(localPath + "/" + fn);
                OutputStream is = new FileOutputStream(localFile);
                ftpClient.retrieveFile(ff.getName(), is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            disConnectFTPClient();
        }
    }

    /**
     * 下载ftp服务器上文件
     *
     * @param ip             ftp连接ip地址
     * @param port           ftp连接端口
     * @param username       ftp连接用户名
     * @param password       ftp连接密码
     * @param remotePath     远程文件路径
     * @param remoteFileName 远程文件名
     * @param localPath      本地文件路径
     */
    public static void downloadFile(String ip, int port, String username, String password, String remotePath, String remoteFileName, String localPath) {
        downloadFile(ip, port, username, password, remotePath, remoteFileName, localPath, null);
    }

    //测试
    public static void main(String[] args) {
        upLoadByFile("192.168.1.203", 21, "sand", "sand", "one", "hanshibo.txt", "d:/poe.txt");
        downloadFile("192.168.1.203", 21, "sand", "sand", "one", "hanshibo.txt", "d:/", "kk.txt");
    }
}