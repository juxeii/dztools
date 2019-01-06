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

float ZorroDto::getFloat(char* fieldId, bool printValue) {
    jfieldID floatId = env->GetFieldID(pluginClass, fieldId, "F");
    jfloat jvalue = env->GetFloatField(pluginObject, floatId);

    if (printValue) {
        char str[80];
        sprintf_s(str, "Value of %s is %f", fieldId, (float)jvalue);
        puts(str);
        BrokerError(str);
    }

    return (float)jvalue;
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

std::string ZorroDto::getString(char* fieldId,  bool printValue) {
    jfieldID stringId = env->GetFieldID(pluginClass, fieldId, "Ljava/lang/String;");
    jstring stringObj = (jstring)env->GetObjectField(pluginObject, stringId);
    const char *c_str = env->GetStringUTFChars(stringObj, NULL);

    if (printValue) {
        char str[80];
        sprintf_s(str, "Value of %s is %s", fieldId, c_str);
        puts(str);
        BrokerError(str);
    }
    std::string string(c_str);
    env->ReleaseStringUTFChars(stringObj, c_str);

    return string;
}
