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
extern JMethodDesc doLogin;
extern JMethodDesc doLogout;
extern JMethodDesc doBrokerTime;
extern JMethodDesc doSubscribeAsset;
extern JMethodDesc doBrokerAsset;
extern JMethodDesc doBrokerAccount;
extern JMethodDesc doBrokerBuy2;
extern JMethodDesc doBrokerTrade;
extern JMethodDesc doBrokerStop;
extern JMethodDesc doBrokerSell;
extern JMethodDesc doBrokerHistory2;
extern JMethodDesc doBrokerCommand;

extern JMethodDesc excGetMessage;
extern JMethodDesc excGetName;

extern const JNINativeMethod nativesTable[2];
extern const int nativesTableSize;

extern const char* JVMClassPathOption;
extern const char* DukaZorroBridgePath;
extern const char* ZorroNativesPath;
extern const char* ExcPath;

extern const std::vector<JMethodDesc*> dukaZorroBridgeMethods;

extern const int JNI_VERSION;

} /* namespace JData */

#endif /* JREFERENCES_HPP */
