package com.jupiter.swap;

import cn.hutool.json.JSONObject;
import com.jupiter.common.client.JupiterApiClient;
import com.jupiter.common.config.JupiterConfig;
import com.jupiter.common.constants.TokenConstants;
import com.jupiter.common.wallet.SolanaWallet;
// 移除旧的 solanaj 导入

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的报价和交换示例
 * 演示如何获取WSOL到USDC的交换报价并执行交换
 */
public class SimpleQuoteAndSwap {
    
    public static void main(String[] args) {
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始获取交换报价...");
            
            // 获取交换报价
            JSONObject quoteData = getSwapQuote(apiClient, wallet);
            System.out.println("报价响应: " + quoteData);
            
            // 获取交换交易
            JSONObject swapData = getSwapTransaction(apiClient, wallet, quoteData);
            System.out.println("交换响应: " + swapData);
            
            // 签署并发送交易
            String transactionBase64 = swapData.getStr("swapTransaction");
            String signature = wallet.signAndSendTransactionFromBase64(transactionBase64);
            
            System.out.println("交易发送成功！签名: " + signature);
            System.out.println("在Solscan上查看交易: https://solscan.io/tx/" + signature);
            
            // 清理资源
            apiClient.close();
            
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取交换报价
     */
    private static JSONObject getSwapQuote(JupiterApiClient apiClient, SolanaWallet wallet) throws IOException {
        Map<String, Object> quoteParams = new HashMap<>();
        quoteParams.put("inputMint", TokenConstants.WSOL);  // WSOL
        quoteParams.put("outputMint", TokenConstants.USDC); // USDC
        quoteParams.put("amount", 10_000_000L); // 0.01 WSOL
        
        return apiClient.get("/swap/v1/quote", quoteParams);
    }
    
    /**
     * 获取交换交易
     */
    private static JSONObject getSwapTransaction(JupiterApiClient apiClient, SolanaWallet wallet, JSONObject quoteData) throws IOException {
        Map<String, Object> swapRequest = new HashMap<>();
        swapRequest.put("userPublicKey", wallet.getPublicKey().toString());
        swapRequest.put("quoteResponse", quoteData);
        
        return apiClient.post("/swap/v1/swap", swapRequest);
    }
}