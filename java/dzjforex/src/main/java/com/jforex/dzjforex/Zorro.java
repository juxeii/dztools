package com.jforex.dzjforex;

public class Zorro {

    public static native int jcallback_BrokerError(String errorMsg);

    public static native int jcallback_BrokerProgress(int progress);
}