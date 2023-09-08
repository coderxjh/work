package com.jcxx.saas;


import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XnzxTest {

    @Test
    public void test() {
        String url = "http://192.168.1.116:8099/xnzx/sys/dept/save";
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        for (int i = 10; i <= 15; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("deptType", "7");
            map.put("name", "初22" + i + "班");
            map.put("parentId", "198");
            map.put("sort", 0);
            map.put("type", 0);
            String jsonString = JSON.toJSONString(map);
            RequestBody requestBody = RequestBody.create(mediaType, jsonString);
            Request request = new Request.Builder()
                    .addHeader("token","881d671e3b0d8c97e6b62be3a2241507")
                    .url(url)
                    .post(requestBody)    //默认就是GET请求，可以省略
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response execute = call.execute();
                System.out.println(execute.body().string());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}