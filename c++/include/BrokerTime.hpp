#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerTime
{
public:
    BrokerTime(JNIHandler&);

    int run(DATE*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID timeId;
};