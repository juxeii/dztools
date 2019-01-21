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
    
    jobject callObjectBridgeMethod(jmethodID, ...);

    jdouble callDoubleBridgeMethod(jmethodID, ...);

    jint callIntBridgeMethod(jmethodID, ...);

    jmethodID registerMethod(const char*, const char*);

    bool isInitialized() const;

private:

    void initializeJVM();

    void initializeJavaReferences();

    void initBridgeObject();

    void initExceptionHandling();

    void registerNatives();

    void checkJNIExcpetion(JNIEnv* env);

    jobject zorroBridgeObject;
    jclass zorroBridgeClass;
    jclass zorroNativesClass;
    jclass exceptionClass;

    const JNINativeMethod nativesTable[3]{
    { (char*)"jcallback_BrokerError", (char*)"(Ljava/lang/String;)I", (void *)&jcallback_BrokerError },
    { (char*)"jcallback_BrokerProgress", (char*)"(I)I", (void *)&jcallback_BrokerProgress },
    { (char*)"triggerQuoteReq", (char*)"()V", (void *)&triggerQuoteReq } };
    const int nativesTableSize = sizeof(nativesTable) / sizeof(nativesTable[0]);
    const char* JVMClassPathOption = "-Djava.class.path=Plugin/dukascopy/dzjforex-0.9.71.jar";
    const char* zorroBridgePath = "com/jforex/dzjforex/zorro/ZorroBridge";
    const char* zorroNativesPath = "com/jforex/dzjforex/zorro/ZorroNatives";
    const char* excPath = "java/lang/Class";
    const int JNI_VERSION = JNI_VERSION_1_8;
    jmethodID constructor;
    jmethodID excGetName;

    JavaVM *jvm = nullptr;
    JNIEnv * env = nullptr;

    bool isJVMLoaded = false;
    bool areJavaReferencesInitialized = false;
};

#endif /* JNIHANDLER_HPP */
