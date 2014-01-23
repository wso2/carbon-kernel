/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.core.security;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * This class is used as the runtime representation of the authenticators.xml file. This is a singleton
 * class that can be used to retrieve the the information about the different authenticators installed
 * in the system.
 */
public class AuthenticatorsConfiguration {

    private static Log log = LogFactory.getLog(AuthenticatorsConfiguration.class);

    /**
     * file name of the authenticators.xml
     */
    private static final String AUTHENTICATORS_FILE_NAME = "authenticators.xml";

    //Constant definitions for Elements in the authenticators.xml
    private static final String ELEM_AUTHENTICATOR = "Authenticator";
    private static final String ELEM_PRIORITY = "Priority";
    private static final String ELEM_CONFIG = "Config";
    private static final String ELEM_PARAMETER = "Parameter";
    private static final String ELEM_SKIP_AUTHENTICATION = "SkipAuthentication";
    private static final String ELEM_SKIP_SESSION_VALIDATION = "SkipSessionValidation";
    private static final String ELEM_URL_CONTAINS = "UrlContains";

    //Constant definitions for attributes in the authenticators.xml
    private static final String ATTR_NAME = "name";
    private static final String ATTR_DISABLED = "disabled";

    /**
     * Singleton authenticator instance
     */
    private static AuthenticatorsConfiguration instance = new AuthenticatorsConfiguration();

    /**
     * this class is used to represent an authenticator configuration in the runtime
     */
    public static class AuthenticatorConfig {

        private String name;

        private int priority;

        private boolean disabled;

        private Map<String, String> parameters = new Hashtable<String, String>();

        private List<String> authenticationSkippingUrls = new ArrayList<String>();

        private List<String> sessionValidationSkippingUrls = new ArrayList<String>();

        private AuthenticatorConfig(String name, int priority, boolean disabled, Map<String,
                String> params) {
            this.name = name;
            this.priority = priority;
            this.disabled = disabled;
            this.parameters = params;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void addAuthenticationSkippingUrl(String url) {
            this.authenticationSkippingUrls.add(url);
        }

        public void addSessionValidationSkippingUrl(String url) {
            this.sessionValidationSkippingUrls.add(url);
        }

        public List<String> getAuthenticationSkippingUrls() {
            return Collections.unmodifiableList(authenticationSkippingUrls);
        }

        public List<String> getSessionValidationSkippingUrls() {
            return Collections.unmodifiableList(sessionValidationSkippingUrls);
        }
    }

    private Map<String, AuthenticatorConfig> authenticatorConfigMap =
            new Hashtable<String, AuthenticatorConfig>();

    /**
     * Returns the AuthenticatorsConfiguration singleton instance
     *
     * @return AuthenticatorsConfiguration singleton instance
     */
    public static AuthenticatorsConfiguration getInstance() {
        return instance;
    }

    private AuthenticatorsConfiguration() {
        initialize();
    }

    /**
     * Read the authenticator info from the file and populate the in-memory model
     */
    private void initialize() {
        String authenticatorsFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                        "security" + File.separator + AUTHENTICATORS_FILE_NAME;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(new File(authenticatorsFilePath));
            OMElement documentElement = new StAXOMBuilder(fileInputStream).getDocumentElement();

            // for each every authenticator defined, create a AuthenticatorConfig instance
            for (Iterator authenticatorElements = documentElement.getChildrenWithLocalName(ELEM_AUTHENTICATOR);
                 authenticatorElements.hasNext();) {
                AuthenticatorConfig authenticatorConfig = processAuthenticatorElement((OMElement) authenticatorElements.next());
                if (authenticatorConfig != null) {
                    this.authenticatorConfigMap.put(authenticatorConfig.getName(), authenticatorConfig);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("authenticators.xml file is not available. Carbon Server is starting with the" +
                      "default authenticators");
        } catch (XMLStreamException e) {
            log.error("Error reading the authenticators.xml. Carbon Server is starting with the default" +
                      "authenticators.");
        }
        finally{
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close the file input stream created for authenticators.xml");
            }
        }
    }

