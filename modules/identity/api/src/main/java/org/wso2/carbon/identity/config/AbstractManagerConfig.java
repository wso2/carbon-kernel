package org.wso2.carbon.identity.config;

import java.util.Properties;

public class AbstractManagerConfig {

	private Properties properties;
	private String clazzName;

	/**
	 * 
	 * @param clazzName
	 * @param properties
	 */
	public AbstractManagerConfig(String clazzName, Properties properties) {
		this.properties = properties;
		this.clazzName = clazzName;
	}

	/**
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * 
	 * @return
	 */
	public String getClazzName() {
		return clazzName;
	}

}
