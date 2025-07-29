package com.jupiter.recurring;

import cn.hutool.json.JSONObject;
import com.jupiter.common.client.JupiterApiClient;
import com.jupiter.common.config.JupiterConfig;
import com.jupiter.common.wallet.SolanaWallet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 取消定期交易订单示例
 * 演示如何取消已创建的定期交易订单
 */
public class CancelOrder {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("用法: java CancelOrder <orderId>");
            System.err.println("请提供要取消的订单ID");
            System.exit(1);
        }
        
        String orderId = args[0];
        
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始取消定期交易订单: " + orderId);
            
            // 取消订单
            cancelRecurringOrder(apiClient, wallet, orderId);
            
            // 清理资源
            apiClient.close();
            
        } catch (Exception e) {
            System.err.println("取消订单失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 取消定期交易订单
     */
    private static void cancelRecurringOrder(JupiterApiClient apiClient, SolanaWallet wallet, String orderId) throws IOException {
        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("orderId", orderId);
        cancelRequest.put("user", wallet.getPublicKey().toString());
        
        JSONObject cancelResponse = apiClient.post("/recurring/v1/cancelOrder", cancelRequest);
        
        String status = cancelResponse.getStr("status");
        
        if ("Success".equals(status)) {
            System.out.println("订单取消成功！");
            System.out.println("订单ID: " + orderId);
        } else {
            String errorMessage = cancelResponse.getStr("error");
            System.err.println("订单取消失败: " + errorMessage);
        }
    }
    
    /**
     * 查询用户的所有定期交易订单
     */
    public static void listUserOrders(JupiterApiClient apiClient, SolanaWallet wallet) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("user", wallet.getPublicKey().toString());
        
        JSONObject ordersResponse = apiClient.get("/recurring/v1/orders", params);
        
        System.out.println("用户订单列表:");
        System.out.println(ordersResponse.toStringPretty());
    }
}