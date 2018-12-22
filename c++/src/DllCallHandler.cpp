#include "DllCallHandler.hpp"
#include "JNIHandler.hpp"
#include "JReferences.hpp"
#include <cstring>

int
DllCallHandler::BrokerLogin(const char *User,
                            const char *Pwd,
                            const char *Type,
                            char *Account)
{
    jniHandler.init();

    env = jniHandler.getJNIEnvironment();
    jstring jUser;
    jstring jPwd;
    jstring jType;
    if (User)
        jUser = env->NewStringUTF(User);
    else
        jUser = env->NewStringUTF("");

    if (Pwd)
        jPwd = env->NewStringUTF(Pwd);
    else
        jPwd = env->NewStringUTF("");

    if (Type)
        jType = env->NewStringUTF(Type);
    else
        jType = env->NewStringUTF("");

    jobjectArray jAccountArray = (jobjectArray) env->NewObjectArray(1,
                                                                    env->FindClass("java/lang/String"),
                                                                    env->NewStringUTF(""));
    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doLogin.methodID,
                                             jUser,
                                             jPwd,
                                             jType,
                                             jAccountArray);

    if (Account)
    {
        jstring jAccount = (jstring) env->GetObjectArrayElement(jAccountArray, 0);
        char* accountID = const_cast<char*>(env->GetStringUTFChars(jAccount, NULL));
        std::size_t nPos = strlen(accountID);
        if (nPos > 1023)
        {
            BrokerError("Account number too big -> truncated");
            nPos = 1023;
        }
        strncpy_s(Account, 1023, accountID, nPos);
        Account[nPos] = 0;
        env->DeleteLocalRef(jAccount);
    }

    env->DeleteLocalRef(jUser);
    env->DeleteLocalRef(jPwd);
    env->DeleteLocalRef(jType);
    env->DeleteLocalRef(jAccountArray);

    return res;
}

int
DllCallHandler::BrokerLogout()
{
    return (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                         JData::doLogout.methodID);
}

int
DllCallHandler::BrokerTime(DATE *pTimeUTC)
{
    jdoubleArray utcTimeArray = env->NewDoubleArray(1);
    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doBrokerTime.methodID,
                                             utcTimeArray);

    if (pTimeUTC)
    {
        jdouble *utcTime = env->GetDoubleArrayElements(utcTimeArray, 0);
        *pTimeUTC = utcTime[0];
        env->ReleaseDoubleArrayElements(utcTimeArray, utcTime, 0);
    }
    env->DeleteLocalRef((jobject) utcTimeArray);

    return res;
}

int
DllCallHandler::SubscribeAsset(const char* Asset)
{
    jstring jAsset = env->NewStringUTF(Asset);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doSubscribeAsset.methodID,
                                             jAsset);
    env->DeleteLocalRef(jAsset);

    return res;
}

int
DllCallHandler::BrokerAsset(char* Asset,
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
    jstring jAsset = env->NewStringUTF(Asset);
    jdoubleArray jAssetParamsArray = env->NewDoubleArray(9);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doBrokerAsset.methodID,
                                             jAsset,
                                             jAssetParamsArray);
    jdouble *assetParams = env->GetDoubleArrayElements(jAssetParamsArray, 0);

    if (pPrice)
        *pPrice = assetParams[0];
    if (pSpread)
        *pSpread = assetParams[1];
    if (pVolume)
        *pVolume = assetParams[2];
    if (pPip)
        *pPip = assetParams[3];
    if (pPipCost)
        *pPipCost = assetParams[4];
    if (pLotAmount)
        *pLotAmount = assetParams[5];
    if (pMarginCost)
        *pMarginCost = assetParams[6];
    if (pRollLong)
        *pRollLong = assetParams[7];
    if (pRollShort)
        *pRollShort = assetParams[8];

    env->DeleteLocalRef(jAsset);
    env->ReleaseDoubleArrayElements(jAssetParamsArray, assetParams, 0);
    env->DeleteLocalRef((jobject) jAssetParamsArray);

    return res;
}

