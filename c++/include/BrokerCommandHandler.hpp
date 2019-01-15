#pragma once
#include <windows.h>
#include <jni.h>
#include <map>
#include "trading.h"
#include "PatchValue.hpp"

class JNIHandler;

class BrokerCommandHandler
{
public:
    BrokerCommandHandler(JNIHandler&);

    var run(int, DWORD);

    HWND getZorroHWND();

private:
    bool isPatchValueActive(PatchValue);

    jmethodID getBcMethodId(int);

    var bcForText(char*, int);

    var bcForGetString(char*, int);

    var bcForDouble(double, int);

    var bcNoParam(int);

    JNIHandler& jniHandler;
    JNIEnv *env;
    std::map<int, jmethodID> bcMethodIdMap;
    jmethodID bcSetOrderText;
    jmethodID bcSetSlippage;
    jmethodID bcSetLimit;
    jmethodID bcGetAccount;
    jmethodID bcGetDigits;
    jmethodID bcGetMaxLot;
    jmethodID bcGetMinLot;
    jmethodID bcGetMarginInit;
    jmethodID bcGetTradeAllowed;
    jmethodID bcGetTime;
    jmethodID bcGetMaxTicks;
    jmethodID bcGetServerState;

    int bcPatch = 0;
    HWND zorroWindow{};
};