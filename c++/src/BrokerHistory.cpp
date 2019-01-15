#pragma once
#include "BrokerHistory.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerHistory::BrokerHistory(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    historyId = jniHandler.registerMethod("brokerHistory2", "(Ljava/lang/String;DDII)Lcom/jforex/dzjforex/history/BrokerHistoryData;");
}

int BrokerHistory::run(const char *assetName,
    const DATE tStart,
    const DATE tEnd,
    const int nTickMinutes,
    const int nTicks,
    T6 *ticks)
{
    constexpr int BROKER_HISTORY_UNAVAILABLE = 0;

    jstring jAsset = env->NewStringUTF(assetName);
    jobject brokerHistoryObject = jniHandler.callBridgeMethod(historyId, jAsset,
        tStart,
        tEnd,
        nTickMinutes,
        nTicks);
    ZorroDto historyDto(env, brokerHistoryObject);

    int returnCode = historyDto.getReturnCode();
    if (returnCode != BROKER_HISTORY_UNAVAILABLE) {
        jclass pluginClass = env->GetObjectClass(brokerHistoryObject);

        jfieldID ticksId = env->GetFieldID(pluginClass, "ticks", "Ljava/util/List;");
        jobject listObject = env->GetObjectField(brokerHistoryObject, ticksId);
        jclass listClass = env->FindClass("java/util/List");
        jmethodID getMethodID = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
        jmethodID sizeID = env->GetMethodID(listClass, "size", "()I");
        int listItemsCount = env->CallIntMethod(listObject, sizeID);

        jclass t6Class = env->FindClass("com/jforex/dzjforex/history/T6Data");
        jfieldID timeId = env->GetFieldID(t6Class, "time", "D");
        jfieldID highId = env->GetFieldID(t6Class, "high", "F");
        jfieldID lowId = env->GetFieldID(t6Class, "low", "F");
        jfieldID openId = env->GetFieldID(t6Class, "open", "F");
        jfieldID closeId = env->GetFieldID(t6Class, "close", "F");
        //jfieldID valueId = env->GetFieldID(t6Class, "value", "F");
        jfieldID volumeId = env->GetFieldID(t6Class, "volume", "F");

        for (int i = 0; i < listItemsCount; ++i)
        {
            jobject t6DataObject = env->CallObjectMethod(listObject, getMethodID, i);

            ticks[i].time = (DATE)env->GetDoubleField(t6DataObject, timeId);
            ticks[i].fHigh = (float)env->GetFloatField(t6DataObject, highId);
            ticks[i].fLow = (float)env->GetFloatField(t6DataObject, lowId);
            ticks[i].fOpen = (float)env->GetFloatField(t6DataObject, openId);
            ticks[i].fClose = (float)env->GetFloatField(t6DataObject, closeId);
            ticks[i].fVol = (float)env->GetFloatField(t6DataObject, volumeId);

            /*char str[120];
            sprintf_s(str, "Index %i Time %f high %f low %f, open %f, close %f, volume %f", i, ticks[i].time, ticks[i].fHigh, ticks[i].fLow, ticks[i].fOpen, ticks[i].fClose, ticks[i].fVol);
            puts(str);
            BrokerError(str);*/

            env->DeleteLocalRef(t6DataObject);
            if (BrokerProgress(1) == 0)
                break;
        }
        env->DeleteLocalRef(listObject);
    }
    env->DeleteLocalRef(jAsset);
    env->DeleteLocalRef(brokerHistoryObject);

    return returnCode;
}