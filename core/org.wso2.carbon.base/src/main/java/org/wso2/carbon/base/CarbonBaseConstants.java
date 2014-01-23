package org.wso2.carbon.base;

public class CarbonBaseConstants {
	public static final String CARBON_CONFIG_DIR_PATH = "carbon.config.dir.path";
	public static final String CARBON_CONFIG_DIR_PATH_ENV = "CARBON_CONFIG_DIR_PATH";
	public static final String CARBON_HOME = "carbon.home";
	public static final String CARBON_HOME_ENV = "CARBON_HOME";
	public static final String AXIS2_CONFIG_REPO_LOCATION = "Axis2Config.RepositoryLocation";

	/**
	 * Remove default constructor and make it not available to initialize.
	 */

	private CarbonBaseConstants() {
		throw new AssertionError("Instantiating utility class...");
	}

}
