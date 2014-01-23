/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.server.admin.privilegedaction;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class processes the XML configuration file <code>privilegedaction.xml</code> found in the root level of
 * <code>org.wso2.carbon.server.admin</code> bundle.
 * The class reads the following two properties
 * <code>skipServiceInvocation</code>
 * <code>skipLowerPriorityExtensions</code>
 */
public class PrivilegedActionConfigurationXMLProcessor {

    private static final Log log = LogFactory.getLog(PrivilegedActionConfigurationXMLProcessor.class);

    public static final String PRIVILEGED_ACTION_CONFIG_FILE = "privilegedaction.xml";
    public static final String LOCAL_NAME_PRIVILEGED_ACTION = "privilegedaction";
    public static final String LOCAL_NAME_SKIP_SERVICE_INVOCATION = "skipServiceInvocation";
    public static final String LOCAL_NAME_SKIP_LOWER_PRIORITY_EXTENSIONS = "skipLowerPriorityExtensions";

    /**
         * Retrun <code>PrivilegedActionConfiguration</code> read from <code>privilegedaction.xml</code>
         *
         * @return  PrivilegedActionConfiguration
         * @throws PrivilegedActionException
         */
    public static PrivilegedActionConfiguration buildPrivilegedActionConfigurationFromFile()
            throws PrivilegedActionException {
        OMElement privilegedActionElement = getPrivilegedActionElement();
        PrivilegedActionConfiguration privilegedActionConfig = buildPrivilegedActionConfiguration(privilegedActionElement);
        return privilegedActionConfig;
    }

    private static PrivilegedActionConfiguration buildPrivilegedActionConfiguration(OMElement privilegedActionElement) {
        PrivilegedActionConfiguration privilegedActionConfig;
        boolean skipServiceInvocation;
        boolean skipLowerPriorityExtensions;

        skipServiceInvocation = Boolean.parseBoolean(privilegedActionElement
              .getFirstChildWithName(new QName(LOCAL_NAME_SKIP_SERVICE_INVOCATION)).getText());
        skipLowerPriorityExtensions = Boolean.parseBoolean(privilegedActionElement
              .getFirstChildWithName(new QName(LOCAL_NAME_SKIP_LOWER_PRIORITY_EXTENSIONS)).getText());

        privilegedActionConfig = new PrivilegedActionConfiguration();
        privilegedActionConfig.setSkipServiceInvocation(skipServiceInvocation);
        privilegedActionConfig.setSkipLowerPriorityExtensions(skipLowerPriorityExtensions);

        return privilegedActionConfig;
    }

    private static OMElement getPrivilegedActionElement() throws PrivilegedActionException {

        InputStream inStream = PrivilegedActionConfigurationXMLProcessor.class.getResourceAsStream("/" + PRIVILEGED_ACTION_CONFIG_FILE);

        if(inStream == null){
            throw new PrivilegedActionException("Unable to find PrivilegedAction configuration file " + PRIVILEGED_ACTION_CONFIG_FILE);
        }

        OMElement privilegedActionElement = null;
        try{
            String confString = convertStreamToString(inStream);
            privilegedActionElement = createOMElement(confString);
            inStream.close();
        } catch (IOException e) {
            log.error("Error occurred while closing InputStream of " + PRIVILEGED_ACTION_CONFIG_FILE, e);
        }
        return privilegedActionElement;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    private static OMElement createOMElement(String string) throws PrivilegedActionException{
        int start = string.indexOf("<" + LOCAL_NAME_PRIVILEGED_ACTION);
        int end = string.lastIndexOf(LOCAL_NAME_PRIVILEGED_ACTION + ">") + LOCAL_NAME_PRIVILEGED_ACTION.length() + 1;
        String stringWithoutComments = string.substring(start, end);
        String trimmedString = stringWithoutComments.replaceAll("[\\n\\s]","");
        try {
            return AXIOMUtil.stringToOM(trimmedString);
        } catch (XMLStreamException e) {
            log.error("Unable to parse " + PRIVILEGED_ACTION_CONFIG_FILE, e);
            throw new PrivilegedActionException("Unable to parse " + PRIVILEGED_ACTION_CONFIG_FILE);
        }
    }


}
