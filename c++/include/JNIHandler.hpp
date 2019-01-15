#ifndef JNIHANDLER_HPP
#define JNIHANDLER_HPP

#include "dukazorrobridge.hpp"
#include <vector>

class JNIHandler
{
public:
    void init();

    JNIEnv* getJNIEnvironment() const;

    JNIEnv* getEnvForCurrentThread();

    void checkJNIExcpetion(JNIEnv* env);

    JavaVM* getJVM() { return jvm; }

    jobject callBridgeMethod(jmethodID, ...);

    jmethodID getBcMethodId(int nCommand);

    jmethodID registerMethod(const char*, const char*);

    jobject zorroBridgeObject;
    jclass zorroBridgeClass;
    jclass zorroNativesClass;
    jclass exceptionClass;
    
    const JNINativeMethod nativesTable[3]{
    { (char*)"jcallback_BrokerError", (char*)"(Ljava/lang/String;)I", (void *)&jcallback_BrokerError },
    { (char*)"jcallback_BrokerProgress", (char*)"(I)I", (void *)&jcallback_BrokerProgress },
    { (char*)"triggerQuoteReq", (char*)"()V", (void *)&triggerQuoteReq } };
    const int nativesTableSize = sizeof(nativesTable) / sizeof(nativesTable[0]);
    const char* JVMClassPathOption = "-Djava.class.path=Plugin/dukascopy/dzjforex-0.9.7.jar";
    const char* zorroBridgePath = "com/jforex/dzjforex/zorro/ZorroBridge";
    const char* zorroNativesPath = "com/jforex/dzjforex/zorro/ZorroNatives";
    const char* excPath = "java/lang/Class";
    const int JNI_VERSION = JNI_VERSION_1_8;
    std::map<int, jmethodID> bcMethodIdMap;

    jmethodID constructor;
    jmethodID brokerLogin;
    jmethodID brokerLogout;
    jmethodID brokerTime;
    jmethodID brokerSubscribeAsset;
    jmethodID brokerAsset;
    jmethodID brokerAccount;
    jmethodID brokerBuy2;
    jmethodID brokerTrade;
    jmethodID brokerStop;
    jmethodID brokerSell;
    jmethodID brokerHistory2;
    jmethodID bcSetOrderText;
    jmethodID bcSetSlippage;
    jmethodID bcSetLimit;
    jmethodID bcGetAccount;
    jmethodID bcGetDigits;
    jmethodID bcGetMaxLot;
    jmethodID bcGetMinLot;
    jmethodID bcGetMarginInit;
    jmethodID bcGetTradeAllowed;
    jmethodID bcGetTime;
    jmethodID bcGetMaxTicks;
    jmethodID bcGetServerState;
    jmethodID excGetName;

private:

    void initializeJVM();

    void initializeJavaReferences();

    void initBridgeObject();

    void initExceptionHandling();

    void registerNatives();

    void registerClassMethods();

    

    JavaVM *jvm = nullptr;
    JNIEnv * env = nullptr;

    bool isJVMLoaded = false;
    bool areJavaReferencesInitialized = false;
};

#endif /* JNIHANDLER_HPP */
