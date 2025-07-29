package com.jupiter.swap;

import cn.hutool.json.JSONObject;
import com.jupiter.common.client.JupiterApiClient;
import com.jupiter.common.config.JupiterConfig;
import com.jupiter.common.constants.TokenConstants;
import com.jupiter.common.wallet.SolanaWallet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取交换指令的示例
 * 演示如何获取交换指令而不是完整的交易
 */
public class QuoteAndSwapInstructions {
    
    public static void main(String[] args) {
        try {
            // 初始化配置
            JupiterConfig config = new JupiterConfig();
            JupiterApiClient apiClient = new JupiterApiClient(config);
            SolanaWallet wallet = new SolanaWallet(config.getPrivateKey(), config.getRpcUrl());
            
            System.out.println("开始获取交换报价...");
            
            // 获取交换报价
            JSONObject quoteData = getSwapQuote(apiClient);
            System.out.println("报价响应: " + quoteData);
            
            // 获取交换指令
            JSONObject instructionsData = getSwapInstructions(apiClient, wallet, quoteData);
            System.out.println("交换指令响应: " + instructionsData);
            
            // 显示指令详情
            displayInstructionDetails(instructionsData);
            
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
    private static JSONObject getSwapQuote(JupiterApiClient apiClient) throws IOException {
        Map<String, Object> quoteParams = new HashMap<>();
        quoteParams.put("inputMint", TokenConstants.WSOL);
        quoteParams.put("outputMint", TokenConstants.USDC);
        quoteParams.put("amount", 50_000_000L); // 0.05 WSOL
        quoteParams.put("slippageBps", 50); // 0.5% 滑点
        
        return apiClient.get("/swap/v1/quote", quoteParams);
    }
    
    /**
     * 获取交换指令
     */
    private static JSONObject getSwapInstructions(JupiterApiClient apiClient, SolanaWallet wallet, JSONObject quoteData) throws IOException {
        Map<String, Object> instructionsRequest = new HashMap<>();
        instructionsRequest.put("userPublicKey", wallet.getPublicKey().toString());
        instructionsRequest.put("quoteResponse", quoteData);
        instructionsRequest.put("wrapAndUnwrapSol", true); // 自动包装和解包装SOL
        instructionsRequest.put("useSharedAccounts", true); // 使用共享账户以节省费用
        
        return apiClient.post("/swap/v1/swap-instructions", instructionsRequest);
    }
    
    /**
     * 显示指令详情
     */
    private static void displayInstructionDetails(JSONObject instructionsData) {
        System.out.println("\\n=== 交换指令详情 ===");
        
        // 显示设置指令
        if (instructionsData.containsKey("setupInstructions")) {
            System.out.println("设置指令数量: " + instructionsData.getJSONArray("setupInstructions").size());
        }
        
        // 显示主要交换指令
        if (instructionsData.containsKey("swapInstruction")) {
            JSONObject swapInstruction = instructionsData.getJSONObject("swapInstruction");
            System.out.println("交换指令程序ID: " + swapInstruction.getStr("programId"));
            System.out.println("交换指令账户数量: " + swapInstruction.getJSONArray("accounts").size());
        }
        
        // 显示清理指令
        if (instructionsData.containsKey("cleanupInstructions")) {
            System.out.println("清理指令数量: " + instructionsData.getJSONArray("cleanupInstructions").size());
        }
        
        // 显示地址查找表
        if (instructionsData.containsKey("addressLookupTableAddresses")) {
            System.out.println("地址查找表数量: " + instructionsData.getJSONArray("addressLookupTableAddresses").size());
        }
        
        // 显示优先费用
        if (instructionsData.containsKey("prioritizationFeeLamports")) {
            long priorityFee = instructionsData.getLong("prioritizationFeeLamports");
            System.out.println("优先费用: " + priorityFee + " lamports");
        }
        
        System.out.println("\\n注意: 这些指令可以用于构建自定义交易或与其他指令组合");
    }
}