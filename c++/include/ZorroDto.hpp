#pragma once
#include <iostream>
#include <sstream>
#include <jni.h>
#include "dukazorrobridge.hpp"

class ZorroDto {
public:
    ZorroDto(JNIEnv *env, const jobject& pluginObject);

    int getReturnCode();

    double getDouble(char* fieldId, bool printValue = false);

    int getInt(char* fieldId, bool printValue = false);

private:
    JNIEnv *env;
    const jobject& pluginObject;
    jclass pluginClass;
};