int
DllCallHandler::BrokerHistory2(const char *Asset,
                               const DATE tStart,
                               const DATE tEnd,
                               const int nTickMinutes,
                               const int nTicks,
                               T6 *ticks)
{
    const int t6StructSize = 7;
    jstring jAsset = env->NewStringUTF(Asset);
    int tickArrayLength = nTicks * t6StructSize;
    jdoubleArray jTicksArray = env->NewDoubleArray(tickArrayLength);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doBrokerHistory2.methodID,
                                             jAsset,
                                             tStart,
                                             tEnd,
                                             nTickMinutes,
                                             nTicks,
                                             jTicksArray);
    jdouble *ticksParams = env->GetDoubleArrayElements(jTicksArray, 0u);

    for (int i = 0; i < nTicks; ++i)
    {
        int paramsIndex = i * t6StructSize;
        ticks[i].fOpen = ticksParams[paramsIndex];
        ticks[i].fClose = ticksParams[paramsIndex + 1];
        ticks[i].fHigh = ticksParams[paramsIndex + 2];
        ticks[i].fLow = ticksParams[paramsIndex + 3];
        ticks[i].time = ticksParams[paramsIndex + 4];
        //ticks[i].fVal = ticksParams[paramsIndex + 5];
        ticks[i].fVol = ticksParams[paramsIndex + 6];

        if (!BrokerProgress(100 * i / nTicks))
            break;
    }
    env->DeleteLocalRef(jAsset);
    env->ReleaseDoubleArrayElements(jTicksArray, ticksParams, 0u);
    env->DeleteLocalRef((jobject) jTicksArray);

    return res;
}

int
DllCallHandler::BrokerAccount(const char *Account,
                              double *pBalance,
                              double *pTradeVal,
                              double *pMarginVal)
{
    if (Account)
    {
        BrokerError("Multiple accounts are not yet supported!");
        return 0;
    }
    jdoubleArray jAccountParamsArray = env->NewDoubleArray(3);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doBrokerAccount.methodID,
                                             jAccountParamsArray);
    jdouble *accountParams = env->GetDoubleArrayElements(jAccountParamsArray, 0);

    if (pBalance)
        *pBalance = accountParams[0];
    if (pTradeVal)
        *pTradeVal = accountParams[1];
    if (pMarginVal)
        *pMarginVal = accountParams[2];

    env->ReleaseDoubleArrayElements(jAccountParamsArray,
                                    accountParams,
                                    0);
    env->DeleteLocalRef((jobject) jAccountParamsArray);

    return res;
}

int
DllCallHandler::BrokerBuy2(char* Asset,
        int nAmount,
        double dStopDist,
        double limit,
        double *pPrice,
        double *pFill)
{
    jstring jAsset = env->NewStringUTF(Asset);
    jdoubleArray jTradeParamsArray = env->NewDoubleArray(2);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doBrokerBuy2.methodID,
                                             jAsset,
											 nAmount,
		                                     dStopDist,
                                             limit,
                                             jTradeParamsArray);
    jdouble *tradeParams = env->GetDoubleArrayElements(jTradeParamsArray, 0);

    if (pPrice)
        *pPrice = tradeParams[0];
    if (pFill)
        *pFill = tradeParams[1];

    env->DeleteLocalRef(jAsset);
    env->ReleaseDoubleArrayElements(jTradeParamsArray, tradeParams, 0);
    env->DeleteLocalRef((jobject) jTradeParamsArray);

    return res;
}

int
DllCallHandler::BrokerTrade(const int nTradeID,
                            double *pOpen,
                            double *pClose,
                            double *pRoll,
                            double *pProfit)
{
    jdoubleArray jOrderParamsArray = env->NewDoubleArray(4);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                             JData::doBrokerTrade.methodID,
                                             nTradeID,
                                             jOrderParamsArray);
    jdouble *orderParams = env->GetDoubleArrayElements(jOrderParamsArray, 0);

    if (res > 0)
    {
        if (pOpen)
            *pOpen = orderParams[0];
        if (pClose)
            *pClose = orderParams[1];
        if (pRoll)
            *pRoll = orderParams[2];
        if (pProfit)
            *pProfit = orderParams[3];
    }
    env->ReleaseDoubleArrayElements(jOrderParamsArray,
                                    orderParams,
                                    0);
    env->DeleteLocalRef((jobject) jOrderParamsArray);

    return res;
}

int
DllCallHandler::BrokerStop(const int nTradeID,
                           const double dStop)
{
    return (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                         JData::doBrokerStop.methodID,
                                         nTradeID,
                                         dStop);
}

int
DllCallHandler::BrokerSell(const int nTradeID,
                           const int nAmount)
{
    return (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                         JData::doBrokerSell.methodID,
                                         nTradeID,
                                         nAmount);
}

int
DllCallHandler::SetOrderText(const char *orderText)
{
    jstring jOrderText = env->NewStringUTF(orderText);

    jint res = (jlong) env->CallObjectMethod(JData::JDukaZorroBridgeObject,
                                     JData::doSetOrderText.methodID,
                                     jOrderText);

    env->DeleteLocalRef(jOrderText);

    return res;
}

int
DllCallHandler::SetLimitPrice(const double limitPrice)
{
    return (jlong)env->CallObjectMethod(JData::JDukaZorroBridgeObject,
        JData::doSetLimitPrice.methodID,
        limitPrice);
}
