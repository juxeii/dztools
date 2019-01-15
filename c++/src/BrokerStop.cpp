#pragma once
#include "BrokerStop.hpp"
#include "JNIHandler.hpp"

BrokerStop::BrokerStop(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    stopId = jniHandler.registerMethod("brokerStop", "(ID)I");
}

int BrokerStop::run(const int nTradeID, const double dStop)
{
    return (jint)jniHandler.callBridgeMethod(stopId, nTradeID, dStop);
}