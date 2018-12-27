#ifndef DUKAZORROBRIDGE_HPP
#define DUKAZORROBRIDGE_HPP

#include <windows.h>
#include <jni.h>
#include "trading.h"

#define GET_ACCOUNT 54
#define SET_ORDERTEXT 131 // Order comment for trades
#define SET_LIMIT 135 // set limit price for entry limit orders

typedef double DATE;
typedef jlong DukaTime;

static const int PLUGIN_VERSION = 2u;

extern int
(__cdecl *BrokerError)(const char *message);
extern int
(__cdecl *BrokerProgress)(const int percent);

var
brokerCommand(int command,
              DWORD parameter);

extern int
jcallback_BrokerError(JNIEnv *env,
                      jclass clazz,
                      jstring msg);
extern int
jcallback_BrokerProgress(JNIEnv *env,
                         jclass clazz,
                         jint progress);

#endif /* DUKAZORROBRIDGE_HPP */
