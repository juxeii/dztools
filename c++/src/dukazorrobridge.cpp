#pragma once
#define DLLFUNC extern "C" __declspec(dllexport)

#include "dukazorrobridge.hpp"
#include "CommandDispatcher.hpp"
#include <string>

int
(__cdecl *BrokerError)(const char *txt) = nullptr;
int
(__cdecl *BrokerProgress)(const int percent) = nullptr;
static CommandDispatcher dispatcher;

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
    HWND hwnd = dispatcher.getZorroHWND();
    if (hwnd) PostMessage(hwnd, WM_APP + 1, 0, 0);
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
    return User ? dispatcher.brokerLogin(User, Pwd, Type, Accounts) : dispatcher.brokerLogout();
}

DLLFUNC int
BrokerTime(DATE *pTimeUTC)
{
    return dispatcher.brokerTime(pTimeUTC);
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
    return pPrice? dispatcher.brokerAsset(Asset,
        pPrice,
        pSpread,
        pVolume,
        pPip,
        pPipCost,
        pLotAmount,
        pMarginCost,
        pRollLong,
        pRollShort)
        : dispatcher.brokerSubscribe(Asset);
}

DLLFUNC int
BrokerHistory2(char* Asset,
    DATE tStart,
    DATE tEnd,
    int nTickMinutes,
    int nTicks,
    T6* ticks)
{
    return dispatcher.brokerHistory(Asset,
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
    return dispatcher.brokerAccount(Account,
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
    return dispatcher.brokerBuy(Asset,
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
    return dispatcher.brokerTrade(nTradeID,
        pOpen,
        pClose,
        pRoll,
        pProfit);
}

DLLFUNC int
BrokerStop(int nTradeID,
    double dStop)
{
    return dispatcher.brokerStop(nTradeID, dStop);
}

DLLFUNC int
BrokerSell(int nTradeID,
    int nAmount)
{
    return dispatcher.brokerSell(nTradeID, nAmount);
}

DLLFUNC var
BrokerCommand(int nCommand,DWORD dwParameter)
{
    return dispatcher.brokerCommand(nCommand, dwParameter);
}
