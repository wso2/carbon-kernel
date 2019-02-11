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

import org.wso2.carbon.core.bootup.validator.util.ValidationResult;

@Deprecated
public abstract class ConfigurationValidator {
	// the configurations to validate and their recommended values
	private Map<String, String> recommendedConfigurations = new HashMap<String, String>();

	/**
	 * @return map of validation results for all the validation parameters assigned
	 *         to this ConfigurationValidator class
	 */
	public abstract Map<String, ValidationResult> validate();

	public Map<String, String> getRecommendedConfigurations() {
		return recommendedConfigurations;
	}

	public void setRecommendedConfigurations(Map<String, String> recommendedConfigurations) {
		this.recommendedConfigurations = recommendedConfigurations;
	}

}
