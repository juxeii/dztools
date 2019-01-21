#pragma once
#include "BrokerAsset.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerAsset::BrokerAsset(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    assetId = jniHandler.registerMethod("brokerAsset", "(Ljava/lang/String;)Lcom/jforex/dzjforex/asset/BrokerAssetData;");
}

int BrokerAsset::run(char* assetName,
    double *pPrice,
    double *pSpread,
    double *pVolume,
    double *pPip,
    double *pPipCost,
    double *pLotAmount,
    double *pMarginCost,
    double *pRollLong,
    double *pRollShort)
{
    constexpr int ASSET_AVAILABLE = 1;

    jstring jAsset = env->NewStringUTF(assetName);
    jobject brokerAssetObject = jniHandler.callObjectBridgeMethod(assetId, jAsset);
    ZorroDto assetDto(env, brokerAssetObject);

    int returnCode = assetDto.getReturnCode();
    if (returnCode == ASSET_AVAILABLE) {
        if (pPrice) *pPrice = assetDto.getDouble("price");
        if (pSpread) *pSpread = assetDto.getDouble("spread");
        if (pVolume) *pVolume = assetDto.getDouble("volume");
        if (pPip) *pPip = assetDto.getDouble("pip");
        if (pPipCost) *pPipCost = assetDto.getDouble("pipCost");
        if (pLotAmount) *pLotAmount = assetDto.getDouble("lotAmount");
        if (pMarginCost) *pMarginCost = assetDto.getDouble("marginCost");
        // pRollLong not supported
        // pRollShort not supported
    }
    env->DeleteLocalRef(jAsset);
    env->DeleteLocalRef(brokerAssetObject);

    return returnCode;
}
