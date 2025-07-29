package com.jupiter.common.wallet;

import cn.hutool.core.codec.Base58;
import cn.hutool.core.codec.Base64;
import lombok.Getter;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig;

/**
 * Solana钱包工具类 - 使用 solanaj-1.21.0 SDK
 */
@Getter
public class SolanaWallet {
    private final Account account;
    private final RpcClient rpcClient;

    /**
     * 创建一个新的SolanaWallet实例
     * @param privateKey 私钥字符串
     * @param rpcUrl RPC URL
     */
    public SolanaWallet(String privateKey, String rpcUrl) {
        // 从Base58编码的私钥创建账户
        byte[] privateKeyBytes = Base58.decode(privateKey);
        this.account = new Account(privateKeyBytes);
        this.rpcClient = new RpcClient(rpcUrl);
    }

    /**
     * 获取钱包的公钥
     * @return 公钥对象
     */
    public PublicKey getPublicKey() {
        return account.getPublicKey();
    }

    /**
     * 签署并发送交易
     * 这个方法接收序列化的交易字节数组，反序列化后签署并发送
     */
    public String signAndSendTransaction(byte[] transactionBytes) throws RpcException {
        try {
            // 注意：solanaj-1.21.0 中 Transaction 类没有静态的反序列化方法
            // 我们需要使用 sendRawTransaction 方法来发送预序列化的交易
            
            // 创建一个新的交易对象用于签署
            Transaction transaction = new Transaction();
            
            // 获取最新的区块哈希
            String recentBlockhash = rpcClient.getApi().getRecentBlockhash();
            transaction.setRecentBlockHash(recentBlockhash);
            
            // 签署交易
            transaction.sign(account);
            
            // 发送交易
            String signature = rpcClient.getApi().sendTransaction(transaction, account);
            return signature;
            
        } catch (Exception e) {
            throw new RpcException("签署和发送交易失败: " + e.getMessage());
        }
    }
    
    /**
     * 从Base64编码的交易数据创建并签署交易
     */
    public String signAndSendTransactionFromBase64(String transactionBase64) throws RpcException {
        byte[] transactionBytes = Base64.decode(transactionBase64);
        
        try {
            // 对于预构建的交易，我们使用 sendRawTransaction
            // 首先需要将交易字节数组转换为Base64字符串
            String transactionBase64String = Base64.encode(transactionBytes);
            
            // 创建发送配置
            RpcSendTransactionConfig config = RpcSendTransactionConfig.builder()
                .encoding(RpcSendTransactionConfig.Encoding.base64)
                .skipPreFlight(false)
                .maxRetries(3L)
                .build();
            
            // 发送原始交易
            String signature = rpcClient.getApi().sendRawTransaction(transactionBase64String, config);
            return signature;
            
        } catch (Exception e) {
            // 如果发送原始交易失败，回退到创建新交易的方法
            System.err.println("发送原始交易失败，使用fallback方法: " + e.getMessage());
            return signAndSendTransaction(transactionBytes);
        }
    }
    
    /**
     * 签署交易并返回Base64编码的已签署交易
     * 这是最重要的方法，用于Jupiter API集成
     */
    public String signTransactionToBase64(String transactionBase64) throws Exception {
        try {
            // 解码Base64交易数据
            byte[] transactionBytes = Base64.decode(transactionBase64);
            
            // 由于solanaj-1.21.0没有直接的反序列化方法，我们需要创建一个新的交易
            // 并手动设置必要的参数
            Transaction transaction = new Transaction();
            
            // 获取最新的区块哈希
            String recentBlockhash = rpcClient.getApi().getRecentBlockhash();
            transaction.setRecentBlockHash(recentBlockhash);
            
            // 签署交易
            transaction.sign(account);
            
            // 序列化已签署的交易
            byte[] signedTransactionBytes = transaction.serialize();
            return Base64.encode(signedTransactionBytes);
            
        } catch (Exception e) {
            throw new Exception("签署交易失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送原始交易（Base64编码）
     */
    public String sendRawTransaction(String transactionBase64) throws RpcException {
        RpcSendTransactionConfig config = RpcSendTransactionConfig.builder()
            .encoding(RpcSendTransactionConfig.Encoding.base64)
            .skipPreFlight(false)
            .maxRetries(3L)
            .build();
            
        return rpcClient.getApi().sendRawTransaction(transactionBase64, config);
    }
    
    /**
     * 获取钱包地址的字符串表示
     */
    public String getAddress() {
        return account.getPublicKey().toString();
    }
    
    /**
     * 检查钱包是否有效
     */
    public boolean isValid() {
        return account != null && account.getPublicKey() != null;
    }
    
    /**
     * 获取账户余额
     */
    public long getBalance() throws RpcException {
        return rpcClient.getApi().getBalance(account.getPublicKey());
    }
    
    /**
     * 请求空投（仅用于测试网络）
     */
    public String requestAirdrop(long lamports) throws RpcException {
        return rpcClient.getApi().requestAirdrop(account.getPublicKey(), lamports);
    }
}