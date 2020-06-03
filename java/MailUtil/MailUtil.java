package com.hh.backend.utils;


import com.hh.backend.base.common.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;


public class MailUtil {
    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

    /**
     * 2018/12/29 11:45
     * 创建本地资源对象
     *
     * @param imageName         模板唯一标识符  <img src="cid:{2}" >
     * @param absoluteImageFile 本地资源
     * @return javax.mail.internet.MimeBodyPart
     * @author owen pan
     */
    public static MimeBodyPart createImageMimeBodyPart(String imageName, FileDataSource absoluteImageFile) throws MessagingException, UnsupportedEncodingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setFileName(MimeUtility.encodeText(absoluteImageFile.getName()));
        mbp.setDataHandler(new DataHandler(absoluteImageFile));
        //设置对应的资源文件的唯一标识符，即 MIME 协议对于邮件的结构组织格式中的 Content-ID 头字段；
        mbp.setHeader("Content-ID", imageName);
        return mbp;
    }

    /**
     * 2018/12/29 11:44
     * 创建网络资源对象
     *
     * @param urlName         模板唯一标识符  <img src="cid:{2}" >
     * @param absoluteLinkURL 网络资源
     * @return javax.mail.internet.MimeBodyPart
     * @author owen pan
     */
    public static MimeBodyPart createURLMimeBodyPart(String urlName, URLDataSource absoluteLinkURL) throws MessagingException, UnsupportedEncodingException {
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setFileName(urlName);
        mbp.setDataHandler(new DataHandler(absoluteLinkURL));
        mbp.setHeader("Content-ID", urlName);
        return mbp;
    }

    /**
     * 2018/12/29 11:14
     * 发送邮件
     *
     * @param sub        邮件标题
     * @param body       邮件内容，支持html方式编写
     * @param tos        发送数组
     * @param copys      抄送数组
     * @param mimeImages 本地文件资源
     * @param mimeURLs   远程文件资源
     * @author owen pan
     */
    private static void send(String sub, String body, String[] tos, String[] copys,
                             Map<String, FileDataSource> mimeImages, Map<String, URLDataSource> mimeURLs) {
        try {
            //配置文件对象
            Properties props = new Properties();
            //邮箱服务地址
            props.put("mail.smtp.host", "smtp.mxhichina.com");
            //是否进行验证
            props.put("mail.smtp.auth", "true");
            //创建一个会话
            Session session = Session.getInstance(props);
            //打开调试，会打印与邮箱服务器会话的内容
            session.setDebug(false);
            Message message = new MimeMessage(session);
            //如果发送人没有写对，会出现javamail 550 Invalid User
            //如果发送人写的和使用的账号不一致，则会出现 553 Mail from must equal authorized user
            InternetAddress from = new InternetAddress("panrh@huihangtech.com");
            from.setPersonal(MimeUtility.encodeText("项目管理电子流系统"));
            message.setFrom(from);
            message.setSubject(sub);

            //发送
            Address[] to = null;
            if (tos != null && tos.length > 0) {
                to = new Address[tos.length];
                int index = 0;
                for (String s : tos) {
                    to[index++] = new InternetAddress(s);
                }
            }
            message.setRecipients(Message.RecipientType.TO, to);

            //抄送
            Address[] ccs = null;
            if (copys != null && copys.length > 0) {
                ccs = new Address[copys.length];
                int index = 0;
                for (String s : copys) {
                    ccs[index++] = new InternetAddress(s);
                }

            }
            message.setRecipients(Message.RecipientType.CC, ccs);

            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            html.setDataHandler(new DataHandler(body, "text/html;charset=utf-8"));
            mainPart.addBodyPart(html);
            //本地文件
            if (mimeImages != null && mimeImages.size() > 0) {
                Set<String> mimeImageSet = mimeImages.keySet();
                for (String f : mimeImageSet) {
                    mainPart.addBodyPart(createImageMimeBodyPart(f, mimeImages.get(f)));
                }
            }
            //远程文件
            if (mimeURLs != null && mimeURLs.size() > 0) {
                Set<String> mimeURLSet = mimeURLs.keySet();
                for (String f : mimeURLSet) {
                    mainPart.addBodyPart(createURLMimeBodyPart(f, mimeURLs.get(f)));
                }
            }

            message.setContent(mainPart);
            message.saveChanges();
            message.setSentDate(new Date());
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.mxhichina.com", 25, "panrh@huihangtech.com", "Uv513146758");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            logger.info(Arrays.toString(tos) + ",发送完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 2019/1/2 9:31
     * 获取邮件模板文本
     * @param path 路径
     * @return java.lang.String
     * @author owen pan
     */
    public static String getEmailMouldStr(String path) {
        URL url = MailUtil.class.getResource(path);
        URLConnection conn = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            conn = url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            inputStream = conn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                stringBuilder.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            org.apache.tomcat.util.http.fileupload.IOUtils.closeQuietly(inputStream);
            org.apache.tomcat.util.http.fileupload.IOUtils.closeQuietly(inputStreamReader);
            org.apache.tomcat.util.http.fileupload.IOUtils.closeQuietly(bufferedReader);
        }
        return stringBuilder.toString();
    }

    /**
     * 2019/1/2 9:30
     * 通用无插件文本发送
     *
     * @param str   发送文本内容
     * @param sub   标题
     * @param tos   发送者
     * @param copys 抄送者
     * @author owen pan
     */
    public static void sendByCommon(String str, String sub, String[] tos, String[] copys) {
        sendByMould(str, sub, tos, copys, null);
    }

    /**
     * 2019/1/2 9:32
     *
     * @param emailModelStr 模板内容文本
     * @param sub 标题
     * @param tos 发送者
     * @param copys 抄送者
     * @param sources 模板参数
     * @author owen pan
     */
    public static void sendByMould(String emailModelStr, String sub, String[] tos, String[] copys, List<Source> sources) {
        String[] keys = new String[]{};
        Map<String, FileDataSource> locals = new HashMap<>();
        Map<String, URLDataSource> remotes = new HashMap<>();
        if (sources != null) {
            keys = new String[sources.size()];
            for (int i = 0; i < sources.size(); i++) {
                Source source = sources.get(i);
                if (source == null) {
                    continue;
                }
                if (source instanceof UrlSource) {
                    keys[i] = "remote" + i;
                    try {
                        remotes.put("remote" + i, new URLDataSource(new URL(UrlSource.class.cast(source).getUrl())));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else if (source instanceof LocalSource) {
                    keys[i] = "local" + i;
                    locals.put("local" + i, new FileDataSource(new File(LocalSource.class.cast(source).getPath())));
                } else if (source instanceof TextSource) {
                    keys[i] = TextSource.class.cast(source).getText();
                }else if(source instanceof LocalAttachmentSource){
                    locals.put("local" + i, new FileDataSource(new File(LocalAttachmentSource.class.cast(source).getPath())));
                }else if(source instanceof UrlAttachmentSource){
                    try {
                        remotes.put("remote" + i, new URLDataSource(new URL(UrlAttachmentSource.class.cast(source).getUrl())));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        String body = MessageFormat.format(emailModelStr, keys);
        send(sub, body, tos, copys, locals, remotes);
    }

    //资源接口
    public static interface Source {
    }

    //网络资源
    public static class UrlSource implements Source {
        private String url;

        public UrlSource(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    //本地资源
    public static class LocalSource implements Source {
        private String path;

        public LocalSource(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    //文本资源
    public static class TextSource implements Source {
        private String text;

        public TextSource(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    //本地附件
    public static class LocalAttachmentSource implements Source {
        private String path;

        public LocalAttachmentSource(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class UrlAttachmentSource implements Source {
        private String url;

        public UrlAttachmentSource(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static void main(String[] args) {
//        sendEmailOfWarns("项目名","核查数据接收超时告警","测试","https://www.baidu.com",new String[]{"1012388145@qq.com"},null);
//        sendByMould("hello<a href='https://www.baidu.com'>baidu</a>", "测试邮件", new String[]{"1012388145@qq.com"}, null, null);


//        List<MailUtil.Source> sources = new ArrayList<>();
////        sources.add(new MailUtil.TextSource("项目名"));
////        sources.add(new MailUtil.TextSource("测试"));
////        sources.add(new MailUtil.LocalSource(MailUtil.class.getResource("/static/images/a1.jpg").getPath()));
////        sources.add(new MailUtil.UrlSource("http://i68.tinypic.com/175rpl.jpg"));
////        sources.add(new MailUtil.TextSource("<a style=\"text-decoration:none;color:#ffffff;font-size:17px; " +
////                "background:#0e8800;padding:10px 60px;border-radius:3px;\" href='" + "http://www.baidu.com" + "' target=\"_blank\">查看详情</a>"));
////        sources.add(new MailUtil.LocalAttachmentSource(MailUtil.class.getResource("/static/images/a4.jpg").getPath()));
////        sources.add(new MailUtil.UrlAttachmentSource("http://i68.tinypic.com/pxiex.jpg"));
////        sendByMould(getEmailMouldStr("/mould/mail_content_url.html"), "hello",
////                new String[]{"1012388145@qq.com"}, null, sources);
    }
}