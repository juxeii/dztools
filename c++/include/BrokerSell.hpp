#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerSell
{
public:
    BrokerSell(JNIHandler&);

    int run(const int, const int);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID sellId;
};