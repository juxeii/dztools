#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerStop
{
public:
    BrokerStop(JNIHandler&);

    int run(const int, const double);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID stopId;
};