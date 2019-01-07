#include "dukazorrobridge.hpp"
#include "DllCallHandler.hpp"
#include "JReferences.hpp"
#include <string>

#define DLLFUNC extern "C" __declspec(dllexport)

int
(__cdecl *BrokerError)(const char *txt) = nullptr;
int
(__cdecl *BrokerProgress)(const int percent) = nullptr;
static DllCallHandler dllCallHandler;

BOOL APIENTRY
DllMain(HMODULE hModule,
    DWORD ul_reason_for_call,
    LPVOID lpReserved)
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
        break;
    case DLL_PROCESS_DETACH:
        break;
    case DLL_THREAD_ATTACH:
        break;
    case DLL_THREAD_DETACH:
        break;
    }
    return TRUE;
}

int
jcallback_BrokerError(JNIEnv *env,
    jclass clazz,
    jstring msg)
{
    int result = 0;
    const char *ctxt = env->GetStringUTFChars(msg, nullptr);
    if (ctxt != nullptr)
    {
        result = BrokerError(ctxt);
        env->ReleaseStringUTFChars(msg, ctxt);
    }
    return result;
}

int
jcallback_BrokerProgress(JNIEnv *env,
    jclass clazz,
    jint progress)
{
    return BrokerProgress(progress);
}

DLLFUNC int
BrokerOpen(char *Name,
    FARPROC fpError,
    FARPROC fpProgress)
{
    strcpy_s(Name, 10, "Dukascopy");
    (FARPROC&)BrokerError = fpError;
    (FARPROC&)BrokerProgress = fpProgress;

    return PLUGIN_VERSION;
}

DLLFUNC void
BrokerHTTP(FARPROC fpSend,
    FARPROC fpStatus,
    FARPROC fpResult,
    FARPROC fpFree)
{
    //BrokerError("BrokerHTTP not yet supported!");
}

DLLFUNC int
BrokerLogin(const char *User,
    const char *Pwd,
    const char *Type,
    char *Accounts)
{
    if (User)
        return dllCallHandler.BrokerLogin(User,
            Pwd,
            Type,
            Accounts);
    return dllCallHandler.BrokerLogout();
}

DLLFUNC int
BrokerTime(DATE *pTimeUTC)
{
    return dllCallHandler.BrokerTime(pTimeUTC);
}

DLLFUNC int
BrokerAsset(char* Asset,
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
    if (!pPrice)
        return dllCallHandler.SubscribeAsset(Asset);
    else
        return dllCallHandler.BrokerAsset(Asset,
            pPrice,
            pSpread,
            pVolume,
            pPip,
            pPipCost,
            pLotAmount,
            pMarginCost,
            pRollLong,
            pRollShort);
}

DLLFUNC int
BrokerHistory2(char* Asset,
    DATE tStart,
    DATE tEnd,
    int nTickMinutes,
    int nTicks,
    T6* ticks)
{
    return dllCallHandler.BrokerHistory2(Asset,
        tStart,
        tEnd,
        nTickMinutes,
        nTicks,
        ticks);
}

DLLFUNC int
BrokerAccount(char* Account,
    double *pBalance,
    double *pTradeVal,
    double *pMarginVal)
{
    return dllCallHandler.BrokerAccount(Account,
        pBalance,
        pTradeVal,
        pMarginVal);
}

DLLFUNC int
BrokerBuy2(char* Asset,
    int nAmount,
    double dStopDist,
    double limit,
    double *pPrice,
    double *pFill)
{
    return dllCallHandler.BrokerBuy2(Asset,
        nAmount,
        dStopDist,
        limit,
        pPrice,
        pFill);
}

DLLFUNC int
BrokerTrade(int nTradeID,
    double *pOpen,
    double *pClose,
    double *pRoll,
    double *pProfit)

{
    return dllCallHandler.BrokerTrade(nTradeID,
        pOpen,
        pClose,
        pRoll,
        pProfit);
}

DLLFUNC int
BrokerStop(int nTradeID,
    double dStop)
{
    return dllCallHandler.BrokerStop(nTradeID, dStop);
}

DLLFUNC int
BrokerSell(int nTradeID,
    int nAmount)
{
    return dllCallHandler.BrokerSell(nTradeID, nAmount);
}

DLLFUNC var
BrokerCommand(int nCommand,
    DWORD dwParameter)
{
    jmethodID methodID;
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
    case SET_ORDERTEXT: methodID = JData::bcSetOrderText.methodID;
    case GET_DIGITS: methodID = JData::bcGetDigits.methodID;
    case GET_MAXLOT: methodID = JData::bcGetMaxLot.methodID;
    case GET_MINLOT: methodID = JData::bcGetMinLot.methodID;
    case GET_MARGININIT: methodID = JData::bcGetMarginInit.methodID;
    case GET_TRADEALLOWED: methodID = JData::bcGetTradeAllowed.methodID;
    {
        char* text = reinterpret_cast<char*>(dwParameter);
        return dllCallHandler.bcForText(text, methodID);
    }
    case GET_TIME: methodID = JData::bcGetTime.methodID;
    case GET_MAXTICKS: methodID = JData::bcGetMaxTicks.methodID;
    case GET_SERVERSTATE: methodID = JData::bcGetServerState.methodID;
    {
        return dllCallHandler.bcNoParam(methodID);
    }
    case GET_ACCOUNT: methodID = JData::bcGetAccount.methodID;
    {
        char* stringToWrite = reinterpret_cast<char*>(dwParameter);
        return dllCallHandler.bcForGetString(stringToWrite, methodID);
    }
    case SET_SLIPPAGE: methodID = JData::bcSetSlippage.methodID;
    case SET_LIMIT: methodID = JData::bcSetLimit.methodID;
    {
        double* pValue = reinterpret_cast<double*>(dwParameter);
        return dllCallHandler.bcForDouble(*pValue, methodID);
    }
    default:
    {
        return 0.0;
    }
    }
}
