#pragma once
#include "BrokerLogin.hpp"
#include "JNIHandler.hpp"
#include "ZorroDto.hpp"

BrokerLogin::BrokerLogin(JNIHandler& jniHandler)
    : jniHandler(jniHandler)
{
    env = jniHandler.getJNIEnvironment();
    loginId = jniHandler.registerMethod("brokerLogin", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/jforex/dzjforex/login/BrokerLoginData;");
    logoutId = jniHandler.registerMethod("brokerLogout", "()I");
}

int BrokerLogin::runLogin(const char *username,
    const char *password,
    const char *type,
    char *account)
{
    jniHandler.init();

    env = jniHandler.getJNIEnvironment();

    constexpr int LOGIN_OK = 1;
    constexpr int ACCOUNT_LENGTH = 1024;
    jstring jUser;
    jstring jPwd;
    jstring jType;
    if (username)
        jUser = env->NewStringUTF(username);
    else
        jUser = env->NewStringUTF("");

    if (password)
        jPwd = env->NewStringUTF(password);
    else
        jPwd = env->NewStringUTF("");

    if (type)
        jType = env->NewStringUTF(type);
    else
        jType = env->NewStringUTF("");

    jobject brokerLoginObject = jniHandler.callBridgeMethod(loginId, jUser, jPwd, jType);
    ZorroDto loginDto(env, brokerLoginObject);
    int returnCode = loginDto.getReturnCode();

    if (returnCode == LOGIN_OK && account) {
        std::string accountstr = loginDto.getString("accountName");
        std::size_t accountLength = accountstr.size();
        if (accountLength > ACCOUNT_LENGTH - 1)
        {
            char str[80];
            sprintf_s(str, "Account names too long -> truncated! Length %i", accountLength);
            puts(str);
            BrokerError(str);
        }
        strncpy_s(account, ACCOUNT_LENGTH, accountstr.c_str(), ACCOUNT_LENGTH - 1);
    }

    env->DeleteLocalRef(jUser);
    env->DeleteLocalRef(jPwd);
    env->DeleteLocalRef(jType);
    env->DeleteLocalRef(brokerLoginObject);

    return returnCode;
}

int BrokerLogin::runLogout()
{
    return (jint)jniHandler.callBridgeMethod(logoutId);
}