#pragma once
#include "ZorroDto.hpp"

ZorroDto::ZorroDto(JNIEnv *env,
    const jobject& pluginObject)
    :env(env),
    pluginObject(pluginObject)
{
    pluginClass = env->GetObjectClass(pluginObject);
}

int ZorroDto::getReturnCode() {
    return getInt("returnCode");
}

double ZorroDto::getDouble(char* fieldId, bool printValue) {
    jfieldID doubleId = env->GetFieldID(pluginClass, fieldId, "D");
    jdouble jvalue = env->GetDoubleField(pluginObject, doubleId);

    if (printValue) {
        char str[80];
        sprintf_s(str, "Value of %s is %f", fieldId, (double)jvalue);
        puts(str);
        BrokerError(str);
    }

    return (double)jvalue;
}

int ZorroDto::getInt(char* fieldId, bool printValue) {
    jfieldID intId = env->GetFieldID(pluginClass, fieldId, "I");
    jint jvalue = env->GetIntField(pluginObject, intId);

    if (printValue) {
        char str[80];
        sprintf_s(str, "Value of %s is %i", fieldId, (int)jvalue);
        puts(str);
        BrokerError(str);
    }

    return (int)jvalue;
}