    /**
     * Create AuthenticatorConfig elements for each authenticator entry
     * @param authenticatorElem OMElement for Authenticator
     * @return  AuthenticatorConfig object
     */
    private AuthenticatorConfig processAuthenticatorElement(OMElement authenticatorElem) {
        // read the name of the authenticator. this is a mandatory attribute.
        OMAttribute nameAttr = authenticatorElem.getAttribute(new QName(ATTR_NAME));
        // if the name is not given, do not register this authenticator
        if(nameAttr == null){
            log.warn("Each Authenticator Configuration should have a unique name attribute. +" +
                     "This Authenticator will not be registered.");
            return null;
        }
        String authenticatorName = nameAttr.getAttributeValue();

        // check whether the disabled attribute is set
        boolean disabled = false;
        if(authenticatorElem.getAttribute(new QName(ATTR_DISABLED)) != null){
            disabled = Boolean.parseBoolean(authenticatorElem.getAttribute(
                    new QName(ATTR_DISABLED)).getAttributeValue());
        }

        // read the priority
        int priority = 0;
        for(Iterator priorityElemItr = authenticatorElem.getChildrenWithLocalName(ELEM_PRIORITY);
            priorityElemItr.hasNext();){
            priority = Integer.parseInt(((OMElement)priorityElemItr.next()).getText());
        }

        // read the config parameters
        Map<String, String> parameterMap = new Hashtable<String, String>();
        for(Iterator configElemItr = authenticatorElem.getChildrenWithLocalName(ELEM_CONFIG);
            configElemItr.hasNext();){
            OMElement configElement = (OMElement)configElemItr.next();
            for(Iterator paramIterator = configElement.getChildrenWithLocalName(ELEM_PARAMETER);
                paramIterator.hasNext();){
                OMElement paramElem = (OMElement)paramIterator.next();
                OMAttribute paramNameAttr = paramElem.getAttribute(new QName(ATTR_NAME));
                if(paramNameAttr == null){
                    log.warn("An Authenticator Parameter should have a name attribute. Skipping the parameter.");
                    continue;
                }
                parameterMap.put(paramNameAttr.getAttributeValue(), paramElem.getText());
            }
        }

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig(authenticatorName,
                priority, disabled, parameterMap);

        // read authentication skipping urls
        for(Iterator configElemItr = authenticatorElem.getChildrenWithLocalName(ELEM_SKIP_AUTHENTICATION);
            configElemItr.hasNext();){
            OMElement configElement = (OMElement)configElemItr.next();
            for(Iterator paramIterator = configElement.getChildrenWithLocalName(ELEM_URL_CONTAINS);
                paramIterator.hasNext();){
                OMElement urlElement = (OMElement)paramIterator.next();

                if (urlElement.getText() != null && !urlElement.getText().isEmpty()) {
                    authenticatorConfig.addAuthenticationSkippingUrl(urlElement.getText().trim());
                }
            }
        }

        // read session validation skipping urls
        for(Iterator configElemItr = authenticatorElem.getChildrenWithLocalName(ELEM_SKIP_SESSION_VALIDATION);
            configElemItr.hasNext();){
            OMElement configElement = (OMElement)configElemItr.next();
            for(Iterator paramIterator = configElement.getChildrenWithLocalName(ELEM_URL_CONTAINS);
                paramIterator.hasNext();){
                OMElement urlElement = (OMElement)paramIterator.next();

                if (urlElement.getText() != null && !urlElement.getText().isEmpty()) {
                    authenticatorConfig.addSessionValidationSkippingUrl(urlElement.getText().trim());
                }
            }
        }

        return authenticatorConfig;
    }

    /**
     * Return the authenticator config for the given name
     * @param authenticatorName name of the authenticator
     * @return  AuthenticatorConfig object
     */
    public AuthenticatorConfig getAuthenticatorConfig(String authenticatorName){
        return authenticatorConfigMap.get(authenticatorName);
    }
}
