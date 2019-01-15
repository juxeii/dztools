#pragma once
#include "BrokerSubscribe.hpp"
#include "JNIHandler.hpp"

BrokerSubscribe::BrokerSubscribe(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    subscribeId = jniHandler.registerMethod("brokerSubscribeAsset", "(Ljava/lang/String;)I");
}

int BrokerSubscribe::run(const char* assetName)
{
    jstring jAsset = env->NewStringUTF(assetName);
    jint res = (jint)jniHandler.callBridgeMethod(subscribeId, jAsset);
    env->DeleteLocalRef(jAsset);

    return res;
}