package com.jupiter.common.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jupiter.common.config.JupiterConfig;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Jupiter API客户端
 */
public class JupiterApiClient {
    private final OkHttpClient httpClient;
    private final JupiterConfig config;
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    public JupiterApiClient(JupiterConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * GET请求
     */
    public JSONObject get(String endpoint, Map<String, Object> params) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(config.getApiBaseUrl() + endpoint).newBuilder();
        
        if (params != null) {
            params.forEach((key, value) -> urlBuilder.addQueryParameter(key, String.valueOf(value)));
        }
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .get();
        
        addAuthHeader(requestBuilder);
        
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                System.err.println("请求失败: " + response.code());
                System.err.println("响应: " + responseBody);
                throw new IOException("请求失败: " + response.code());
            }
            
            return JSONUtil.parseObj(responseBody);
        }
    }
    
    /**
     * POST请求
     */
    public JSONObject post(String endpoint, Object requestBody) throws IOException {
        String jsonBody = JSONUtil.toJsonStr(requestBody);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        
        Request.Builder requestBuilder = new Request.Builder()
                .url(config.getApiBaseUrl() + endpoint)
                .post(body);
        
        addAuthHeader(requestBuilder);
        
        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            String responseBody = response.body().string();
            
            if (!response.isSuccessful()) {
                System.err.println("请求失败: " + response.code());
                System.err.println("响应: " + responseBody);
                throw new IOException("请求失败: " + response.code());
            }
            
            return JSONUtil.parseObj(responseBody);
        }
    }
    
    private void addAuthHeader(Request.Builder requestBuilder) {
        if (config.hasApiKey()) {
            requestBuilder.addHeader("x-api-key", config.getApiKey());
        }
    }
    
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
}