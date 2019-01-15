#pragma once
#include <jni.h>

typedef double DATE;
class JNIHandler;

class BrokerAsset
{
public:
    BrokerAsset(JNIHandler&);

    int run(char*, double*, double*, double*, double*, double*, double*, double*, double*, double*);

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID assetId;
};