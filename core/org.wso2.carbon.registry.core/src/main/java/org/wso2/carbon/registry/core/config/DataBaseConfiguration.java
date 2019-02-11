/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.config;

import org.wso2.securevault.SecretResolver;

/**
 * This class is to hold, database configurations such as URL, userName, and
 * password. Users can define them in registry.xml or at specify them at runtime
 * using the APIs provided.
 */
public class DataBaseConfiguration {

	private String dataSourceName;
	private String dbUrl;
	private String userName;
	private String password;
	private String driverName;
	private String configName;

	private String maxWait = null;
	private String maxActive = null;
	private String maxIdle = null;
	private String minIdle = null;
	private String validationQuery = null;
	private SecretResolver secretResolver;
	private String testWhileIdle = null;
	private String timeBetweenEvictionRunsMillis = null;
	private String minEvictableIdleTimeMillis = null;
	private String numTestsPerEvictionRun = null;

	/**
	 * Method to obtain the test while idle of the data source.
	 * 
	 * @return the testWhileIdle of the data source.
	 */
	public String getTestWhileIdle() {
		return testWhileIdle;
	}

	/**
	 * Method to set the TestWhileIdle of the data source.
	 * 
	 * @param testWhileIdle
	 *            the value of TestWhileIdle of the data source.
	 */
	public void setTestWhileIdle(String testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	/**
	 * Method to obtain the time between eviction runs millis of the data
	 * source.
	 * 
	 * @return the timeBetweenEvictionRunsMillis of the data source.
	 */
	public String getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	/**
	 * Method to set the Time Between Eviction Runs Millis of the data source.
	 * 
	 * @param timeBetweenEvictionRunsMillis
	 *            the value of the timeBetweenEvictionRunsMillis of the data
	 *            source.
	 */
	public void setTimeBetweenEvictionRunsMillis(
			String timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	/**
	 * Method to obtain the min evictable idle time millis of the data source.
	 * 
	 * @return the minEvictableIdleTimeMillis of the data source.
	 */
	public String getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	/**
	 * Method to set the Min Evictable Idle Time Millis of the data source.
	 * 
	 * @param minEvictableIdleTimeMillis
	 *            the value of the minEvictableIdleTimeMillis of the data
	 *            source.
	 */
	public void setMinEvictableIdleTimeMillis(String minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * Method to obtain the name of the data source.
	 * 
	 * @return the name of the data source.
	 */
	public String getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	/**
	 * Method to set the Num Tests PerEviction Run of the data source.
	 * 
	 * @param numTestsPerEvictionRun
	 *            the value of NumTestsPerEvictionRun of the data source.
	 */
	public void setNumTestsPerEvictionRun(String numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	/**
	 * Method to obtain the name of the data source.
	 * 
	 * @return the name of the data source.
	 */
	public String getDataSourceName() {
		return dataSourceName;
	}

	/**
	 * Method to set the name of the data source.
	 * 
	 * @param dataSourceName
	 *            the name of the data source.
	 */
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	/**
	 * Method to obtain the database URL.
	 * 
	 * @return the database URL.
	 */
	public String getDbUrl() {
		return dbUrl;
	}

	/**
	 * Method to set the database URL.
	 * 
	 * @param dbUrl
	 *            the database URL.
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	/**
	 * Method to obtain the user name.
	 * 
	 * @return the user name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Method to set the user name.
	 * 
	 * @param userName
	 *            the user name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Method to obtain the password.
	 * 
	 * @return the password.
	 */
	public String getPassWord() {
		return password;
	}

	/**
	 * Method to set the password.
	 * 
	 * @param password
	 *            the password.
	 */
	public void setPassWord(String password) {
		this.password = password;
	}

	/**
	 * Method to obtain the name of the driver.
	 * 
	 * @return the name of the driver.
	 */
	public String getDriverName() {
		return driverName;
	}

	/**
	 * Method to set the name of the driver.
	 * 
	 * @param driverName
	 *            the name of the driver.
	 */
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	/**
	 * Method to obtain the name of the configuration.
	 * 
	 * @return the name of the configuration.
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * Method to set the name of the configuration.
	 * 
	 * @param configName
	 *            the name of the configuration.
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	/**
	 * Method to obtain the maxWait value.
	 * 
	 * @return the maxWait value defined.
	 */
	public String getMaxWait() {
		return maxWait;
	}

	/**
	 * Method to set the maxWait value.
	 * 
	 * @param maxWait
	 *            the maxWait value defined.
	 */
	public void setMaxWait(String maxWait) {
		this.maxWait = maxWait;
	}

	/**
	 * Method to obtain the maxActive value.
	 * 
	 * @return the maxActive value defined.
	 */
	public String getMaxActive() {
		return maxActive;
	}

	/**
	 * Method to set the maxActive value.
	 * 
	 * @param maxActive
	 *            the maxActive value defined.
	 */
	public void setMaxActive(String maxActive) {
		this.maxActive = maxActive;
	}

	/**
	 * Method to obtain the maxIdle value.
	 * 
	 * @return the maxIdle value defined.
	 */
	public String getMaxIdle() {
		return maxIdle;
	}

	/**
	 * Method to set the maxIdle value.
	 * 
	 * @param maxIdle
	 *            the maxIdle value defined.
	 */
	public void setMaxIdle(String maxIdle) {
		this.maxIdle = maxIdle;
	}

	/**
	 * Method to obtain the minIdle value.
	 * 
	 * @return the minIdle value defined.
	 */
	public String getMinIdle() {
		return minIdle;
	}

	/**
	 * Method to set the minIdle value.
	 * 
	 * @param minIdle
	 *            the minIdle value defined.
	 */
	public void setMinIdle(String minIdle) {
		this.minIdle = minIdle;
	}

	public void setPasswordManager(SecretResolver secretResolver) {
		this.secretResolver = secretResolver;
	}

	/**
	 * If the password is protected , then decrypts the password and returns the
	 * plain text Otherwise, returns the given password as-is
	 * 
	 * @return Resolved password
	 */
	public String getResolvedPassword() {
		if (secretResolver != null && secretResolver.isInitialized()) {
			if (secretResolver.isTokenProtected("wso2registry." + configName
					+ ".password")) {
				return secretResolver.resolve("wso2registry." + configName
						+ ".password");
			} else {
				return password;
			}
		} else {
			return password;
		}
	}

	/**
	 * Method to obtain the validation query value.
	 * 
	 * @return the validation query value defined.
	 */
	public String getValidationQuery() {
		return validationQuery;
	}

	/**
	 * Method to set the validation query value.
	 * 
	 * @param validationQuery
	 *            the validation query value defined.
	 */
	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}
}
