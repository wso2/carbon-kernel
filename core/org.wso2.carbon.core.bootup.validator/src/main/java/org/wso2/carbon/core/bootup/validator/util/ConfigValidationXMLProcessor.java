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
package org.wso2.carbon.core.bootup.validator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.wso2.carbon.core.bootup.validator.ConfigurationValidator;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

/**
 * This class is used to process config_validation.xml parameters
 */
public class ConfigValidationXMLProcessor {

	public static final String CONFIG_VALIDATION_XML = "config-validation.xml";
	// tags
	public static final String CONFIG_ROOT_TAG = "Configuration";
	public static final String PARAMETER_TAG = "Parameter";
	public static final String VALIDATOR_TAG = "Validator";
	// tag attributes
	public static final String ID_ATTRIBUTE = "id";
	public static final String CLASS_ATTRIBUTE = "class";
	public static final String VALIDATOR_ENABLED_ATTRIBUTE = "enabled";

	// boolean to activate/deactivate bootup validator
	private static boolean activate = false;
	private static final Log log = LogFactory.getLog(ConfigValidationXMLProcessor.class);
	private Document dom;
	// list of configuration validators for different system configurations
	// eg: JVMValidator, SystemValidator
	private List<ConfigurationValidator> validators = new ArrayList<ConfigurationValidator>();

	public void parseConfigValidationXml() throws ParserConfigurationException, SAXException,
	                                      IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		dom = db.parse(getConfigRecommendationsXML());
		parseDocument();
	}

	private static String getConfigRecommendationsXML() {
		String confDir = CarbonUtils.getEtcCarbonConfigDirPath();
		String xmlPath = confDir + File.separator + CONFIG_VALIDATION_XML;
		return xmlPath;
	}

	private void parseDocument() {
		// get the Configuration root element:
		Element configRootElement = dom.getDocumentElement();
		String enabled = configRootElement.getAttribute(VALIDATOR_ENABLED_ATTRIBUTE);
		// process the validation xml only if it is enabled
		if (Boolean.valueOf(enabled)) {
			ConfigValidationXMLProcessor.activate = true;
			// process the list of Validator elements
			NodeList validatorNodeList = configRootElement.getElementsByTagName(VALIDATOR_TAG);
			if (validatorNodeList != null && validatorNodeList.getLength() > 0) {
				for (int i = 0; i < validatorNodeList.getLength(); i++) {
					Element el = (Element) validatorNodeList.item(i);
					// get the ConfigValidator object and add it to the list
					ConfigurationValidator validator;
					try {
						validator = initConfigValidator(el);
						validators.add(validator);
					} catch (Exception e) {
						log.error("Error occured while loading ConfigurationValidator class : " +
						                  el.getAttribute(CLASS_ATTRIBUTE), e);
					}

				}
			}
		}

	}

	/**
	 * Initializes an instance of the ConfigurationValidator class with the
	 * fully
	 * qualified class name given in the xml element.
	 * 
	 * @param elem
	 * @return ConfigurationValidator object
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private ConfigurationValidator initConfigValidator(Element elem) throws InstantiationException,
	                                                                IllegalAccessException,
	                                                                ClassNotFoundException {

		HashMap<String, String> validationParams = new HashMap<String, String>();
		String className = elem.getAttribute(CLASS_ATTRIBUTE);
		// loading the ConfigurationValidator class
		ConfigurationValidator validator =
		                                   (ConfigurationValidator) Class.forName(className)
		                                                                 .newInstance();
		NodeList nl = elem.getElementsByTagName(PARAMETER_TAG);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				String paramName = el.getAttribute(ID_ATTRIBUTE);
				if (el.hasChildNodes()) {
					String val = el.getFirstChild().getNodeValue();
					if (paramName != null && val != null) {
						validationParams.put(paramName, val);
					}
				}
			}
		}
		// setting the recommededConfigurations for this configValidator
		validator.setRecommendedConfigurations(validationParams);
		return validator;
	}

	public List<ConfigurationValidator> getValidators() {
		return validators;
	}

	public void setConfigValidators(ArrayList<ConfigurationValidator> configValidators) {
		this.validators = configValidators;
	}

	public static boolean isActivated() {
		return activate;
	}
}
