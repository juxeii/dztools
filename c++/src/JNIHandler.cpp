#include <assert.h>
#include <vector>
#include "JNIHandler.hpp"
#include "PropertiesParser.h"
#include <stdarg.h>
using namespace cppproperties;

void JNIHandler::init()
{
    initializeJVM();
    initializeJavaReferences();
}

void JNIHandler::initializeJVM()
{
    if (isJVMLoaded) return;

    JavaVMInitArgs args;
    constexpr int noOfOptions = 2;
    JavaVMOption options[noOfOptions];

    Properties props = PropertiesParser::Read("Plugin/dukascopy/Plugin.properties");
    auto names = props.GetPropertyNames();
    auto heapSize = props.GetProperty("plugin.maxheapsize");
    auto maxHeapSizeString = "-Xmx" + heapSize + "m";

    options[0].optionString = (char*)JVMClassPathOption;
    options[1].optionString = (char*)maxHeapSizeString.c_str();

    args.version = JNI_VERSION;
    args.options = options;
    args.nOptions = noOfOptions;
    args.ignoreUnrecognized = JNI_TRUE;

    jint res = JNI_CreateJavaVM(&jvm, (void **)&env, &args);
    assert(res == JNI_OK);
    isJVMLoaded = true;
}

JNIEnv* JNIHandler::getJNIEnvironment() const {
    return env;
}

JNIEnv* JNIHandler::getEnvForCurrentThread()
{
    int envStat = jvm->GetEnv((void **)&env, JNI_VERSION);
    if (envStat == JNI_EDETACHED) {
        jint res = jvm->AttachCurrentThread((void**)&env, NULL);
        assert(res == JNI_OK);
    }
    return env;
}

void JNIHandler::initializeJavaReferences()
{
    if (areJavaReferencesInitialized) return;

    initExceptionHandling();
    initBridgeObject();
    registerNatives();

    areJavaReferencesInitialized = true;
}

void JNIHandler::initBridgeObject()
{
    zorroBridgeClass = env->FindClass(zorroBridgePath);
    constructor = registerMethod("<init>", "()V");
    zorroBridgeObject = env->NewObject(zorroBridgeClass, constructor);
    checkJNIExcpetion(env);
}

void JNIHandler::initExceptionHandling()
{
    exceptionClass = env->FindClass(excPath);
    excGetName = env->GetMethodID(exceptionClass, "getName", "()Ljava/lang/String;");
}

void JNIHandler::registerNatives()
{
    zorroNativesClass = env->FindClass(zorroNativesPath);
    env->RegisterNatives(zorroNativesClass, nativesTable, nativesTableSize);
    checkJNIExcpetion(env);
}

jmethodID JNIHandler::registerMethod(const char* name, const char* signature)
{
    jmethodID methodID = env->GetMethodID(zorroBridgeClass, name, signature);
    checkJNIExcpetion(env);
    return methodID;
}

void JNIHandler::checkJNIExcpetion(JNIEnv* env)
{
    jthrowable exc = env->ExceptionOccurred();
    if (!exc) return;

    jclass exccls(env->GetObjectClass(exc));
    jstring name = static_cast<jstring>(env->CallObjectMethod(exccls, excGetName));
    char const* utfName(env->GetStringUTFChars(name, 0));

    jmethodID excGetMessageId = env->GetMethodID(exccls, "getMessage", "()Ljava/lang/String;");
    jstring message = static_cast<jstring>(env->CallObjectMethod(exc, excGetMessageId));
    char const* utfMessage(env->GetStringUTFChars(message, 0));

    BrokerError(utfName);
    BrokerError(utfMessage);

    env->ReleaseStringUTFChars(message, utfMessage);
    env->ReleaseStringUTFChars(name, utfName);
    env->ExceptionClear();
}

jobject JNIHandler::callBridgeMethod(jmethodID methodId, ...)
{
    va_list args;
    jobject result;
    va_start(args, methodId);
    result = env->CallObjectMethodV(zorroBridgeObject, methodId, args);
    va_end(args);
    return result;
}

jdouble JNIHandler::callDoubleBridgeMethod(jmethodID methodId, ...)
{
    va_list args;
    jdouble result;
    va_start(args, methodId);
    result = env->CallDoubleMethodV(zorroBridgeObject, methodId, args);
    va_end(args);
    return result;
}
