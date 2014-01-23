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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.bootup.validator.util.ValidationResult;

public class RequiredSystemPropertiesValidator extends ConfigurationValidator {

	private static final Log log = LogFactory.getLog(RequiredSystemPropertiesValidator.class);

	@Override
	public Map<String, ValidationResult> validate() {
		Map<String, String> recommendedSystemProperties = getRecommendedConfigurations();
		Map<String, ValidationResult> validationResults = new HashMap<String, ValidationResult>();
		// validate systemProperty values
		for (String propKey : recommendedSystemProperties.values()) {
			try {
				ValidationResult result = validateSystemProperty(propKey);
				validationResults.put(propKey, result);
			} catch (Exception e) {
				log.warn("Could not validate system property : " + propKey);
				log.debug("Error occured while trying to validate system property : " + propKey, e);
			}
		}
		return validationResults;
	}

	/**
	 * Validates the given system property is not null
	 * 
	 * @param propKey
	 *            the system property key
	 * @return Validation Result
	 */
	private ValidationResult validateSystemProperty(String propKey) {
		ValidationResult result = new ValidationResult();
		String msg = null;
		boolean isValid;
		String systemPropertyValue = System.getProperty(propKey);
		if (systemPropertyValue == null) {
			systemPropertyValue = System.getenv(propKey);
		}
		if (systemPropertyValue != null) {
			isValid = true;
		} else {
			msg = "Value is not set for the required system property :" + propKey;
			isValid = false;
		}
		result.setValidationMessage(msg);
		result.setValid(isValid);
		return result;
	}

}
