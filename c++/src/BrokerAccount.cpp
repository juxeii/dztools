#pragma once
#include "BrokerAccount.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerAccount::BrokerAccount(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    accountId = jniHandler.registerMethod("brokerAccount", "()Lcom/jforex/dzjforex/account/BrokerAccountData;");
}

int BrokerAccount::run(const char *Account,
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

    jobject brokerAccountObject = jniHandler.callObjectBridgeMethod(accountId);
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