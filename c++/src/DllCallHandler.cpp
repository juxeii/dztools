#include "DllCallHandler.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"
#include <cstring>
#include <bitset>

bool isPatchValueActive(PatchValue pv) {
    std::bitset<32> bitset(bcPatch);
    return bitset.test(pv);
}

DllCallHandler::DllCallHandler(const JNIHandler& jniHandler)
    :jniHandler(jniHandler)
{}

int
DllCallHandler::BrokerLogin(const char *User,
    const char *Pwd,
    const char *Type,
    char *Account)
{
    jniHandler.init();

    env = jniHandler.getJNIEnvironment();

    constexpr int LOGIN_OK = 1;
    constexpr int ACCOUNT_LENGTH = 1024;
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

    jobject brokerLoginObject = env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerLogin,
        jUser,
        jPwd,
        jType);
    ZorroDto loginDto(env, brokerLoginObject);

    int returnCode = loginDto.getReturnCode();
    if (returnCode == LOGIN_OK && Account) {
        std::string accountstr = loginDto.getString("accountName");
        std::size_t accountLength = accountstr.size();
        if (accountLength > ACCOUNT_LENGTH - 1)
        {
            char str[80];
            sprintf_s(str, "Account names too long -> truncated! Length %i", accountLength);
            puts(str);
            BrokerError(str);
        }
        strncpy_s(Account, ACCOUNT_LENGTH, accountstr.c_str(), ACCOUNT_LENGTH - 1);
    }

    env->DeleteLocalRef(jUser);
    env->DeleteLocalRef(jPwd);
    env->DeleteLocalRef(jType);
    env->DeleteLocalRef(brokerLoginObject);

    return returnCode;
}

int
DllCallHandler::BrokerLogout()
{
    return (jlong)env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerLogout);
}

int
DllCallHandler::BrokerTime(DATE *pTimeUTC)
{
    constexpr int CONNECTION_LOST_NEW_LOGIN_REQUIRED = 0;

    jobject brokerTimeObject = env->CallObjectMethod(jniHandler.zorroBridgeObject, jniHandler.brokerTime);
    ZorroDto timeDto(env, brokerTimeObject);

    int returnCode = timeDto.getReturnCode();
    if (returnCode != CONNECTION_LOST_NEW_LOGIN_REQUIRED && pTimeUTC) *pTimeUTC = timeDto.getDouble("serverTime");

    env->DeleteLocalRef(brokerTimeObject);

    return returnCode;
}

int
DllCallHandler::SubscribeAsset(const char* Asset)
{
    jstring jAsset = env->NewStringUTF(Asset);

    jint res = (jlong)env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerSubscribeAsset,
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
    jobject brokerAssetObject = env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerAsset,
        jAsset);
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

int
DllCallHandler::BrokerHistory2(const char *Asset,
    const DATE tStart,
    const DATE tEnd,
    const int nTickMinutes,
    const int nTicks,
    T6 *ticks)
{
    constexpr int BROKER_HISTORY_UNAVAILABLE = 0;

    jstring jAsset = env->NewStringUTF(Asset);
    jobject brokerHistoryObject = env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerHistory2,
        jAsset,
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
    constexpr int ACCOUNT_AVAILABLE = 1;

    jobject brokerAccountObject = env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerAccount);
    ZorroDto accountDto(env, brokerAccountObject);

    int returnCode = accountDto.getReturnCode();
    if (returnCode == ACCOUNT_AVAILABLE) {
        if (pBalance) *pBalance = accountDto.getDouble("balance");
        if (pTradeVal) *pTradeVal = accountDto.getDouble("tradeVal");
        if (pMarginVal) *pMarginVal = accountDto.getDouble("marginVal");
    }
    env->DeleteLocalRef(brokerAccountObject);

    return returnCode;
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
    jobject brokerBuysObject = env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerBuy2,
        jAsset,
        nAmount,
        dStopDist,
        limit);
    ZorroDto buyDto(env, brokerBuysObject);

    int returnCode = buyDto.getReturnCode();
    if (pPrice) *pPrice = buyDto.getDouble("price");
    if (pFill) {
        double fillAmount = buyDto.getDouble("fill");
        if (fillAmount != 0) *pFill = fillAmount;
    }

    env->DeleteLocalRef(jAsset);
    env->DeleteLocalRef(brokerBuysObject);

    return returnCode;
}

int
DllCallHandler::BrokerTrade(const int nTradeID,
    double *pOpen,
    double *pClose,
    double *pRoll,
    double *pProfit)
{
    jobject brokerTradeObject = env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerTrade,
        nTradeID);
    ZorroDto tradeDto(env, brokerTradeObject);

    int returnCode = tradeDto.getReturnCode();
    if (pOpen) *pOpen = tradeDto.getDouble("open");
    if (pClose) *pClose = tradeDto.getDouble("close");
    if (pProfit) *pProfit = tradeDto.getDouble("profit");
    //pRoll not supported
    env->DeleteLocalRef(brokerTradeObject);

    return returnCode;
}

int
DllCallHandler::BrokerStop(const int nTradeID,
    const double dStop)
{
    return (jlong)env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerStop,
        nTradeID,
        dStop);
}

int
DllCallHandler::BrokerSell(const int nTradeID,
    const int nAmount)
{
    return (jlong)env->CallObjectMethod(jniHandler.zorroBridgeObject,
        jniHandler.brokerSell,
        nTradeID,
        nAmount);
}

var
DllCallHandler::bcForText(char* orderText, int nCommand)
{
    jstring jText = env->NewStringUTF(orderText);
    jdouble returnCode = env->CallDoubleMethod(jniHandler.zorroBridgeObject,
        jniHandler.getBcMethodId(nCommand),
        jText);
    env->DeleteLocalRef(jText);

    return returnCode;
}

var
DllCallHandler::bcForGetString(char *stringToWrite, int nCommand)
{
    jobject brokerCommandDataObject = env->CallObjectMethod(jniHandler.zorroBridgeObject, jniHandler.getBcMethodId(nCommand));
    ZorroDto commandDto(env, brokerCommandDataObject);
    int returnCode = commandDto.getReturnCode();
    if (returnCode != 0) {
        std::string string = commandDto.getString("data");
        strncpy_s(stringToWrite, string.size(), string.c_str(), string.size());
    }
    env->DeleteLocalRef(brokerCommandDataObject);


    return  commandDto.getReturnCode();
}

var
DllCallHandler::bcForDouble(double value, int nCommand)
{

    return env->CallDoubleMethod(jniHandler.zorroBridgeObject,
        jniHandler.getBcMethodId(nCommand),
        value);
}

var
DllCallHandler::bcNoParam(int nCommand)
{
    return env->CallDoubleMethod(jniHandler.zorroBridgeObject, jniHandler.getBcMethodId(nCommand));
}
