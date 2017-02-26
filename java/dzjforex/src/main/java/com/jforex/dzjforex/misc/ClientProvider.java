package com.jforex.dzjforex.misc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.jforex.dzjforex.Zorro;

public class ClientProvider {

    private final static Logger logger = LogManager.getLogger(ClientProvider.class);

    public static IClient get() {
        try {
            return ClientFactory.getDefaultInstance();
        } catch (final ClassNotFoundException e) {
            logger.error("IClient ClassNotFoundException occured! " + e.getMessage());
        } catch (final IllegalAccessException e) {
            logger.error("IClient IllegalAccessException occured!" + e.getMessage());
        } catch (final InstantiationException e) {
            logger.error("IClient InstantiationException occured!" + e.getMessage());
        }
        Zorro.indicateError();
        return null;
    }
}
