#pragma once
#include "CommandDispatcher.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

CommandDispatcher::CommandDispatcher()
{
    brokerLoginHandler = std::make_shared<BrokerLogin>(jniHandler);
}

int CommandDispatcher::brokerLogin(const char *username,
    const char *password,
    const char *type,
    char *account)
{
    int returnCode = brokerLoginHandler->runLogin(username, password, type, account);

    brokerLoginHandler = std::make_shared<BrokerLogin>(jniHandler);
    brokerSusbcribeHandler = std::make_shared<BrokerSubscribe>(jniHandler);
    brokerTimeHandler = std::make_shared<BrokerTime>(jniHandler);
    brokerAssetHandler = std::make_shared<BrokerAsset>(jniHandler);
    brokerTradeHandler = std::make_shared<BrokerTrade>(jniHandler);
    brokerAccountHandler = std::make_shared<BrokerAccount>(jniHandler);
    brokerStopHandler = std::make_shared<BrokerStop>(jniHandler);
    brokerSellHandler = std::make_shared<BrokerSell>(jniHandler);
    brokerBuyHandler = std::make_shared<BrokerBuy>(jniHandler);
    brokerHistoryHandler = std::make_shared<BrokerHistory>(jniHandler);
    brokerCommandHandler = std::make_shared<BrokerCommandHandler>(jniHandler);

    return returnCode;
}

int CommandDispatcher::brokerLogout()
{
    return brokerLoginHandler->runLogout();
}

int CommandDispatcher::brokerSubscribe(const char* assetName)
{
    return brokerSusbcribeHandler->run(assetName);
}

int CommandDispatcher::brokerTime(DATE *pTimeUTC)
{
    return brokerTimeHandler->run(pTimeUTC);
}

int CommandDispatcher::brokerAccount(const char *Account,
    double *pBalance,
    double *pTradeVal,
    double *pMarginVal)
{
    return brokerAccountHandler->run(Account, pBalance, pTradeVal, pMarginVal);
}

int CommandDispatcher::brokerBuy(char* assetName,
    int nAmount,
    double dStopDist,
    double limit,
    double *pPrice,
    double *pFill)
{
    return brokerBuyHandler->run(assetName, nAmount, dStopDist, limit, pPrice, pFill);
}

int CommandDispatcher::brokerTrade(const int nTradeID,
    double *pOpen,
    double *pClose,
    double *pRoll,
    double *pProfit)
{
    return brokerTradeHandler->run(nTradeID, pOpen, pClose, pRoll, pProfit);
}

int CommandDispatcher::brokerSell(const int nTradeID, const int nAmount)
{
    return brokerSellHandler->run(nTradeID, nAmount);
}

int CommandDispatcher::brokerStop(const int nTradeID, const double dStop)
{
    return brokerStopHandler->run(nTradeID, dStop);
}

int CommandDispatcher::brokerAsset(char* assetName,
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
    return brokerAssetHandler->run(assetName, pPrice, pSpread, pVolume, pPip, pPipCost, pLotAmount, pMarginCost, pRollLong, pRollShort);
}

int CommandDispatcher::brokerHistory(const char *assetName,
    const DATE tStart,
    const DATE tEnd,
    const int nTickMinutes,
    const int nTicks,
    T6 *ticks)
{
    return brokerHistoryHandler->run(assetName, tStart, tEnd, nTickMinutes, nTicks, ticks);
}

var CommandDispatcher::brokerCommand(int nCommand, DWORD dwParameter)
{
    return brokerCommandHandler->run(nCommand, dwParameter);
}

HWND CommandDispatcher::getZorroHWND()
{
    return brokerCommandHandler->getZorroHWND();
}
