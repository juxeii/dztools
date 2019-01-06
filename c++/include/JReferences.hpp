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
extern JMethodDesc brokerCommand;
extern JMethodDesc bcSetOrderText;

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
