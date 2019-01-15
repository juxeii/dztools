#pragma once
#include "BrokerBuy.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerBuy::BrokerBuy(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    buyId = jniHandler.registerMethod("brokerBuy2", "(Ljava/lang/String;IDD)Lcom/jforex/dzjforex/buy/BrokerBuyData;");
}

int BrokerBuy::run(char* assetName,
    int nAmount,
    double dStopDist,
    double limit,
    double *pPrice,
    double *pFill)
{
    jstring jAsset = env->NewStringUTF(assetName);
    jobject brokerBuyObject = jniHandler.callBridgeMethod(buyId, jAsset,
        nAmount,
        dStopDist,
        limit);
    ZorroDto buyDto(env, brokerBuyObject);

    int returnCode = buyDto.getReturnCode();
    if (pPrice) *pPrice = buyDto.getDouble("price");
    if (pFill) {
        double fillAmount = buyDto.getDouble("fill");
        if (fillAmount != 0) *pFill = fillAmount;
    }

    env->DeleteLocalRef(jAsset);
    env->DeleteLocalRef(brokerBuyObject);

    return returnCode;
}