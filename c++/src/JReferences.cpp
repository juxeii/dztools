#include "JReferences.hpp"
#include "PatchValue.hpp"

namespace JData
{

    jobject JDukaZorroBridgeObject;

    jclass JDukaZorroBridgeClass;
    jclass JDukaZorroNativesClass;
    jclass ExceptionClass;

    JMethodDesc constructor = { nullptr, "<init>","()V" };
    JMethodDesc brokerLogin = { nullptr, "brokerLogin","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/jforex/dzjforex/login/BrokerLoginData;" };
    JMethodDesc brokerLogout = { nullptr, "brokerLogout","()I" };
    JMethodDesc brokerTime = { nullptr, "brokerTime","()Lcom/jforex/dzjforex/time/BrokerTimeData;" };
    JMethodDesc brokerSubscribeAsset = { nullptr, "brokerSubscribeAsset", "(Ljava/lang/String;)I" };
    JMethodDesc brokerAsset = { nullptr, "brokerAsset","(Ljava/lang/String;)Lcom/jforex/dzjforex/asset/BrokerAssetData;" };
    JMethodDesc brokerAccount = { nullptr, "brokerAccount","()Lcom/jforex/dzjforex/account/BrokerAccountData;" };
    JMethodDesc brokerBuy2 = { nullptr, "brokerBuy2","(Ljava/lang/String;IDD)Lcom/jforex/dzjforex/buy/BrokerBuyData;" };
    JMethodDesc brokerTrade = { nullptr, "brokerTrade","(I)Lcom/jforex/dzjforex/trade/BrokerTradeData;" };
    JMethodDesc brokerStop = { nullptr, "brokerStop","(ID)I" };
    JMethodDesc brokerSell = { nullptr, "brokerSell","(II)I" };
    JMethodDesc brokerHistory2 = { nullptr, "brokerHistory2", "(Ljava/lang/String;DDII)Lcom/jforex/dzjforex/history/BrokerHistoryData;" };
    JMethodDesc bcSetOrderText = { nullptr, "bcSetOrderText","(Ljava/lang/String;)D" };
    JMethodDesc bcSetSlippage = { nullptr, "bcSetSlippage","(D)D" };
    JMethodDesc bcSetLimit = { nullptr, "bcSetLimit","(D)D" };
    JMethodDesc bcGetAccount = { nullptr, "bcGetAccount","()Lcom/jforex/dzjforex/command/BrokerCommandData;" };
    JMethodDesc bcGetDigits = { nullptr, "bcGetDigits","(Ljava/lang/String;)D" };
    JMethodDesc bcGetMaxLot = { nullptr, "bcGetMaxLot","(Ljava/lang/String;)D" };
    JMethodDesc bcGetMinLot = { nullptr, "bcGetMinLot","(Ljava/lang/String;)D" };
    JMethodDesc bcGetMarginInit = { nullptr, "bcGetMarginInit","(Ljava/lang/String;)D" };
    JMethodDesc bcGetTradeAllowed = { nullptr, "bcGetTradeAllowed","(Ljava/lang/String;)D" };
    JMethodDesc bcGetTime = { nullptr, "bcGetTime","()D" };
    JMethodDesc bcGetMaxTicks = { nullptr, "bcGetMaxTicks","()D" };
    JMethodDesc bcGetServerState = { nullptr, "bcGetServerState","()D" };

    JMethodDesc excGetMessage = { nullptr, "getMessage","()Ljava/lang/String;" };
    JMethodDesc excGetName = { nullptr, "getName","()Ljava/lang/String;" };

    const JNINativeMethod nativesTable[2]{ { (char*)"jcallback_BrokerError",    (char*)"(Ljava/lang/String;)I", (void *)&jcallback_BrokerError },
                                            { (char*)"jcallback_BrokerProgress", (char*)"(I)I",                  (void *)&jcallback_BrokerProgress } };

    const char* JVMClassPathOption = "-Djava.class.path=Plugin/dukascopy/dzjforex-0.9.6.jar";
    const char* DukaZorroBridgePath = "com/jforex/dzjforex/zorro/ZorroBridge";
    const char* ZorroNativesPath = "com/jforex/dzjforex/zorro/ZorroNatives";
    const char* ExcPath = "java/lang/Class";

    const std::vector<JMethodDesc*> dukaZorroBridgeMethods = {
    &constructor,
    &brokerLogin,
    &brokerLogout,
    &brokerTime,
    &brokerSubscribeAsset,
    &brokerAsset,
    &brokerAccount,
    &brokerBuy2,
    &brokerTrade,
    &brokerStop,
    &brokerSell,
    &brokerHistory2,
    &bcSetOrderText,
    &bcSetSlippage,
    &bcSetLimit,
    &bcGetAccount,
    &bcGetDigits,
    &bcGetMaxLot,
    &bcGetMinLot,
    &bcGetMarginInit,
    &bcGetTradeAllowed,
    &bcGetTime,
    &bcGetMaxTicks,
    &bcGetServerState };

    const int nativesTableSize = sizeof(nativesTable) / sizeof(nativesTable[0]);

    const int JNI_VERSION = JNI_VERSION_1_8;

} /* namespace JData */

