package cn.hs.police.util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class HttpClientUtil {
    private static Logger log = LoggerFactory.getLogger(HttpClientUtil2.class);

    public static String postJson(String url, String jsonStr) {
        log("发送URL：" + url);
        log("发送json内容：" + jsonStr);
        String result = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            StringEntity strEntity = new StringEntity(URLEncoder.encode(jsonStr, "UTF-8"));
            strEntity.setContentEncoding("UTF-8");
            strEntity.setContentType("application/json");
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(strEntity);
            CloseableHttpResponse response = httpClient.execute(httppost);
            handleResponse(response);
        } catch (Exception e) {
            log.warn("发送Post json请求失败：" + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String postKeyValue(String url, List<NameValuePair> params) {
        log("发送URL：" + url);
        log("发送KeyValue内容：" + params);
        String result = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(60000)
                    .setConnectionRequestTimeout(60000)
                    .setSocketTimeout(60000)
                    .setRedirectsEnabled(true)
                    .build();
            httppost.setConfig(requestConfig);
            httppost.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8"));
            httppost.setHeader(new BasicHeader("Accept", "text/json;charset=utf-8"));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            CloseableHttpResponse response = httpClient.execute(httppost);
            result = handleResponse(response);
        } catch (Exception e) {
            log.error("发送数据失败：" + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String sendByGet(String url) {
        String result = null;
        CloseableHttpClient httpCilent2 = HttpClients.createDefault();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .setSocketTimeout(30000)
                .setRedirectsEnabled(true)
                .build();
        HttpGet httpGet2 = new HttpGet(url);
        httpGet2.setConfig(requestConfig);
        try {
            CloseableHttpResponse httpResponse = httpCilent2.execute(httpGet2);
            result = handleResponse(httpResponse);
        } catch (IOException e) {
            log.error("发送数据失败：" + e.getMessage());
            return null;
        } finally {
            try {
                httpCilent2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String handleResponse(CloseableHttpResponse response) throws IOException {
        String result = null;
        try {
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    if(response.getLastHeader("Content-Type").getValue().toLowerCase().contains("utf-8")) {
                        result = EntityUtils.toString(entity,"UTF-8");
                    }else if(response.getLastHeader("Content-Type").getValue().toLowerCase().contains("gbk")){
                        result = EntityUtils.toString(entity,"GBK");
                    }else if(response.getLastHeader("Content-Type").getValue().toLowerCase().contains("gb2312")){
                        result = EntityUtils.toString(entity,"GB2312");
                    }else {
                        result = EntityUtils.toString(entity,"UTF-8");
                    }
                    log("返回数据：" + result);
                }
                EntityUtils.consume(entity);
            } else {
                log.warn("请求返回异常码:" + response.getStatusLine().getStatusCode() + " : " + response.getStatusLine().getReasonPhrase());
            }
        } finally {
            if (response != null ) {
                response.close();
            }
        }
        return result;
    }

    private static void log(String str) {
        if (str.length() > 200) {
            log.info(str.substring(0, 200) + " ...");
        } else {
            log.info(str);
        }
    }
}
