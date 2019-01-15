#pragma once
#include <jni.h>

class JNIHandler;

class BrokerSubscribe
{
public:
    BrokerSubscribe(JNIHandler&);

    int run(const char*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID subscribeId;
};