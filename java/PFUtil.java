package com.hh.jinhua.service.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 2019/11/13 15:56
 *
 * @author owen pan
 */
public class PFUtil {
    /**
     * 日志输出器
     */
    private static Logger log = LoggerFactory.getLogger(PFUtil.class);

    /**
     * 获取默认编码
     *
     * @return 默认编码
     */
    public static Charset getDefaultCharset() {
        return Charset.forName("UTF-8");
    }

    /**
     * 数字金额转大写
     */
    public static String digitUppercase(double v) {
        final String UNIT = "万千佰拾亿千佰拾万千佰拾元角分";
        final String DIGIT = "零壹贰叁肆伍陆柒捌玖";
        final double MAX_VALUE = 9999999999999.99D;

        if (v < 0 || v > MAX_VALUE) {
            return "参数非法!";
        }
        long l = Math.round(v * 100);
        if (l == 0) {
            return "零元整";
        }
        String strValue = l + "";
        // i用来控制数
        int i = 0;
        // j用来控制单位
        int j = UNIT.length() - strValue.length();
        String rs = "";
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);
            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' || UNIT.charAt(j) == '元') {
                    rs = rs + UNIT.charAt(j);
                    isZero = false;
                }
            } else {
                if (isZero) {
                    rs = rs + "零";
                    isZero = false;
                }
                rs = rs + DIGIT.charAt(ch - '0') + UNIT.charAt(j);
            }
        }
        if (!rs.endsWith("分")) {
            rs = rs + "整";
        }
        rs = rs.replaceAll("亿万", "亿");
        return rs;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @param defExt   缺省扩展名
     * @return 扩展名
     */
    public static String getExtension(String filename, String defExt) {
        if ((filename != null) && (filename.length() > 0)) {
            int i = filename.lastIndexOf('.');
            if ((i > -1) && (i < (filename.length() - 1))) {
                return filename.substring(i + 1);
            }
        }
        return defExt;
    }

    /**
     * 获取系统唯一编号,采用UUID编码规则
     *
     * @return String
     */
    public static String getRandomUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }


    /**
     * 获取当前time-zone
     *
     * @return 日期
     */
    public static ZoneId getZoneId() {
        return ZoneId.of("UTC+08:00");
    }

    /**
     * 获取系统当前日期毫秒值
     *
     * @return 时间戳
     */
    public static long getDateTimeEpochMilli() {
        return getDateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    /**
     * 获取系统当前日期时间戳
     *
     * @param time 时间
     * @return 时间戳
     */
    public static long getDateTimeEpochMilli(LocalDateTime time) {
        if (null == time) {
            return 0L;
        }
        return time.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
    }

    /**
     * 获取系统当前日期
     *
     * @return 日期
     */
    public static LocalDateTime getDateTime() {
        return LocalDateTime.now(getZoneId());
    }

    /**
     * 把字符串时间,转换成日期
     *
     * @param timestamp 时间戳
     * @return 日期
     */
    public static LocalDateTime getDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(getZoneId()).toLocalDateTime();
    }

    /**
     * 把字符串时间,转换成日期
     *
     * @param timestamp 时间戳
     * @return 日期
     */
    public static LocalDateTime getDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(getZoneId()).toLocalDateTime();
    }

    /**
     * 把字符串时间,转换成时间
     *
     * @param val 日期字符串.格式为:yyyy-MM-dd HH:mm:ss
     * @return 日期
     */
    public static LocalDateTime getDateTime(String val) {
        LocalDateTime localDateTime = null;
        if (StringUtils.isBlank(val)) {
            return null;
        }
        try {
            val = val.trim();
            String pattern = "yyyy-MM-dd HH:mm:ss";
            val = val.replace("/", "-");
            // 处理 2019-06-27 10:06:46.0
            val = val.replace(".0", "");
            if ("yyyy-MM-dd".length() == val.length()) {
                val += " 00:00:00";
            } else if ("yyyy-MM-dd HH:mm".length() == val.length()) {
                val += ":00";
            } else if ("yyyy-MM-dd HH:mm:ss.SSS".length() == val.length()) {
                pattern = "yyyy-MM-dd HH:mm:ss.SSS";
            } else if ("yyyy-MM-dd HH:mm:ss.S".length() == val.length()) {
                pattern = "yyyy-MM-dd HH:mm:ss.S";
            }
            localDateTime = LocalDateTime.parse(val, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.error("把字符串时间[ " + val + " ], 转换成时间发生异常:" + e.getMessage());
        }
        return localDateTime;
    }

    /**
     * 把字符串时间,转换成时间
     *
     * @param pattern 日期格式
     * @param val     日期字符串.格式为:yyyy-MM-dd HH:mm:ss
     * @return 日期
     */
    public static LocalDateTime getDateTime(String pattern, String val) {
        if (StringUtils.isBlank(val) || StringUtils.isBlank(pattern)) {
            return null;
        }
        LocalDateTime localDateTime = null;
        try {
            localDateTime = LocalDateTime.parse(val, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.error("把字符串时间[ " + val + " ], 转换成时间发生异常:" + e.getMessage());
        }
        return localDateTime;
    }

    /**
     * 格式化指定日期 默认格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 格式化后的字符串
     */
    public static String getFormatDateTime() {
        return getFormatDateTime(getDateTime());
    }

    /**
     * 格式化指定日期 默认格式 yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime 传入的日期
     * @return 格式化后的字符串
     */
    public static String getFormatDateTime(LocalDateTime dateTime) {
        return getFormatDateTime(null, dateTime);
    }

    /**
     * 格式化指定日期
     *
     * @param pattern  传入日期格式"yyyy-MM-dd HH:mm:ss"
     * @param dateTime 传入的日期
     * @return 格式化后的字符串
     */
    public static String getFormatDateTime(String pattern, LocalDateTime dateTime) {
        if (null == dateTime) {
            return "";
        }

        if (StringUtils.isBlank(pattern)) {
            // 默认显示的时间格式
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化指定日期 yyyy-MM-dd
     *
     * @return 格式化后的字符串
     */
    public static String getFormatDate() {
        return getFormatDate("yyyy-MM-dd", getDateTime());
    }

    /**
     * 格式化指定日期 yyyy-MM-dd
     *
     * @param dateTime 传入的日期
     * @return 格式化后的字符串
     */
    public static String getFormatDate(Temporal dateTime) {
        return getFormatDate("yyyy-MM-dd", dateTime);
    }

    /**
     * 格式化指定日期
     *
     * @param pattern  传入日期格式"yyyy-MM-dd"
     * @param dateTime 传入的日期
     * @return 格式化后的字符串
     */
    public static String getFormatDate(String pattern, Temporal dateTime) {
        if (null == dateTime) {
            return "";
        }
        if (StringUtils.isBlank(pattern)) {
            pattern = "yyyy-MM-dd";
        }
        if(dateTime instanceof LocalDateTime) {
            return LocalDateTime.class.cast( dateTime).format(DateTimeFormatter.ofPattern(pattern));
        }else if(dateTime instanceof LocalDate){
            return LocalDate.class.cast(dateTime).format(DateTimeFormatter.ofPattern(pattern));
        }else {
            throw new RuntimeException("not support format the bean of "+dateTime.getClass());
        }
    }


    /**
     * 发送信息(UDP),并接收一个返回包
     *
     * @param msg     数据
     * @param ip      IP
     * @param port    端口
     * @param timeout 超时 单位毫秒
     * @return 返回值
     */
    public static String sendByUDP(String msg, String ip, int port, int timeout) {
        String recVal;
        try {
            if (timeout <= 0) {
                timeout = 1000;
            }
            log.debug("发送【" + ip + ":" + port + "】：" + msg);

            DatagramSocket client = new DatagramSocket();
            byte[] send = msg.getBytes(getDefaultCharset());
            DatagramPacket pack = new DatagramPacket(send, send.length, InetAddress.getByName(ip), port);

            byte[] recs = new byte[102400];
            DatagramPacket recPacket = new DatagramPacket(recs, recs.length);
            client.setSoTimeout(timeout);
            client.send(pack);
            client.receive(recPacket);
            recVal = new String(recs, 0, recPacket.getLength());
            client.close();
            log.debug("接收【" + ip + ":" + port + "】：" + recVal);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "操作失败！" + e.getMessage();
        }
        return recVal;
    }

    /**
     * 发送信息(TCP),并接收一个返回包
     *
     * @param msg     数据
     * @param ip      IP
     * @param port    端口
     * @param timeout 超时 单位毫秒
     * @return 返回信息
     */
    public static String sendByTCP(String msg, String ip, int port, int timeout) {
        String recVal = "";
        try {
            if (timeout <= 0) {
                timeout = 1000;
            }
            log.debug("TCP【" + ip + ":" + port + "】：" + msg);

            Socket client = new Socket(ip, port);
            client.setSoTimeout(timeout);

            InputStream is = client.getInputStream();
            OutputStream os = client.getOutputStream();

            os.write(msg.getBytes(getDefaultCharset()));
            os.flush();

            byte[] recs = new byte[1024000];

            int len = is.read(recs);
            recVal = new String(recs, 0, len, getDefaultCharset());

            os.close();
            is.close();
            client.close();
            log.debug("接收【" + ip + ":" + port + "】：" + recVal);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "操作失败！" + e.getMessage();
        }
        return recVal;
    }

    /**
     * 发送信息(TCP), 不接收返回包
     *
     * @param msg     数据
     * @param ip      IP
     * @param port    端口
     * @param timeout 超时 单位毫秒
     * @return null 发送成功，否则返回异常信息
     */
    public static void sendByTCPNoReceive(String msg, String ip, int port, int timeout) {
        try {
            if (timeout <= 0) {
                timeout = 1000;
            }
            log.debug("发送【" + ip + ":" + port + "】：" + msg);

            Socket client = new Socket(ip, port);
            client.setSoTimeout(timeout);

            OutputStream os = client.getOutputStream();

            os.write(msg.getBytes(getDefaultCharset()));
            os.flush();

            os.close();
            client.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 拷贝文件
     *
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 1024];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }

            // 刷新此缓冲的输出流
            outBuff.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            // 关闭流
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
        }
    }

    /**
     * 获取文件内容
     *
     * @param filePath 文件路径
     * @return
     */
    public static String readFile(String filePath) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            String val = null;
            while ((val = reader.readLine()) != null) {
                stringBuffer.append(val);
            }
            reader.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return stringBuffer.toString();
    }

    /**
     * 保存文件内容
     *
     * @param filePath 文件路径
     * @param content  文件内容
     */
    public static void saveFile(String filePath, String content) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 保存文件内容
     *
     * @param filePath 文件路径
     * @param content  文件内容
     */
    public static void saveFile(String filePath, byte[] content) {
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            outputStream.write(content);
            outputStream.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 拷贝文件
     *
     * @param oldPath 源路径
     * @param newPath 目标路径
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                // 文件存在时
                InputStream inStream = new FileInputStream(oldPath);
                // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024 * 100];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                fs.close();
                inStream.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 拷贝文件
     *
     * @param url      文件地址
     * @param filePath 保存路径
     */
    public static boolean downloadFile(String url, String filePath) {
        try {
            URL fileUrl = new URL(url);
            URLConnection conn = fileUrl.openConnection();
            conn.setConnectTimeout(6 * 1000);
            conn.setReadTimeout(60 * 1000);

            File newFile = new File(filePath);
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }

            try (InputStream inputStream = conn.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath)) {

                byte[] buffer = new byte[1204];

                int readByte = 0;
                while ((readByte = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readByte);
                }
                outputStream.flush();
            } catch (Exception e) {
                log.error("url = [" + url + "], filePath = [" + filePath + "]");
                log.error(e.getMessage(), e);
                return false;
            }
        } catch (Exception e) {
            log.error("url = [" + url + "], filePath = [" + filePath + "]");
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 获取URL内容
     *
     * @param uri     网址
     * @param timeout 超时
     * @return 网页内容
     */
    public static String getURLContentByHttpURLConnection(String uri, int timeout) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("content-type", "text/html");
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String str = null;
            while ((str = br.readLine()) != null) {
                content.append(str);
            }
            br.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * 格式化手机号码
     *
     * @param mobile 11位手机号码
     * @return 前三后四中间补* （188****7896）
     */
    public static String getFormatMobile(String mobile) {
        if (StringUtils.isBlank(mobile) || mobile.length() < 11) {
            return mobile;
        }
        return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 格式化SQL参数中特殊字符
     *
     * @param sql
     * @return
     */
    public static String escapeSql(String sql) {
        if (StringUtils.isBlank(sql)) {
            return sql;
        }
        sql = sql.replace("_", "\\_")
                .replace("%", "\\%");
        return sql;
    }

    /**
     * double转成字符串
     *
     * @param value   double
     * @param pattern 格式 #.## （1.234 -> 1.23 | 1.1 -> 1.1） #。00 （1.234 -> 1.234 | 1.1 -> 1.10）
     * @return 格式化后的字符串
     */
    public static String toNumberString(double value, String pattern) {
        if (StringUtils.isBlank(pattern)) {
            pattern = "#.##";
        }
        DecimalFormat df = new DecimalFormat(pattern);

        return df.format(value);
    }


    /**
     * 通过反射获取Bean中所有属性和属性值
     *
     * @param o Bean
     * @return 属性和属性值
     */
    public static Map<String, String> getBeanMap(Object o) {
        Map<String, String> fieldMap = new HashMap<>();

        try {
            Field[] fields = o.getClass().getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                Object obj = field.get(o);

                if (obj instanceof String || obj instanceof Integer || obj instanceof Double) {
                    fieldMap.put(field.getName(), field.get(o).toString());
                    continue;
                }
            }
            if (null != o.getClass().getGenericSuperclass()) {
                // 如果有父类
                fields = o.getClass().getSuperclass().getDeclaredFields();
                Field.setAccessible(fields, true);
                for (Field field : fields) {
                    Object obj = field.get(o);

                    if (obj instanceof String || obj instanceof Integer || obj instanceof Double) {
                        fieldMap.put(field.getName(), field.get(o).toString());
                        continue;
                    }
                }
            }
        } catch (Exception e) {
        }

        return fieldMap;
    }

    /**
     * 将list中，根据对象某一属性，将该属性设为key的map
     * @param list 对象集合
     * @param key 关键字
     * @param <T> 对象类型
     * @return HashMap<String, T>
     */
    public static <T> HashMap<String, T> listToMapByKey(List<T> list, String key) {
        if (list == null) {
            return null;
        }
        HashMap<String, T> map = new HashMap<>();
        if (list.size() > 0) {
            if (list.get(0) instanceof Map) {
                for (T o : list) {
                    Map it = (Map) o;
                    Object k = map.get(key);
                    if (k == null) {
                        throw new RuntimeException("key not support to be null");
                    }
                    map.put(it.get(key).toString(), o);
                }
            } else {
                try {
                    list.get(0).getClass().getMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
                    for (T o : list) {
                        Method method = o.getClass().getMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
                        Object k = method.invoke(o);
                        map.put(k.toString(),o);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

                }
            }
        }
        return map;
    }

    /**
     * 前面补占位符到固定长度
     * @param raw
     * @param replace
     * @param length
     * @return
     */
    public static String fillToPrefix(Object raw,char replace,int length){
        String str=raw.toString();
        while (str.length()<length){
            str=replace+str;
        }
        return str;
    }

    public static PanMap<String,Object> newPanMap(){
        return new PanMap<String,Object>();
    }

    public static class PanMap<K,V> extends HashMap<K,V>{

        public PanMap<K,V> setKeyValuePair(K key, V value) {
            super.put(key, value);
            return this;
        }
    }

    public static class PanKeyValuePair{
        private String key;
        private Object value;

        public PanKeyValuePair(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "PanKeyValuePair{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
