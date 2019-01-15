#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerTrade
{
public:
    BrokerTrade(JNIHandler&);

    int run(const int, double*, double*, double*, double*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID tradeId;
};