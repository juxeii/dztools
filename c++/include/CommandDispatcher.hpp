#pragma once
#define DLLFUNC extern "C" __declspec(dllexport)

#include <memory>
#include "dukazorrobridge.hpp"
#include "JNIHandler.hpp"
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
#include "BrokerCommandHandler.hpp"

class CommandDispatcher {

public:
    CommandDispatcher();

    int brokerLogin(const char*, const char*, const char*, char*);

    int brokerLogout();

    int brokerSubscribe(const char*);

    int brokerTime(DATE*);

    int brokerAccount(const char*, double*, double*, double*);

    int brokerBuy(char*, int, double, double, double*, double*);

    int brokerTrade(const int, double*, double*, double*, double*);

    int brokerSell(const int, const int);

    int brokerStop(const int, const double);

    int brokerAsset(char*, double*, double*, double*, double*, double*, double*, double*, double*, double*);

    int brokerHistory(const char*, const DATE, const DATE, const int, const int, T6*);

    var brokerCommand(int, DWORD);

    HWND CommandDispatcher::getZorroHWND();
private:
    JNIHandler jniHandler;
    std::shared_ptr<BrokerLogin> brokerLoginHandler;
    std::shared_ptr<BrokerSubscribe> brokerSusbcribeHandler;
    std::shared_ptr < BrokerTime> brokerTimeHandler;
    std::shared_ptr < BrokerAsset> brokerAssetHandler;
    std::shared_ptr < BrokerTrade> brokerTradeHandler;
    std::shared_ptr < BrokerAccount> brokerAccountHandler;
    std::shared_ptr < BrokerStop> brokerStopHandler;
    std::shared_ptr < BrokerSell> brokerSellHandler;
    std::shared_ptr < BrokerBuy> brokerBuyHandler;
    std::shared_ptr < BrokerHistory> brokerHistoryHandler;
    std::shared_ptr < BrokerCommandHandler> brokerCommandHandler;
};