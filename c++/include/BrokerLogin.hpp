#pragma once
#include <jni.h>

class JNIHandler;

class BrokerLogin
{
public:
    BrokerLogin(JNIHandler&);

    int runLogin(const char*, const char*, const char*, char*);

    int runLogout();

private:
    JNIHandler& jniHandler;
    JNIEnv *env;
    jmethodID loginId;
    jmethodID logoutId;
};