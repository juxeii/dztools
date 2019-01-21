#pragma once
#include "BrokerTime.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerTime::BrokerTime(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    timeId = jniHandler.registerMethod("brokerTime", "()Lcom/jforex/dzjforex/time/BrokerTimeData;");
}

int BrokerTime::run(DATE *pTimeUTC)
{
    constexpr int CONNECTION_LOST_NEW_LOGIN_REQUIRED = 0;

    jobject brokerTimeObject =jniHandler.callObjectBridgeMethod(timeId);
    ZorroDto timeDto(env, brokerTimeObject);

    int returnCode = timeDto.getReturnCode();
    if (returnCode != CONNECTION_LOST_NEW_LOGIN_REQUIRED && pTimeUTC) *pTimeUTC = timeDto.getDouble("serverTime");

    env->DeleteLocalRef(brokerTimeObject);

    return returnCode;
}