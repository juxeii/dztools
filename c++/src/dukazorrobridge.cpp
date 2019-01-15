#include "dukazorrobridge.hpp"
#include "DllCallHandler.hpp"
#include "BrokerSubscribe.hpp"
#include "BrokerLogin.hpp"
#include "BrokerTime.hpp"
#include "BrokerAsset.hpp"
#include "BrokerTrade.hpp"
#include "BrokerAccount.hpp"
#include "BrokerStop.hpp"
#include "BrokerSell.hpp"
#include "BrokerBuy.hpp"
#include "BrokerHistory.hpp"
#include <string>

#define DLLFUNC extern "C" __declspec(dllexport)

int
(__cdecl *BrokerError)(const char *txt) = nullptr;
int
(__cdecl *BrokerProgress)(const int percent) = nullptr;
static JNIHandler jniHandler;
static DllCallHandler dllCallHandler(jniHandler);
static BrokerSubscribe brokerSusbcribe(jniHandler);
static BrokerLogin brokerLogin(jniHandler);
static BrokerTime brokerTime(jniHandler);
static BrokerAsset brokerAsset(jniHandler);
static BrokerTrade brokerTrade(jniHandler);
static BrokerAccount brokerAccount(jniHandler);
static BrokerStop brokerStop(jniHandler);
static BrokerSell brokerSell(jniHandler);
static BrokerBuy brokerBuy(jniHandler);
static BrokerHistory brokerHistory(jniHandler);

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

void
triggerQuoteReq()
{
    if (zorroWindow) PostMessage(zorroWindow, WM_APP + 1, 0, 0);
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
    return User ? brokerLogin.runLogin(User, Pwd, Type, Accounts) : brokerLogin.runLogout();
}

DLLFUNC int
BrokerTime(DATE *pTimeUTC)
{
    return brokerTime.run(pTimeUTC);
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
    return pPrice? brokerAsset.run(Asset,
        pPrice,
        pSpread,
        pVolume,
        pPip,
        pPipCost,
        pLotAmount,
        pMarginCost,
        pRollLong,
        pRollShort)
        : brokerSusbcribe.run(Asset);
}

DLLFUNC int
BrokerHistory2(char* Asset,
    DATE tStart,
    DATE tEnd,
    int nTickMinutes,
    int nTicks,
    T6* ticks)
{
    return brokerHistory.run(Asset,
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
    return brokerAccount.run(Account,
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
    return brokerBuy.run(Asset,
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
    return brokerTrade.run(nTradeID,
        pOpen,
        pClose,
        pRoll,
        pProfit);
}

DLLFUNC int
BrokerStop(int nTradeID,
    double dStop)
{
    return brokerStop.run(nTradeID, dStop);
}

DLLFUNC int
BrokerSell(int nTradeID,
    int nAmount)
{
    return brokerSell.run(nTradeID, nAmount);
}

DLLFUNC var
BrokerCommand(int nCommand,
    DWORD dwParameter)
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
    {
        char* text = reinterpret_cast<char*>(dwParameter);
        return dllCallHandler.bcForText(text, nCommand);
    }
    case GET_TIME:
    case GET_MAXTICKS:
    case GET_SERVERSTATE:
    {
        return dllCallHandler.bcNoParam(nCommand);
    }
    case GET_ACCOUNT:
    {
        char* stringToWrite = reinterpret_cast<char*>(dwParameter);
        return dllCallHandler.bcForGetString(stringToWrite, nCommand);
    }
    case SET_HWND:
    {
        zorroWindow = (HWND)dwParameter;
        return 1;
    }
    case SET_SLIPPAGE:
    {
        double pValue = (double)dwParameter;
        return dllCallHandler.bcForDouble(pValue, nCommand);
    }
    case SET_LIMIT:
    {
        double* pValue = reinterpret_cast<double*>(dwParameter);
        return dllCallHandler.bcForDouble(*pValue, nCommand);
    }
    default:
    {
        return 0.0;
    }
    }
}
