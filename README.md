# Jupiter Exchange API Java版本项目总览

## 项目简介
PS:官方居然没有Java版示例，于是我便翻译了一个完整的Java版本，希望能帮助Java开发者更好地使用Jupiter Exchange API。

这是Jupiter Exchange API Python示例的完整Java版本，使用Hutool工具库提供了一套完整的Java SDK和示例代码，帮助Java开发者快速集成Jupiter Exchange的各种功能。

## 技术栈

- **Java 21+**: 核心开发语言
- **Maven**: 项目构建和依赖管理
- **Hutool**: 目前最流行的Java工具库，提供丰富的工具方法
- **OkHttp**: 高性能HTTP客户端
- **Jackson**: JSON处理
- **SolanaJ**: Solana区块链Java SDK

## 核心功能模块

### 1. Common模块 (通用工具)
- **JupiterConfig**: 环境配置管理，支持.env文件
- **JupiterApiClient**: 统一的API客户端，支持认证和错误处理
- **SolanaWallet**: Solana钱包操作，包括签名和交易发送
- **TokenConstants**: 常用代币地址和常量定义

### 2. Swap API模块 (代币交换)
- **SimpleQuoteAndSwap**: 基础的代币交换功能
- **QuoteAndSwapInstructions**: 获取交换指令，支持自定义交易构建

### 3. Ultra API模块 (高级交易)
- **OrderAndExecute**: 创建和执行Ultra交易订单
- **SellEverythingBackToSol**: 批量卖出所有代币换取SOL

### 4. Recurring API模块 (定期交易)
- **SimpleCreateOrderAndExecute**: 创建定期执行的交易订单
- **CancelOrder**: 取消定期交易订单

### 5. Trigger API模块 (触发交易)
- **PriceTriggerExample**: 基于价格条件的触发交易

## 项目特色

### 1. 完全中文化
- 所有注释和文档都使用中文
- 错误信息和日志输出中文化
- 符合中国开发者使用习惯

### 2. 使用Hutool工具库
- 简化JSON处理：使用`JSONUtil`和`JSONObject`
- 便捷的编码解码：`Base58`、`Base64`等
- 环境变量管理：`Props`类读取.env文件
- 字符串工具：`StrUtil`进行字符串操作

### 3. 模块化设计
- 清晰的模块分离，每个API类型独立模块
- 通用功能抽取到common模块
- 便于维护和扩展

### 4. 完善的错误处理
- 统一的异常处理机制
- 详细的错误信息输出
- 优雅的资源清理

## 与Python版本的对应关系

| Python示例 | Java对应类 | 功能说明 |
|------------|------------|----------|
| `swap-api/simple-quote-and-swap/main.py` | `SimpleQuoteAndSwap` | 简单代币交换 |
| `swap-api/simple-quote-and-swap-instructions/main.py` | `QuoteAndSwapInstructions` | 获取交换指令 |
| `ultra-api/order-and-execute/main.py` | `OrderAndExecute` | Ultra API订单执行 |
| `ultra-api/sell-everything-back-to-sol/main.py` | `SellEverythingBackToSol` | 批量卖出代币 |
| `recurring-api/simple-create-order-and-execute/main.py` | `SimpleCreateOrderAndExecute` | 定期交易创建 |
| `recurring-api/cancel-order/main.py` | `CancelOrder` | 取消定期交易 |
| `trigger-api/simple-create-order-and-execute/main.py` | `PriceTriggerExample` | 价格触发交易 |

## 快速开始

### 1. 环境准备
```bash
# 确保Java 21+和Maven已安装
java -version
mvn -version
```

### 2. 项目配置
```bash
# 克隆项目
git clone <your-repo-url>
cd java-examples

# 配置环境变量
cp .env-example .env
# 编辑.env文件填入您的配置

# 编译项目
mvn clean install
```

### 3. 运行示例
```bash
# 使用便捷脚本运行
./run-example.sh swap-api SimpleQuoteAndSwap

# 或直接使用Maven
cd swap-api
mvn exec:java -Dexec.mainClass="com.jupiter.swap.SimpleQuoteAndSwap"
```

## 开发指南

### 添加新的API示例
1. 在对应模块下创建新的Java类
2. 继承或使用common模块的工具类
3. 实现具体的API调用逻辑
4. 添加适当的错误处理和日志输出

### 扩展功能
1. 在common模块添加通用工具方法
2. 在constants包中添加新的常量定义
3. 更新相关文档和示例

## 最佳实践

1. **安全性**: 使用环境变量管理敏感信息
2. **错误处理**: 实现完善的异常处理机制
3. **资源管理**: 及时关闭HTTP客户端等资源
4. **代码规范**: 遵循Java编码规范和注释标准

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目：

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 许可证

本项目采用MIT许可证，与原Python项目保持一致。

---

**注意**: 这是一个完整的Jupiter Exchange API Java实现，包含了原Python项目的所有核心功能，并针对Java开发者进行了优化和改进。