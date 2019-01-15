#pragma once
#include "BrokerSell.hpp"
#include "JNIHandler.hpp"

BrokerSell::BrokerSell(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    sellId = jniHandler.registerMethod("brokerSell", "(II)I");
}

int BrokerSell::run(const int nTradeID, const int nAmount)
{
    return (jint)jniHandler.callBridgeMethod(sellId, nTradeID, nAmount);
}