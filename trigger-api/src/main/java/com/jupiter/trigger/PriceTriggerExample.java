package com.jupiter.trigger;

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
 * 价格触发交易示例
 * 演示如何创建基于价格触发的交易订单
 */
public class PriceTriggerExample {
    
    public static void main(String[] args) {
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始创建价格触发订单...");
            
            // 创建价格触发订单
            JSONObject orderData = createPriceTriggerOrder(apiClient, wallet);
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
     * 创建价格触发订单
     */
    private static JSONObject createPriceTriggerOrder(JupiterApiClient apiClient, SolanaWallet wallet) throws IOException {
        // 构建价格触发参数
        Map<String, Object> priceParams = new HashMap<>();
        priceParams.put("inAmount", 50_000_000L); // 0.05 WSOL
        priceParams.put("triggerPrice", 100.0); // 当WSOL/USDC价格达到100时触发
        priceParams.put("orderType", "buy"); // 买入订单
        
        Map<String, Object> params = new HashMap<>();
        params.put("price", priceParams);
        
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("inputMint", TokenConstants.WSOL);  // WSOL
        orderRequest.put("outputMint", TokenConstants.USDC); // USDC
        orderRequest.put("params", params);
        orderRequest.put("user", wallet.getPublicKey().toString());
        
        return apiClient.post("/trigger/v1/createOrder", orderRequest);
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
        
        JSONObject executeResponse = apiClient.post("/trigger/v1/execute", executeRequest);
        
        String signature = executeResponse.getStr("signature");
        String status = executeResponse.getStr("status");
        
        if ("Success".equals(status)) {
            System.out.println("触发订单创建成功！签名: " + signature);
            System.out.println("在Solscan上查看交易: https://solscan.io/tx/" + signature);
            System.out.println("订单将在价格条件满足时自动执行");
        } else {
            String errorCode = executeResponse.getStr("code");
            String errorMessage = executeResponse.getStr("error");
            
            System.err.println("订单创建失败！签名: " + signature);
            System.err.println("自定义程序错误代码: " + errorCode);
            System.err.println("消息: " + errorMessage);
            System.err.println("在Solscan上查看交易: https://solscan.io/tx/" + signature);
        }
    }
}