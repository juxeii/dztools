#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerAccount
{
public:
    BrokerAccount(JNIHandler&);

    int run(const char*, double* ,double* ,double*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID accountId;
};