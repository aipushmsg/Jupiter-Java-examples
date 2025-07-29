package com.jupiter.common.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * Jupiter配置管理类
 */
public class JupiterConfig {
    private static final Props props = new Props(".env");
    
    private final String privateKey;
    private final String rpcUrl;
    private final String apiKey;
    private final String apiBaseUrl;
    
    public JupiterConfig() {
        this.privateKey = props.getStr("PRIVATE_KEY");
        this.rpcUrl = props.getStr("RPC_URL");
        this.apiKey = props.getStr("API_KEY");
        
        // 免费用户使用lite-api.jup.ag，付费用户使用api.jup.ag
        this.apiBaseUrl = StrUtil.isNotBlank(apiKey) ? "https://api.jup.ag" : "https://lite-api.jup.ag";
        
        validateConfig();
    }
    
    private void validateConfig() {
        if (StrUtil.isBlank(privateKey)) {
            throw new IllegalStateException("错误：必须在.env文件中设置PRIVATE_KEY");
        }
        if (StrUtil.isBlank(rpcUrl)) {
            throw new IllegalStateException("错误：必须在.env文件中设置RPC_URL");
        }
    }
    
    public String getPrivateKey() {
        return privateKey;
    }
    
    public String getRpcUrl() {
        return rpcUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    public boolean hasApiKey() {
        return StrUtil.isNotBlank(apiKey);
    }
}