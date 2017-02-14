package com.jforex.dzjforex;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.programming.client.ClientUtil;
import com.jforex.programming.connection.Authentification;
import com.jforex.programming.connection.LoginCredentials;

public class ZorroBridge {
    
    private IClient client;
    private ClientUtil clientUtil;
    private Authentification authentification;
    private final PluginConfig pluginConfig = ConfigFactory.create(PluginConfig.class);
    
    private final static Logger logger = LogManager.getLogger(ZorroBridge.class);
    
    public ZorroBridge(){
        initClientInstance();
        
        clientUtil = new ClientUtil(client, pluginConfig.CACHE_DIR());
        authentification = clientUtil.authentification();
    }
    
    private void initClientInstance() {
        try {
            client = ClientFactory.getDefaultInstance();
            logger.debug("IClient successfully initialized.");
            return;
        } catch (ClassNotFoundException e) {
            logger.error("IClient ClassNotFoundException occured! " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("IClient IllegalAccessException occured!" + e.getMessage());
        } catch (InstantiationException e) {
            logger.error("IClient InstantiationException occured!" + e.getMessage());
        }
        ZorroLogger.indicateError();
    }

    public int doLogin(String userName,
                       String password,
                       String type,
                       String accountInfos[]) {
        LoginCredentials credentials = new LoginCredentials(pluginConfig.CONNECT_URL_DEMO(), 
                                                            userName, 
                                                            password);
        authentification
            .login(credentials)
            .blockingAwait();
        
        return client.isConnected()? 1: 0;
    }
}
