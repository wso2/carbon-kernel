package org.wso2.carbon.core;

/**
 * This class is used as a listener for getting notifications when the server startup happens.
 */
public interface ServerStartupObserver {

    /**
     * This method will be invoked just before completing server startup.
     * E.g. before starting all the transports.
     */
    public void completingServerStartup();


    /**
     * This method will be invoked just after completing server startup.
     * E.g. after starting all the transports.
     */
    public void completedServerStartup();

}
