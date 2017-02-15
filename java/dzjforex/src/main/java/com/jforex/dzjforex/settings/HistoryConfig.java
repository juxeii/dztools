package com.jforex.dzjforex.settings;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "classpath:HistoryConfig.properties" })
public interface HistoryConfig extends Config {
    String Asset();

    int StartYear();

    int EndYear();

    String Path();
}
