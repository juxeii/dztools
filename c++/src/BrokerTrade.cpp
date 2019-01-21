#pragma once
#include "BrokerTrade.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerTrade::BrokerTrade(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    tradeId = jniHandler.registerMethod("brokerTrade", "(I)Lcom/jforex/dzjforex/trade/BrokerTradeData;");
}

int BrokerTrade::run(const int nTradeID,
    double *pOpen,
    double *pClose,
    double *pRoll,
    double *pProfit)
{
    jobject brokerTradeObject = jniHandler.callObjectBridgeMethod(tradeId, nTradeID);
    ZorroDto tradeDto(env, brokerTradeObject);

    int returnCode = tradeDto.getReturnCode();
    if (pOpen) *pOpen = tradeDto.getDouble("open");
    if (pClose) *pClose = tradeDto.getDouble("close");
    if (pProfit) *pProfit = tradeDto.getDouble("profit");
    //pRoll not supported
    env->DeleteLocalRef(brokerTradeObject);

    return returnCode;
}