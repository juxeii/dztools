#pragma once
#include <cstring>
#include <bitset>
#include "BrokerCommandHandler.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerCommandHandler::BrokerCommandHandler(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();

    bcSetOrderText = jniHandler.registerMethod("bcSetOrderText", "(Ljava/lang/String;)D");
    bcSetSlippage = jniHandler.registerMethod("bcSetSlippage", "(D)D");
    bcSetLimit = jniHandler.registerMethod("bcSetLimit", "(D)D");
    bcGetAccount = jniHandler.registerMethod("bcGetAccount", "()Lcom/jforex/dzjforex/command/BrokerCommandData;");
    bcGetDigits = jniHandler.registerMethod("bcGetDigits", "(Ljava/lang/String;)D");
    bcGetMaxLot = jniHandler.registerMethod("bcGetMaxLot", "(Ljava/lang/String;)D");
    bcGetMinLot = jniHandler.registerMethod("bcGetMinLot", "(Ljava/lang/String;)D");
    bcGetMarginInit = jniHandler.registerMethod("bcGetMarginInit", "(Ljava/lang/String;)D");
    bcGetTradeAllowed = jniHandler.registerMethod("bcGetTradeAllowed", "(Ljava/lang/String;)D");
	bcGetPosition = jniHandler.registerMethod("bcGetPosition", "(Ljava/lang/String;)D");
    bcGetTime = jniHandler.registerMethod("bcGetTime", "()D");
    bcGetMaxTicks = jniHandler.registerMethod("bcGetMaxTicks", "()D");
    bcGetServerState = jniHandler.registerMethod("bcGetServerState", "()D");
    bcGetNTrades = jniHandler.registerMethod("bcGetNTrades", "()D");

    bcMethodIdMap = {
                {SET_ORDERTEXT, bcSetOrderText},
                {GET_DIGITS, bcGetDigits},
                {GET_MAXLOT, bcGetMaxLot},
                {GET_MINLOT, bcGetMinLot},
                {GET_MARGININIT, bcGetMarginInit},
                {GET_TRADEALLOWED, bcGetTradeAllowed},
                {GET_POSITION, bcGetPosition},
                {GET_TIME, bcGetTime},
                {GET_MAXTICKS, bcGetMaxTicks},
                {GET_SERVERSTATE, bcGetServerState},
                {GET_NTRADES, bcGetNTrades},
                {GET_ACCOUNT, bcGetAccount},
                {SET_SLIPPAGE, bcSetSlippage},
                {SET_LIMIT, bcSetLimit}
    };
}

bool BrokerCommandHandler::isPatchValueActive(PatchValue pv) {
    std::bitset<32> bitset(bcPatch);
    return bitset.test(pv);
}

jmethodID BrokerCommandHandler::getBcMethodId(int nCommand) {
    auto it = bcMethodIdMap.find(nCommand);
    if (it != bcMethodIdMap.end()) {
        return it->second;
    }
    else {
        return nullptr;
    }
}

var BrokerCommandHandler::run(int nCommand, DWORD dwParameter)
{
    switch (nCommand)
    {
    case GET_MAXREQUESTS:
        return 0;
    case GET_PRICETYPE:
        return 1;
    case GET_LOCK:
        return -1;
    case SET_PATCH:
    {
        bcPatch = static_cast<int>(dwParameter);
        return 1;
    }
    case SET_ORDERTEXT:
    case GET_DIGITS:
    case GET_MAXLOT:
    case GET_MINLOT:
    case GET_MARGININIT:
    case GET_TRADEALLOWED:
    case GET_POSITION:
    {
        char* text = reinterpret_cast<char*>(dwParameter);
        return bcForText(text, nCommand);
    }
    case GET_TIME:
    case GET_MAXTICKS:
    case GET_SERVERSTATE:
    case GET_NTRADES:
    {
        return bcNoParam(nCommand);
    }
    case GET_ACCOUNT:
    {
        char* stringToWrite = reinterpret_cast<char*>(dwParameter);
        return bcForGetString(stringToWrite, nCommand);
    }
    case SET_HWND:
    {
        zorroWindow = (HWND)dwParameter;
        return 1;
    }
    case SET_SLIPPAGE:
    {
        double pValue = (double)dwParameter;
        return bcForDouble(pValue, nCommand);
    }
    case SET_LIMIT:
    {
        double* pValue = reinterpret_cast<double*>(dwParameter);
        return bcForDouble(*pValue, nCommand);
    }
    default:
    {
        return 0.0;
    }
    }
}

var BrokerCommandHandler::bcForText(char* orderText, int nCommand)
{
    jstring jText = env->NewStringUTF(orderText);
    jdouble returnCode = jniHandler.callDoubleBridgeMethod(getBcMethodId(nCommand), jText);
    env->DeleteLocalRef(jText);

    return returnCode;
}

var BrokerCommandHandler::bcForGetString(char *stringToWrite, int nCommand)
{
    jobject brokerCommandHandlerDataObject = jniHandler.callBridgeMethod(getBcMethodId(nCommand));
    ZorroDto commandDto(env, brokerCommandHandlerDataObject);
    int returnCode = commandDto.getReturnCode();
    if (returnCode != 0) {
        std::string string = commandDto.getString("data");
        strncpy_s(stringToWrite, string.size(), string.c_str(), string.size());
    }
    env->DeleteLocalRef(brokerCommandHandlerDataObject);

    return  returnCode;
}

var BrokerCommandHandler::bcForDouble(double value, int nCommand)
{
    return jniHandler.callDoubleBridgeMethod(getBcMethodId(nCommand), value);
}

var BrokerCommandHandler::bcNoParam(int nCommand)
{
    return jniHandler.callDoubleBridgeMethod(getBcMethodId(nCommand));
}

HWND BrokerCommandHandler::getZorroHWND()
{
    return zorroWindow;
}