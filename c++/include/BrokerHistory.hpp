#pragma once
#include <windows.h>
#include <jni.h>
#include "trading.h"

class JNIHandler;

class BrokerHistory
{
public:
    BrokerHistory(JNIHandler&);

    int run(const char*, const DATE, const DATE, const int, const int, T6*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID historyId;
};