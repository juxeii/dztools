#ifndef JREFERENCES_HPP
#define JREFERENCES_HPP

#include <vector>
#include "dukazorrobridge.hpp"

typedef struct JMethodDesc
{
    jmethodID methodID;
    const char *name;
    const char *signature;
} JMethodDesc;

namespace JData
{

    extern jobject JDukaZorroBridgeObject;

    extern jclass JDukaZorroBridgeClass;
    extern jclass JDukaZorroNativesClass;
    extern jclass ExceptionClass;

    extern JMethodDesc constructor;
    extern JMethodDesc brokerLogin;
    extern JMethodDesc brokerLogout;
    extern JMethodDesc brokerTime;
    extern JMethodDesc brokerSubscribeAsset;
    extern JMethodDesc brokerAsset;
    extern JMethodDesc brokerAccount;
    extern JMethodDesc brokerBuy2;
    extern JMethodDesc brokerTrade;
    extern JMethodDesc brokerStop;
    extern JMethodDesc brokerSell;
    extern JMethodDesc brokerHistory2;
    extern JMethodDesc bcSetOrderText;
    extern JMethodDesc bcSetSlippage;
    extern JMethodDesc bcSetLimit;
    extern JMethodDesc bcSetHWND;
    extern JMethodDesc bcGetAccount;
    extern JMethodDesc bcGetDigits;
    extern JMethodDesc bcGetMaxLot;
    extern JMethodDesc bcGetMinLot;
    extern JMethodDesc bcGetMarginInit;
    extern JMethodDesc bcGetTradeAllowed;
    extern JMethodDesc bcGetTime;
    extern JMethodDesc bcGetMaxTicks;
    extern JMethodDesc bcGetServerState;

    extern JMethodDesc excGetMessage;
    extern JMethodDesc excGetName;

    extern const JNINativeMethod nativesTable[3];
    extern const int nativesTableSize;

    extern const char* JVMClassPathOption;
    extern const char* DukaZorroBridgePath;
    extern const char* ZorroNativesPath;
    extern const char* ExcPath;

    extern const std::vector<JMethodDesc*> dukaZorroBridgeMethods;

    extern const int JNI_VERSION;

} /* namespace JData */

#endif /* JREFERENCES_HPP */
