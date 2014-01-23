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
package org.wso2.carbon.core.bootup.validator.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.wso2.carbon.core.bootup.validator.ConfigurationValidator;
import org.wso2.carbon.core.bootup.validator.util.ConfigValidationXMLProcessor;
import org.wso2.carbon.core.bootup.validator.util.ValidationResult;
import org.wso2.carbon.core.bootup.validator.util.ValidationResultPrinter;
import org.wso2.carbon.utils.CarbonUtils;

public class BootupValidationActivator implements BundleActivator {

	private static final Log log = LogFactory.getLog(BootupValidationActivator.class);
	private ConfigValidationXMLProcessor configXMLProcessor;
	private List<ConfigurationValidator> validators;
	private Map<String, ValidationResult> allResults = new HashMap<String, ValidationResult>();

	public void start(BundleContext bundleContext) {
		try {
            // If Carbon Kernel is running in the optimized mode, we avoid validating the resources
            if (CarbonUtils.isOptimized()) {
                return;
            }
			// parsing the config_validation.xml
			configXMLProcessor = new ConfigValidationXMLProcessor();
			configXMLProcessor.parseConfigValidationXml();
			if (ConfigValidationXMLProcessor.isActivated()) {
				log.debug("Bootup Validator is activated...");
				validators = configXMLProcessor.getValidators();
				validateConfigurations(validators);
				printWarnings();
			} else {
				log.debug("Bootup Validator is disabled...");
			}
		} catch (Exception e) {
			log.error("Error occured while starting Bootup Validation Activator", e);
		}
	}

	private void validateConfigurations(List<ConfigurationValidator> configValidators) {
		for (ConfigurationValidator validator : configValidators) {
			Map<String, ValidationResult> results = validator.validate();
			allResults.putAll(results);
		}
	}

	private void printWarnings() {
		ValidationResultPrinter.logConsoleWarnings(allResults);
	}

	public void stop(BundleContext arg0) throws Exception {

	}

}