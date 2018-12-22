#ifndef DLLCALLHANDLER_HPP
#define DLLCALLHANDLER_HPP

#include "dukazorrobridge.hpp"
#include "JNIHandler.hpp"

class DllCallHandler
{

public:

    int
    BrokerLogin(const char *User,
                const char *Pwd,
                const char *Type,
                char *Account);

    int
    BrokerLogout();

    int
    BrokerTime(DATE *pTimeUTC);

    int
    SubscribeAsset(const char* Asset);

    int
    BrokerAsset(char* Asset,
                double *pPrice,
                double *pSpread,
                double *pVolume,
                double *pPip,
                double *pPipCost,
                double *pLotAmount,
                double *pMarginCost,
                double *pRollLong,
                double *pRollShort);

    int
    BrokerHistory2(const char *Asset,
                   const DATE tStart,
                   const DATE tEnd,
                   const int nTickMinutes,
                   const int nTicks,
                   T6 *ticks);

    int
    BrokerAccount(const char *Account,
                  double *pBalance,
                  double *pTradeVal,
                  double *pMarginVal);

    int
    BrokerBuy2(char* Asset,
        int nAmount,
        double dStopDist,
        double limit,
        double *pPrice,
        double *pFill);

    int
    BrokerTrade(const int nTradeID,
                double *pOpen,
                double *pClose,
                double *pRoll,
                double *pProfit);

    int
    BrokerStop(const int nTradeID,
               const double dStop);

    int
    BrokerSell(const int nTradeID,
               const int nAmount);

    int
    SetOrderText(const char *orderText);

    int
    SetLimitPrice(const double limitPrice);

private:
    JNIHandler jniHandler;
    JNIEnv *env;
};

#endif /* DLLCALLHANDLER_HPP */
