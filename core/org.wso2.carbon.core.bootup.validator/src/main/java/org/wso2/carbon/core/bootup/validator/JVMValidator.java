/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.core.bootup.validator;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.bootup.validator.util.UnknownParameterException;
import org.wso2.carbon.core.bootup.validator.util.ValidationResult;

public class JVMValidator extends ConfigurationValidator {

	private static final Log log = LogFactory.getLog(JVMValidator.class);
	private RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

	public static final String INIT_HEAP_MEMORY_PARAM = "initHeapSize";
	public static final String MAX_HEAP_MEMORY_PARAM = "maxHeapSize";
	public static final String MAX_PERMGEN_SIZE_PARAM = "maxPermGenSize";
	private static final String JAVA_VERSION = "java.version";

	@Override
	public Map<String, ValidationResult> validate() {
		Map<String, String> recommendedConfigs = getRecommendedConfigurations();
		Map<String, ValidationResult> validationResults = new HashMap<String, ValidationResult>();
		for (String paramName : recommendedConfigs.keySet()) {
			try {
				ValidationResult result = validateConfiguration(paramName);
				validationResults.put(paramName, result);
			} catch (Exception e) {
				log.warn("Could not validate the JVM for configuration parameter : " + paramName);
				log.debug("Error occured while trying to validate configuration parameter : " +
				          paramName, e);
			}
		}
		return validationResults;
	}

	protected ValidationResult validateConfiguration(String parameterName) {
		ValidationResult result;
		if (INIT_HEAP_MEMORY_PARAM.equals(parameterName)) {
			String recommendedInitHeapSize = getRecommendedConfigurations().get(parameterName);
			long systemInitHeapSize = getInitHeapSize();
			result =
			         validateInitialHeapSize(systemInitHeapSize,
			                                 Long.parseLong(recommendedInitHeapSize));
		} else if (MAX_HEAP_MEMORY_PARAM.equals(parameterName)) {
			String recommendedMaxHeapSize = getRecommendedConfigurations().get(parameterName);
			long systemMaxHeapSize = getMaxHeapSize();
			result = validateMaxHeapSize(systemMaxHeapSize, Long.parseLong(recommendedMaxHeapSize));
		} else if (MAX_PERMGEN_SIZE_PARAM.equals(parameterName)) {
			String recommendedMaxPermGen = getRecommendedConfigurations().get(parameterName);
			long systemMaxPermGen = getPermGenSize();
			result =
			         validateMaxPermGenSize(systemMaxPermGen, Long.parseLong(recommendedMaxPermGen));
		} else {
			throw new UnknownParameterException(" Unknown paramater :" + parameterName);
		}
		return result;
	}

	private ValidationResult validateInitialHeapSize(long initHeapSize, long minReq) {
		ValidationResult result = new ValidationResult();
		String msg = null;
		boolean isValid;
		if (initHeapSize >= minReq) {
			isValid = true;
		} else {
			msg =
			      "Initial Heap Memory (MB) :" + initHeapSize +
			              " of the running JVM is set below the recommended minimum size :" +
			              minReq;
			isValid = false;
		}
		result.setValidationMessage(msg);
		result.setValid(isValid);
		return result;
	}

	private ValidationResult validateMaxHeapSize(long maxHeapSize, long minReq) {
		ValidationResult result = new ValidationResult();
		String msg = null;
		boolean isValid;
		if (maxHeapSize >= minReq) {
			isValid = true;
		} else {
			msg =
			      "Maximum Heap Memory (MB) :" + maxHeapSize +
			              " of the running JVM is set below the recommended minimum size :" +
			              minReq;
			isValid = false;
		}
		result.setValidationMessage(msg);
		result.setValid(isValid);
		return result;
	}

	private ValidationResult validateMaxPermGenSize(long maxPermSize, long minReq) {
		ValidationResult result = new ValidationResult();
		String msg = null;
		boolean isValid;
		// kernel is supposed to be run with java1.7 and above and max perm gen size is invalid from java 1.8 onwards.
		if (maxPermSize >= minReq || !"1.7".equals(getJavaVersion())) {
			isValid = true;
		} else {
			msg =
			      "Maximum PermGen space (MB) :" + maxPermSize +
			              " of the running JVM is set below the recommended minimum size :" +
			              minReq;
			isValid = false;
		}
		result.setValidationMessage(msg);
		result.setValid(isValid);
		return result;
	}

	/**
	 * @return The initial heap size (MB) in the JVM
	 */
	private long getInitHeapSize() {
		List<String> args = runtimeBean.getInputArguments();
		String initHeapArg = "-Xms";
		long initHeapSize = 0;
		for (String arg : args) {
			if (arg.contains(initHeapArg)) {
				int startIndex = arg.indexOf(initHeapArg) + 4;
				String value = arg.substring(startIndex);
				if (value.contains("m")) {
					int endIndex = value.indexOf('m');
					if (endIndex != -1) {
						value = value.substring(0, endIndex);
					}
				}
				initHeapSize = Long.parseLong(value);
				break;
			}
		}
		return initHeapSize;
	}

	/**
	 * @return The maximum heap size (MB) in the JVM
	 */
	private long getMaxHeapSize() {
		List<String> args = runtimeBean.getInputArguments();
		String maxHeapArg = "-Xmx";
		long maxHeapSize = 0;
		for (String arg : args) {
			if (arg.contains(maxHeapArg)) {
				int startIndex = arg.indexOf(maxHeapArg) + 4;
				String value = arg.substring(startIndex);
				if (value.contains("m")) {
					int endIndex = value.indexOf('m');
					if (endIndex != -1) {
						value = value.substring(0, endIndex);
					}
				}
				maxHeapSize = Long.parseLong(value);
				break;
			}
		}
		return maxHeapSize;
	}

	/**
	 * 
	 * @return The maximum permgen size (MB) in the JVM
	 */
	private long getPermGenSize() {
		List<String> args = runtimeBean.getInputArguments();
		String permGenArg = "-XX:MaxPermSize=";
		long permGenSize = 0;
		for (String arg : args) {
			if (arg.contains(permGenArg)) {
				int startIndex = arg.indexOf(permGenArg) + 16;
				String value = arg.substring(startIndex);
				if (value.contains("m")) {
					int endIndex = value.indexOf('m');
					if (endIndex != -1) {
						value = value.substring(0, endIndex);
					}
				}
				permGenSize = Long.parseLong(value);
				break;
			}
		}
		return permGenSize;
	}


	/**
	 * Returns the major version of java.
	 *
	 * @return major version of java
	 */
	private String getJavaVersion() {
		String javaVersion = System.getProperty(JAVA_VERSION);
		return javaVersion.substring(0, javaVersion.lastIndexOf("."));
	}
}
