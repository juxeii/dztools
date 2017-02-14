#include "dukazorrobridge.hpp"
#include "DllCallHandler.hpp"
#include <sstream>

#define DLLFUNC extern "C" __declspec(dllexport)

int
(__cdecl *BrokerError)(const char *txt) = nullptr;
int
(__cdecl *BrokerProgress)(const int percent) = nullptr;
const int HISTORY_DOWNLOAD = 666;
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
    strcpy(Name, "Dukascopy");
    (FARPROC&) BrokerError = fpError;
    (FARPROC&) BrokerProgress = fpProgress;

    return PLUGIN_VERSION;
}

DLLFUNC void
BrokerHTTP(FARPROC fpSend,
           FARPROC fpStatus,
           FARPROC fpResult,
           FARPROC fpFree)
{

}

DLLFUNC int
BrokerLogin(const char *User,
            const char *Pwd,
            const char *Type,
            char *Accounts)
{
    if (User)
        return dllCallHandler.BrokerLogin(User, Pwd, Type, Accounts);
    else
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
    return dllCallHandler.BrokerAccount(Account, pBalance, pTradeVal, pMarginVal);
}

DLLFUNC int
BrokerBuy(char* Asset,
          int nAmount,
          double dStopDist,
          double *pPrice)
{
    return dllCallHandler.BrokerBuy(Asset, nAmount, dStopDist, pPrice);
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

DLLFUNC double
BrokerCommand(int nCommand,
              DWORD dwParameter)
{
    return 0l;
}
