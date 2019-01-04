#include "DllCallHandler.hpp"
#include "JNIHandler.hpp"
#include "JReferences.hpp"
#include "ZorroDto.hpp"
#include <cstring>
#include <bitset>

bool isPatchValueActive(PatchValue pv) {
    std::bitset<32> bitset(bcPatch);
    return bitset.test(pv);
}

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
    constexpr int CONNECTION_LOST_NEW_LOGIN_REQUIRED = 0;

    jobject brokerTimeObject = env->CallObjectMethod(JData::JDukaZorroBridgeObject, JData::doBrokerTime.methodID);
    ZorroDto timeDto(env, brokerTimeObject);

    int returnCode = timeDto.getReturnCode();
    if (returnCode != CONNECTION_LOST_NEW_LOGIN_REQUIRED) *pTimeUTC = timeDto.getDouble("serverTime");

    return returnCode;
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
    constexpr int ASSET_AVAILABLE = 1;

    jstring jAsset = env->NewStringUTF(Asset);
    jobject brokerAssetObject = env->CallObjectMethod(JData::JDukaZorroBridgeObject,
        JData::doBrokerAsset.methodID,
        jAsset);
    ZorroDto assetDto(env, brokerAssetObject);

    int returnCode = assetDto.getReturnCode();
    if (returnCode == ASSET_AVAILABLE) {
        *pPrice = assetDto.getDouble("price");
        *pSpread = assetDto.getDouble("spread");
        if(pVolume) *pVolume = assetDto.getDouble("volume");
        if(pPip) *pPip = assetDto.getDouble("pip");
        if(pPipCost) *pPipCost = assetDto.getDouble("pipCost");
        if (pLotAmount) *pLotAmount = assetDto.getDouble("lotAmount");
        if (pMarginCost) *pMarginCost = assetDto.getDouble("marginCost");
        // pRollLong not supported
        // pRollShort not supported
    }
    env->DeleteLocalRef(jAsset);

    return returnCode;
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

    if (pBalance) {
        if(!isPatchValueActive(PatchValue::BALANCE_EQUITY)) *pBalance = accountParams[0];
    }
    if (pTradeVal) {
        if (!isPatchValueActive(PatchValue::TRADE_PROFIT_OPEN)) *pTradeVal = accountParams[1];
    }
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
    if (pFill) //not supported

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
            //not supported
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

var
DllCallHandler::BrokerCommand(int command,
                              void* data,
                              int size)
{
    jbyteArray byteArray = env->NewByteArray(size);
    void *temp = env->GetPrimitiveArrayCritical((jarray)byteArray, 0);
    memcpy(temp, data, size);
    env->ReleasePrimitiveArrayCritical(byteArray, temp, 0);

    jdoubleArray returnValueArray = env->NewDoubleArray(1);

    env->CallObjectMethod(JData::JDukaZorroBridgeObject,
        JData::doBrokerCommand.methodID,
        command,
        byteArray,
        returnValueArray);
    jdouble *returnValue = env->GetDoubleArrayElements(returnValueArray, 0);
    double res = (double)returnValue[0];

    switch (command)
    {
    case GET_ACCOUNT:
    {
        jbyte* elements = env->GetByteArrayElements(byteArray, NULL);
        memcpy((char*)data, elements, size);
        env->ReleaseByteArrayElements(byteArray, elements, JNI_ABORT);
    }
    default:
    {}
    }

    env->DeleteLocalRef(byteArray);
    env->ReleaseDoubleArrayElements(returnValueArray,
        returnValue,
        0);
    env->DeleteLocalRef((jobject)returnValueArray);

    return res;
}
