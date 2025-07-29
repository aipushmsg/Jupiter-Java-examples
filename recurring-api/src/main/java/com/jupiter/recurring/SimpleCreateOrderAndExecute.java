package com.jupiter.recurring;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONObject;
import com.jupiter.common.client.JupiterApiClient;
import com.jupiter.common.config.JupiterConfig;
import com.jupiter.common.constants.TokenConstants;
import com.jupiter.common.wallet.SolanaWallet;
import org.p2p.solanaj.core.Transaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 定期交易API示例
 * 演示如何创建定期交易订单并执行
 */
public class SimpleCreateOrderAndExecute {
    
    public static void main(String[] args) {
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始创建定期交易订单...");
            
            // 创建定期交易订单
            JSONObject orderData = createRecurringOrder(apiClient, wallet);
            System.out.println("创建订单响应: " + orderData);
            
            // 签署交易
            String signedTransactionBase64 = signTransaction(wallet, orderData.getStr("transaction"));
            
            // 执行订单
            executeOrder(apiClient, orderData.getStr("requestId"), signedTransactionBase64);
            
            // 清理资源
            apiClient.close();
            
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建定期交易订单
     */
    private static JSONObject createRecurringOrder(JupiterApiClient apiClient, SolanaWallet wallet) throws IOException {
        // 构建时间参数
        Map<String, Object> timeParams = new HashMap<>();
        timeParams.put("inAmount", 100_000_000L); // 0.1 WSOL
        timeParams.put("interval", 3600); // 每小时
        timeParams.put("numberOfOrders", 3); // 3次订单
        
        Map<String, Object> params = new HashMap<>();
        params.put("time", timeParams);
        
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("inputMint", TokenConstants.WSOL);  // WSOL
        orderRequest.put("outputMint", TokenConstants.USDC); // USDC
        orderRequest.put("params", params);
        orderRequest.put("user", wallet.getPublicKey().toString());
        
        return apiClient.post("/recurring/v1/createOrder", orderRequest);
    }
    
    /**
     * 签署交易
     */
    private static String signTransaction(SolanaWallet wallet, String transactionBase64) {
        try {
            return wallet.signTransactionToBase64(transactionBase64);
        } catch (Exception e) {
            throw new RuntimeException("签署交易失败", e);
        }
    }
    
    /**
     * 执行订单
     */
    private static void executeOrder(JupiterApiClient apiClient, String requestId, String signedTransaction) throws IOException {
        Map<String, Object> executeRequest = new HashMap<>();
        executeRequest.put("signedTransaction", signedTransaction);
        executeRequest.put("requestId", requestId);
        
        JSONObject executeResponse = apiClient.post("/recurring/v1/execute", executeRequest);
        
        String signature = executeResponse.getStr("signature");
        String status = executeResponse.getStr("status");
        
        if ("Success".equals(status)) {
            System.out.println("交易发送成功！签名: " + signature);
            System.out.println("在Solscan上查看交易: https://solscan.io/tx/" + signature);
        } else {
            String errorCode = executeResponse.getStr("code");
            String errorMessage = executeResponse.getStr("error");
            
            System.err.println("交易失败！签名: " + signature);
            System.err.println("自定义程序错误代码: " + errorCode);
            System.err.println("消息: " + errorMessage);
            System.err.println("在Solscan上查看交易: https://solscan.io/tx/" + signature);
        }
    }
}