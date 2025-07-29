package com.jupiter.ultra;

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
 * Ultra API订单创建和执行示例
 * 演示如何使用Ultra API创建订单并执行交易
 */
public class OrderAndExecute {
    
    public static void main(String[] args) {
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始创建Ultra订单...");
            
            // 创建订单
            JSONObject orderData = createOrder(apiClient, wallet);
            System.out.println("订单响应: " + orderData);
            
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
     * 创建Ultra订单
     */
    private static JSONObject createOrder(JupiterApiClient apiClient, SolanaWallet wallet) throws IOException {
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("inputMint", TokenConstants.WSOL);  // WSOL
        orderParams.put("outputMint", TokenConstants.USDC); // USDC
        orderParams.put("amount", 10_000_000L); // 0.01 WSOL
        orderParams.put("taker", wallet.getPublicKey().toString()); // 钱包公钥
        
        return apiClient.get("/ultra/v1/order", orderParams);
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
        
        JSONObject executeResponse = apiClient.post("/ultra/v1/execute", executeRequest);
        
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