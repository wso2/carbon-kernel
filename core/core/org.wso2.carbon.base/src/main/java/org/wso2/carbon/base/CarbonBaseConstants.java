package org.wso2.carbon.base;

public class CarbonBaseConstants {
	public static final String CARBON_CONFIG_DIR_PATH = "carbon.config.dir.path";
	public static final String CARBON_SERVICEPACKS_DIR_PATH = "carbon.servicepacks.dir.path";
	public static final String CARBON_DROPINS_DIR_PATH = "carbon.dropins.dir.path";
	public static final String CARBON_EXTERNAL_LIB_DIR_PATH = "carbon.external.lib.dir.path"; // components/lib
	public static final String CARBON_EXTENSIONS_DIR_PATH = "carbon.extensions.dir.path";
	public static final String CARBON_COMPONENTS_DIR_PATH= "carbon.components.dir.path";
	public static final String CARBON_PATCHES_DIR_PATH = "carbon.patches.dir.path";
	public static final String CARBON_INTERNAL_LIB_DIR_PATH = "carbon.internal.lib.dir.path"; //lib normally internal tomcat
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
