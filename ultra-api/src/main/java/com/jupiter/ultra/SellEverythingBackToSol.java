package com.jupiter.ultra;

import cn.hutool.core.codec.Base64;
import cn.hutool.json.JSONArray;
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
 * 将所有代币卖回SOL的示例
 * 演示如何查询钱包余额并将所有代币交换回SOL
 */
public class SellEverythingBackToSol {
    
    public static void main(String[] args) {
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始查询钱包余额...");
            
            // 获取钱包中的所有代币余额
            JSONArray tokenBalances = getTokenBalances(apiClient, wallet);
            
            if (tokenBalances.isEmpty()) {
                System.out.println("钱包中没有找到代币余额");
                return;
            }
            
            System.out.println("找到 " + tokenBalances.size() + " 个代币余额");
            
            // 为每个非SOL代币创建卖出订单
            for (int i = 0; i < tokenBalances.size(); i++) {
                JSONObject tokenBalance = tokenBalances.getJSONObject(i);
                String mint = tokenBalance.getStr("mint");
                long amount = tokenBalance.getLong("amount");
                
                // 跳过SOL和WSOL
                if (TokenConstants.WSOL.equals(mint) || amount == 0) {
                    continue;
                }
                
                System.out.println("正在处理代币: " + mint + ", 数量: " + amount);
                
                try {
                    // 创建卖出订单
                    sellTokenToSol(apiClient, wallet, mint, amount);
                } catch (Exception e) {
                    System.err.println("处理代币 " + mint + " 时出错: " + e.getMessage());
                }
            }
            
            // 清理资源
            apiClient.close();
            
        } catch (Exception e) {
            System.err.println("执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取钱包代币余额
     */
    private static JSONArray getTokenBalances(JupiterApiClient apiClient, SolanaWallet wallet) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("wallet", wallet.getPublicKey().toString());
        
        JSONObject response = apiClient.get("/balance/v1/tokens", params);
        return response.getJSONArray("tokens");
    }
    
    /**
     * 将指定代币卖出换取SOL
     */
    private static void sellTokenToSol(JupiterApiClient apiClient, SolanaWallet wallet, String inputMint, long amount) throws IOException {
        // 创建卖出订单
        Map<String, Object> orderParams = new HashMap<>();
        orderParams.put("inputMint", inputMint);
        orderParams.put("outputMint", TokenConstants.WSOL); // 卖出换取WSOL
        orderParams.put("amount", amount);
        orderParams.put("taker", wallet.getPublicKey().toString());
        
        JSONObject orderResponse = apiClient.get("/ultra/v1/order", orderParams);
        
        if (orderResponse.containsKey("error")) {
            System.err.println("创建卖出订单失败: " + orderResponse.getStr("error"));
            return;
        }
        
        System.out.println("卖出订单创建成功: " + inputMint);
        
        // 签署交易
        String signedTransactionBase64 = signTransaction(wallet, orderResponse.getStr("transaction"));
        
        // 执行订单
        executeOrder(apiClient, orderResponse.getStr("requestId"), signedTransactionBase64, inputMint);
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
    private static void executeOrder(JupiterApiClient apiClient, String requestId, String signedTransaction, String tokenMint) throws IOException {
        Map<String, Object> executeRequest = new HashMap<>();
        executeRequest.put("signedTransaction", signedTransaction);
        executeRequest.put("requestId", requestId);
        
        JSONObject executeResponse = apiClient.post("/ultra/v1/execute", executeRequest);
        
        String signature = executeResponse.getStr("signature");
        String status = executeResponse.getStr("status");
        
        if ("Success".equals(status)) {
            System.out.println("代币 " + tokenMint + " 卖出成功！签名: " + signature);
            System.out.println("在Solscan上查看交易: https://solscan.io/tx/" + signature);
        } else {
            String errorCode = executeResponse.getStr("code");
            String errorMessage = executeResponse.getStr("error");
            
            System.err.println("代币 " + tokenMint + " 卖出失败！签名: " + signature);
            System.err.println("错误代码: " + errorCode);
            System.err.println("错误消息: " + errorMessage);
        }
    }
}