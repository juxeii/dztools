#ifndef JNIHANDLER_HPP
#define JNIHANDLER_HPP

#include "dukazorrobridge.hpp"

class JNIHandler
{
public:
    void init();

    JNIEnv* getJNIEnvironment();

    JNIEnv* getEnvForCurrentThread();

    void checkJNIExcpetion(JNIEnv* env);

    JavaVM* getJVM() { return jvm; }

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
