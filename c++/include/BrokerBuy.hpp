#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerBuy
{
public:
    BrokerBuy(JNIHandler&);

    int run(char*, int, double, double, double*, double*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID buyId;
};