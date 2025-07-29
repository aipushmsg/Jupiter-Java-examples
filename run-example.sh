#!/bin/bash

# Jupiter Exchange API Java示例运行脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 显示使用说明
show_usage() {
    echo -e "${BLUE}Jupiter Exchange API Java示例运行脚本${NC}"
    echo ""
    echo "用法: $0 <模块> <示例类>"
    echo ""
    echo "可用模块和示例："
    echo -e "${GREEN}swap-api:${NC}"
    echo "  - SimpleQuoteAndSwap           # 简单交换示例"
    echo "  - QuoteAndSwapInstructions     # 交换指令示例"
    echo ""
    echo -e "${GREEN}ultra-api:${NC}"
    echo "  - OrderAndExecute              # 订单创建和执行"
    echo "  - SellEverythingBackToSol      # 卖出所有代币换SOL"
    echo ""
    echo -e "${GREEN}recurring-api:${NC}"
    echo "  - SimpleCreateOrderAndExecute  # 创建定期交易订单"
    echo "  - CancelOrder                  # 取消定期交易订单"
    echo ""
    echo -e "${GREEN}trigger-api:${NC}"
    echo "  - PriceTriggerExample          # 价格触发交易示例"
    echo ""
    echo "示例："
    echo "  $0 swap-api SimpleQuoteAndSwap"
    echo "  $0 ultra-api OrderAndExecute"
    echo "  $0 recurring-api CancelOrder ORDER_ID_HERE"
}

# 检查参数
if [ $# -lt 2 ]; then
    show_usage
    exit 1
fi

MODULE=$1
CLASS=$2
shift 2
ARGS="$@"

# 检查.env文件
if [ ! -f ".env" ]; then
    echo -e "${RED}错误: 未找到.env文件${NC}"
    echo -e "${YELLOW}请复制.env-example为.env并填入您的配置${NC}"
    echo "cp .env-example .env"
    exit 1
fi

# 检查模块是否存在
if [ ! -d "$MODULE" ]; then
    echo -e "${RED}错误: 模块 '$MODULE' 不存在${NC}"
    show_usage
    exit 1
fi

echo -e "${BLUE}正在编译项目...${NC}"
mvn clean install -q

echo -e "${BLUE}正在运行示例: $MODULE/$CLASS${NC}"
cd "$MODULE"

if [ -n "$ARGS" ]; then
    mvn exec:java -Dexec.mainClass="com.jupiter.$MODULE.$CLASS" -Dexec.args="$ARGS" -q
else
    mvn exec:java -Dexec.mainClass="com.jupiter.$MODULE.$CLASS" -q
fi

echo -e "${GREEN}示例执行完成${NC}"