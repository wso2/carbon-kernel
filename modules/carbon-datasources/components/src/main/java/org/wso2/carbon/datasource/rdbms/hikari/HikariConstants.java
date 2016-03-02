package org.wso2.carbon.datasource.rdbms.hikari;

/**
 * Class holding the default values for Hikari configuration.
 */
public class HikariConstants {

    /**
     * Private constructor
     */
    private HikariConstants() {
    }

    public static long CONNECTION_TIME_OUT = 30000;
    public static long IDLE_TIME_OUT = 600000;
    public static long MAX_LIFE_TIME = 1800000;
    public static int MAXIMUM_POOL_SIZE = 10;

}
