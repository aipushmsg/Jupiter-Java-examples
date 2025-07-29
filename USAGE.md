# Jupiter Exchange API Java示例使用指南

## 项目结构

```
java-examples/
├── common/                    # 通用工具和配置
│   └── src/main/java/com/jupiter/common/
│       ├── config/           # 配置管理
│       ├── client/           # API客户端
│       ├── wallet/           # 钱包工具
│       └── constants/        # 常量定义
├── swap-api/                 # 交换API示例
├── ultra-api/                # Ultra API示例
├── recurring-api/            # 定期交易API示例
├── trigger-api/              # 触发交易API示例
├── .env-example              # 环境变量示例
└── pom.xml                   # Maven主配置
```

## 快速开始

### 1. 环境准备

确保您的系统已安装：
- Java 21 或更高版本
- Maven 3.6 或更高版本

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env-example .env

# 编辑.env文件，填入您的配置
vim .env
```

在`.env`文件中设置：
```
PRIVATE_KEY=your_base58_encoded_private_key
RPC_URL=https://api.mainnet-beta.solana.com
API_KEY=your_jupiter_api_key_optional
```

### 3. 编译项目

```bash
# 在项目根目录执行
mvn clean install
```

## 示例说明

### Swap API - 代币交换

#### 简单交换示例
```bash
cd swap-api
mvn exec:java -Dexec.mainClass="com.jupiter.swap.SimpleQuoteAndSwap"
```

功能：
- 获取WSOL到USDC的交换报价
- 创建交换交易
- 签署并发送交易

#### 交换指令示例
```bash
cd swap-api
mvn exec:java -Dexec.mainClass="com.jupiter.swap.QuoteAndSwapInstructions"
```

功能：
- 获取交换指令而非完整交易
- 适用于需要自定义交易构建的场景

### Ultra API - 高级交易

#### 订单创建和执行
```bash
cd ultra-api
mvn exec:java -Dexec.mainClass="com.jupiter.ultra.OrderAndExecute"
```

功能：
- 创建Ultra交易订单
- 签署并执行订单

#### 卖出所有代币换取SOL
```bash
cd ultra-api
mvn exec:java -Dexec.mainClass="com.jupiter.ultra.SellEverythingBackToSol"
```

功能：
- 查询钱包中的所有代币余额
- 将所有非SOL代币卖出换取SOL

### Recurring API - 定期交易

#### 创建定期交易订单
```bash
cd recurring-api
mvn exec:java -Dexec.mainClass="com.jupiter.recurring.SimpleCreateOrderAndExecute"
```

功能：
- 创建定期执行的交易订单
- 设置交易间隔和次数

#### 取消定期交易订单
```bash
cd recurring-api
mvn exec:java -Dexec.mainClass="com.jupiter.recurring.CancelOrder" -Dexec.args="ORDER_ID"
```

功能：
- 取消指定的定期交易订单

### Trigger API - 触发交易

#### 价格触发交易
```bash
cd trigger-api
mvn exec:java -Dexec.mainClass="com.jupiter.trigger.PriceTriggerExample"
```

功能：
- 创建基于价格条件的触发交易
- 当价格达到设定条件时自动执行

## 核心组件说明

### JupiterConfig
配置管理类，负责：
- 读取环境变量
- 验证配置完整性
- 提供API基础URL选择

### JupiterApiClient
API客户端类，提供：
- HTTP GET/POST请求封装
- 自动API密钥认证
- 错误处理和响应解析

### SolanaWallet
钱包工具类，支持：
- 从私钥创建钱包
- 交易签署
- RPC客户端集成

### TokenConstants
代币常量定义，包含：
- 常用代币的Mint地址
- 精度和单位转换常量

## 最佳实践

### 1. 安全性
- 永远不要在代码中硬编码私钥
- 使用环境变量存储敏感信息
- 在生产环境中使用质押RPC连接

### 2. 错误处理
- 总是检查API响应状态
- 实现适当的重试机制
- 记录详细的错误信息

### 3. 性能优化
- 复用API客户端实例
- 使用连接池管理HTTP连接
- 合理设置超时时间

### 4. 交易管理
- 验证交易参数
- 监控交易状态
- 处理交易失败情况

## 常见问题

### Q: 如何获取私钥？
A: 您可以从Solana钱包（如Phantom、Solflare）导出私钥，确保是Base58编码格式。

### Q: 免费API和付费API有什么区别？
A: 免费API使用`lite-api.jup.ag`，有速率限制；付费API使用`api.jup.ag`，需要API密钥，有更高的限制。

### Q: 如何处理交易失败？
A: 检查错误代码和消息，常见原因包括余额不足、滑点过大、网络拥堵等。

### Q: 支持哪些代币？
A: Jupiter支持Solana生态系统中的大部分SPL代币，具体列表可通过API查询。

## 支持和社区

- [Jupiter官方文档](https://station.jup.ag/docs/)
- [Jupiter Discord社区](https://discord.gg/jup)
- [GitHub Issues](https://github.com/your-repo/java-examples/issues)

## 许可证

本项目采用MIT许可证，详见LICENSE文件。