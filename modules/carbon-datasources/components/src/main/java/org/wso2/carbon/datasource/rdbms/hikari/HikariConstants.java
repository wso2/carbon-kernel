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

    public static final long CONNECTION_TIME_OUT = 30000;

    public static final long IDLE_TIME_OUT = 600000;

    public static final long MAX_LIFE_TIME = 1800000;

    public static final int MAXIMUM_POOL_SIZE = 10;
}
