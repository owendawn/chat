package com.hh.gismiddleware.util;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 2019/6/13 13:54
 * 需要jar包 okhttp3 、 okio1
 *
 * @author owen pan
 */
public class OkHttpUtil {
    private static Logger log = LoggerFactory.getLogger(OkHttpUtil.class);

    public OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(8000, TimeUnit.MILLISECONDS)
            .readTimeout(8000, TimeUnit.MILLISECONDS)
            .build();

    public String sendBySync(Request request) {
        try {
            Response response = okHttpClient.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    public void sendByAsync(Request request, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(8000, TimeUnit.MILLISECONDS)
                .connectTimeout(8000, TimeUnit.MILLISECONDS)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error(e.getMessage(),e);
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
            }
        });
    }

    public static OkHttpUtil newInstance() {
        return new OkHttpUtil();
    }


    public static void main(String[] args) {
        String url = "http://172.21.26.3:7008/addlive";
        String result = "";
        //get
        result = OkHttpUtil.newInstance().sendBySync(new Request.Builder()
                .header("Content-Type","application/json;charset=UTF-8")
                .header("Accept","application/json;charset=UTF-8")
                .url(url).get().build());
        //post
        RequestBody requestBody = new FormBody.Builder().add("Command", "101").build();
        result = OkHttpUtil.newInstance().sendBySync(new Request.Builder().url(url).post(requestBody).build());
        //put
        result = OkHttpUtil.newInstance().sendBySync(new Request.Builder()
                .url(url).put(RequestBody.create(okhttp3.MediaType.get(MediaType.APPLICATION_JSON_UTF8_VALUE), "{}")).build());
        //patch
        result = OkHttpUtil.newInstance().sendBySync(new Request.Builder().url(url).patch(RequestBody.create(okhttp3.MediaType.get(MediaType.APPLICATION_JSON_UTF8_VALUE), "{}")).build());
        //delete
        result = OkHttpUtil.newInstance().sendBySync(new Request.Builder().url(url).delete(RequestBody.create(okhttp3.MediaType.get(MediaType.APPLICATION_JSON_UTF8_VALUE), "{}")).build());
        System.out.println(result);
    }
}
