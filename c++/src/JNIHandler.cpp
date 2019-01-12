#include <assert.h>
#include <vector>
#include "JNIHandler.hpp"
#include "JReferences.hpp"
#include "PropertiesParser.h"
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

    options[0].optionString = (char*)JData::JVMClassPathOption;
    options[1].optionString = (char*)maxHeapSizeString.c_str();

    args.version = JData::JNI_VERSION;
    args.options = options;
    args.nOptions = noOfOptions;
    args.ignoreUnrecognized = JNI_TRUE;

    jint res = JNI_CreateJavaVM(&jvm, (void **)&env, &args);
    assert(res == JNI_OK);
    isJVMLoaded = true;
}

JNIEnv* JNIHandler::getJNIEnvironment() {
    return env;
}

JNIEnv* JNIHandler::getEnvForCurrentThread()
{
    int envStat = jvm->GetEnv((void **)&env, JData::JNI_VERSION);
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
    JData::JDukaZorroBridgeClass = env->FindClass(JData::DukaZorroBridgePath);
    registerClassMethods();
    JData::JDukaZorroBridgeObject = env->NewObject(JData::JDukaZorroBridgeClass, JData::constructor.methodID);
    checkJNIExcpetion(env);
}

void JNIHandler::initExceptionHandling()
{
    JData::ExceptionClass = env->FindClass(JData::ExcPath);
    JData::excGetName.methodID = env->GetMethodID(JData::ExceptionClass, JData::excGetName.name, JData::excGetName.signature);
}

void JNIHandler::registerNatives()
{
    JData::JDukaZorroNativesClass = env->FindClass(JData::ZorroNativesPath);
    env->RegisterNatives(JData::JDukaZorroNativesClass, JData::nativesTable, JData::nativesTableSize);
    checkJNIExcpetion(env);
}

void JNIHandler::registerClassMethods()
{
    for (auto *desc : JData::dukaZorroBridgeMethods)
    {
        desc->methodID = env->GetMethodID(JData::JDukaZorroBridgeClass, desc->name, desc->signature);
        checkJNIExcpetion(env);
    }
}

void JNIHandler::checkJNIExcpetion(JNIEnv* env)
{
    jthrowable exc = env->ExceptionOccurred();
    if (!exc) return;

    jclass exccls(env->GetObjectClass(exc));
    jstring name = static_cast<jstring>(env->CallObjectMethod(exccls, JData::excGetName.methodID));
    char const* utfName(env->GetStringUTFChars(name, 0));

    JData::excGetMessage.methodID = env->GetMethodID(exccls, JData::excGetMessage.name, JData::excGetMessage.signature);
    jstring message = static_cast<jstring>(env->CallObjectMethod(exc, JData::excGetMessage.methodID));
    char const* utfMessage(env->GetStringUTFChars(message, 0));

    BrokerError(utfName);
    BrokerError(utfMessage);

    env->ReleaseStringUTFChars(message, utfMessage);
    env->ReleaseStringUTFChars(name, utfName);
    env->ExceptionClear();
}
