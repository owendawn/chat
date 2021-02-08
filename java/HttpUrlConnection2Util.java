package com.hh.media.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 2018/11/26 13:54
 * httpUrlConnection 工具类
 * <p>
 *
 * @author owen pan
 */
public class HttpUrlConnection2Util {
    private  Logger log=LoggerFactory.getLogger(HttpUrlConnection2Util.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private HttpURLConnection conn;
    private LogLevel logLevel=LogLevel.INFO;
    private HttpComunication httpComunication;
    private TheMap<String, String> header = new TheMap<String, String>() {{
        put("Content-Type", "application/json;charset=utf-8");
        put("Connection", "Keep-Alive");
    }};
    private TheMap<String, Object> params = new TheMap<>();
    private int connectTimeout = 15 * 1000;
    private int readTimeout = 25 * 1000;


    public static HttpUrlConnection2Util newInstance() {
        return new HttpUrlConnection2Util();
    }

    private SSLSocketFactory getSSLSocketFactory() {
        // 创建SSLContext对象，并使用我们指定的信任管理器初始化
        TrustManager[] tm = {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        }};
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public HttpURLConnection getHttpURLConnection(String url) throws IOException {
        if (url.startsWith("https:")) {
            conn = (HttpsURLConnection) new URL(null, url, new sun.net.www.protocol.https.Handler()).openConnection();
            ((HttpsURLConnection) conn).setSSLSocketFactory(getSSLSocketFactory());
        } else {
            conn = (HttpURLConnection) new URL(null, url, new sun.net.www.protocol.http.Handler()).openConnection();
        }
        return conn;
    }
    public HttpUrlConnection2Util setLogLevel(LogLevel logLevel){
        this.logLevel=logLevel;
        return this;
    }
    public HttpUrlConnection2Util clearHeaderPairs() {
        this.header.clear();
        return this;
    }

    public HttpUrlConnection2Util setHeaderPair(String key, String value) {
        this.header.put(key, value);
        return this;
    }

    public HttpUrlConnection2Util setHeaders(TheMap<String, String> headers) {
        this.header = headers;
        return this;
    }

    public HttpUrlConnection2Util clearParamPairs() {
        this.params.clear();
        return this;
    }

    public HttpUrlConnection2Util setParamPair(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    public HttpUrlConnection2Util setParams(TheMap<String, Object> params) {
        this.params = params;
        return this;
    }

    public TheMap<String, Object> getParams() {
        return params;
    }

    public HttpUrlConnection2Util setConnectTimeout(int millis) {
        this.connectTimeout = millis;
        return this;
    }

    public HttpUrlConnection2Util setReadTimeout(int millis) {
        this.readTimeout = millis;
        return this;
    }

    public HttpComunication finish() {
        if (conn != null) {
            conn.disconnect();
        }
        return httpComunication;
    }

    public HttpComunication unfinish() {
        return httpComunication;
    }

    private HttpURLConnection initConnect(String url) throws IOException {
        conn = getHttpURLConnection(url);
        //HttpURLConnection默认也支持从服务端读取结果流，所以下面的setDoInput也可以省略
        conn.setDoInput(true);
        // Post请求必须设置允许输出 默认false
        conn.setDoOutput(true);
        //禁用网络缓存
        conn.setUseCaches(false);
        // 设置连接主机超时时间
        conn.setConnectTimeout(this.connectTimeout);
        //设置从主机读取数据超时
        conn.setReadTimeout(this.readTimeout);
        for (String k : header.keySet()) {
            conn.setRequestProperty(k, header.get(k));
        }
        //备案信息
        Iterator<String> requestHeaderIterator = conn.getRequestProperties().keySet().iterator();
        StringBuilder sbRequestHeader = new StringBuilder();
        while (requestHeaderIterator.hasNext()) {
            String requestHeaderKey = requestHeaderIterator.next();
            sbRequestHeader.append(requestHeaderKey).append(":").append(conn.getRequestProperty(requestHeaderKey)).append("\n");
        }
        httpComunication.setRequestHeader(sbRequestHeader.toString());
        return conn;
    }

    public HttpUrlConnection2Util get(String url) {
        httpComunication = new HttpComunication();
        httpComunication.setType(HttpMethod.GET);
        try {
            if (params != null && !params.isEmpty()) {
                if (!url.contains("?")) {
                    url += "?";
                }
                url = url + "&" + map2FormData(params);
            }
            conn = initConnect(url);
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod("GET");
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
            conn.connect();
            handleResponse(conn, httpComunication);
            return this;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            httpComunication.setCode(500);
            httpComunication.setMsg(e.getMessage());
        }
        return this;
    }

    public HttpUrlConnection2Util otherRequest(String url, HttpMethod method) {
        httpComunication = new HttpComunication();
        httpComunication.setType(HttpMethod.GET);
        try {
            if (params != null && !params.isEmpty()) {
                if (!url.contains("?")) {
                    url += "?";
                }
                url = url + "&" + map2FormData(params);
            }
            conn = initConnect(url);
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod(method.name());
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //conn.connect()方法不必显式调用，当调用conn.getInputStream()方法时内部也会自动调用connect方法
            conn.connect();
            handleResponse(conn, httpComunication);
            return this;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            httpComunication.setCode(500);
            httpComunication.setMsg(e.getMessage());
        }
        return this;
    }

    public HttpUrlConnection2Util postJson(String url) {
        httpComunication = new HttpComunication();
        httpComunication.setType(HttpMethod.POST);
        try {
            conn = initConnect(url);
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod("POST");
            //设置请求中的媒体类型信息。
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //获取conn的输出流
            OutputStream os = conn.getOutputStream();
            //将请求体写入到conn的输出流中
            if (params != null) {
                os.write(objectMapper.writeValueAsString(params).getBytes());
            }
            //记得调用输出流的flush方法
            os.flush();
            //关闭输出流
            os.close();

            handleResponse(conn, httpComunication);
            return this;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            httpComunication.setCode(500);
            httpComunication.setMsg(e.getMessage());
        }
        return this;
    }

    public HttpUrlConnection2Util postForm(String url) {
        httpComunication = new HttpComunication();
        httpComunication.setType(HttpMethod.POST);
        try {
            conn = initConnect(url);
            //HttpURLConnection默认就是用GET发送请求，所以下面的setRequestMethod可以省略
            conn.setRequestMethod("POST");
            //设置请求中的媒体类型信息。
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            //在对各种参数配置完成后，通过调用connect方法建立TCP连接，但是并未真正获取数据
            //获取conn的输出流
            OutputStream os = conn.getOutputStream();
            //将请求体写入到conn的输出流中
            if (params != null) {
                os.write(map2FormData(params).getBytes());
            }
            //记得调用输出流的flush方法
            os.flush();
            //关闭输出流
            os.close();

            handleResponse(conn, httpComunication);
            return this;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            httpComunication.setCode(500);
            httpComunication.setMsg(e.getMessage());
        }
        return this;
    }

    //map to form data
    private static String map2FormData(Map<String, Object> params) throws UnsupportedEncodingException {
        if (params == null) {
            return "";
        }
        StringBuilder tempParams = new StringBuilder();
        int pos = 0;
        for (String key : params.keySet()) {
            if (pos > 0) {
                tempParams.append("&");
            }
            tempParams.append(String.format("%s=%s", key, params.get(key) == null ? "" : URLEncoder.encode((String) params.get(key), "utf-8")));
            pos++;
        }
        return tempParams.toString();
    }

    //处理响应体
    private void handleResponse(HttpURLConnection conn, HttpComunication httpComunication) {
        try {
            //调用getInputStream方法后，服务端才会收到请求，并阻塞式地接收服务端返回的数据
            InputStream is = conn.getInputStream();
            //将InputStream转换成byte数组,getBytesByInputStream会关闭输入流
            byte[] responseBody = getBytesByInputStream(is);
            httpComunication.setResponseBody(responseBody);
            //获取响应头
            Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
            int size = responseHeaderMap.size();
            StringBuilder sbResponseHeader = new StringBuilder();
            for (int i = 0; i < size; i++) {
                String responseHeaderKey = conn.getHeaderFieldKey(i);
                sbResponseHeader.append(responseHeaderKey).append(":").append(conn.getHeaderField(i)).append("\n");
            }
            httpComunication.setResponseHeader(sbResponseHeader.toString());
            httpComunication.setCode(conn.getResponseCode());
            // 判断请求是否成功
            String str = httpComunication.getResponseBodyStr();
            if (httpComunication.getResponseBodyStr() != null && httpComunication.getResponseBodyStr().length() > 100) {
                str = str.substring(0, 100) + "...";
            }
            if(logLevel.equal(LogLevel.DEBUG)) {
                log.debug("response:" + str);
            }
        } catch (IOException e) {
            try {
                httpComunication.setCode(conn.getResponseCode());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            httpComunication.setMsg(e.getMessage());
            log.warn("请求失败("+httpComunication.getType().name()+")["+httpComunication.getCode()+"]:" + e.getMessage());
        }catch (Exception e){
            httpComunication.setCode(500);
            httpComunication.setMsg(e.getMessage());
            log.warn("请求异常("+httpComunication.getType().name()+")["+httpComunication.getCode()+"]:" + e.getMessage(), e);
        }
    }

    /**
     * 从InputStream中读取数据，转换成byte数组，最后关闭InputStream
     *
     * @param is 输入流
     * @return byte[]
     */
    private static byte[] getBytesByInputStream(InputStream is) {
        byte[] bytes = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        byte[] buffer = new byte[1024 * 8];
        int length;
        try {
            while ((length = bis.read(buffer)) > 0) {
                bos.write(buffer, 0, length);
            }
            bos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    public static class HttpComunication {


        //请求头
        private String requestHeader;
        //请求体
        private byte[] requestBody;
        //响应头
        private String responseHeader;
        //响应体
        private byte[] responseBody;
        private HttpMethod type;
        private String msg;
        private Integer code = 200;

        public HttpComunication() {
        }

        public String getRequestHeader() {
            return requestHeader;
        }

        public void setRequestHeader(String requestHeader) {
            this.requestHeader = requestHeader;
        }

        public byte[] getRequestBody() {
            return requestBody;
        }

        public void setRequestBody(byte[] requestBody) {
            this.requestBody = requestBody;
        }

        public String getResponseHeader() {
            return responseHeader;
        }

        public void setResponseHeader(String responseHeader) {
            this.responseHeader = responseHeader;
        }

        public byte[] getResponseBody() {
            return responseBody;
        }

        public String getResponseBodyStr() {
            try {
                return new String(responseBody, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        public <T> T getResponseEntity(Class<T> cls) {
            try {
                return objectMapper.readValue(getResponseBodyStr(), cls);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void setResponseBody(byte[] responseBody) {
            this.responseBody = responseBody;
        }

        public HttpMethod getType() {
            return type;
        }

        public void setType(HttpMethod type) {
            this.type = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "HttpComunication{" +
                    "requestHeader='" + requestHeader.replaceAll("\n", "") + '\'' +
                    ", code=" + code +
                    ", msg=" + msg +
                    ", type=" + type +
                    ", requestBody=" + (requestBody == null || "".equals(requestBody) ? "null" : "not null") +
//                    ", requestBodyStr=" + (requestBody == null ? "null" : new String(requestBody)) +
                    ", responseHeader='" + responseHeader.replaceAll("\n", "") + '\'' +
                    ", responseBody=" + (responseBody == null ? "null" : "not null") +
//                    ", responseBodyStr=" + (responseBody == null ? "" : new String(responseBody)) +
                    '}';
        }
    }

    public static class TheMap<K, V> extends HashMap<K, V> {
        public TheMap<K, V> putPair(K k, V v) {
            this.put(k, v);
            return this;
        }
    }

    public static enum HttpMethod {GET, POST,PUT,DELETE}
    public static enum LogLevel{
        ERROR,INFO,DEBUG;
        public boolean equal(LogLevel logLevel){
           return this.name().equals(logLevel.name());
        }
    }

    public static void main(String[] args) {
        HttpComunication httpComunication = HttpUrlConnection2Util.newInstance().get("https://www.baidu.com").finish();
        System.out.println(httpComunication.getResponseBodyStr().length());
    }
}
