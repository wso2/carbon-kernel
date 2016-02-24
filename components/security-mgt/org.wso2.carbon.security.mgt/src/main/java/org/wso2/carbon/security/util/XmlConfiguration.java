/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.security.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.wso2.carbon.CarbonException;

import java.io.InputStream;
import java.util.List;

/**
 *
 */
public class XmlConfiguration {

    private static Log log = LogFactory.getLog(XmlConfiguration.class);

    private StAXOMBuilder builder;

    private String serverNamespace = null;

    public XmlConfiguration(String xmlFile) throws CarbonException {
        this(xmlFile, null);
    }

    public XmlConfiguration(String xmlFile, String serverNamespace) throws CarbonException {
        if (serverNamespace != null) {
            this.serverNamespace = serverNamespace;
        }
        try {
            builder = new StAXOMBuilder(xmlFile);
            builder.getDocumentElement().build();
        } catch (Exception e) {
            String msg =
                    "Error occurred while trying to instantiate StAXOMBuilder for XML file " +
                            xmlFile;
            log.error(msg, e);
            throw new CarbonException(msg, e);
        }
    }

    public XmlConfiguration(InputStream xmlFile, String serverNamespace) throws CarbonException {
        if (serverNamespace != null) {
            this.serverNamespace = serverNamespace;
        }
        try {
            builder = new StAXOMBuilder(xmlFile);
            builder.getDocumentElement().build();
        } catch (Exception e) {
            String msg =
                    "Error occurred while trying to instantiate StAXOMBuilder for XML file " +
                            xmlFile;
            log.error(msg, e);
            throw new CarbonException(msg, e);
        }
    }

    public String getUniqueValue(String xPath) {
        SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
        nsCtx.addNamespace("ns", serverNamespace);
        try {
            XPath xp = new AXIOMXPath(xPath);
            xp.setNamespaceContext(nsCtx);
            OMElement elem = builder.getDocumentElement();
            if (elem != null) {
                List nodeList = xp.selectNodes(elem);
                Object obj;
                if (!nodeList.isEmpty() && ((obj = nodeList.get(0)) != null)) {
                    return ((OMElement) obj).getText();
                }
            }
        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + xPath + " failed", e);
        }
        return null;
    }

    public OMElement[] getElements(String xPath) {
        SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
        nsCtx.addNamespace("ns", serverNamespace);
        try {
            XPath xp = new AXIOMXPath(xPath);
            xp.setNamespaceContext(nsCtx);
            OMElement elem = builder.getDocumentElement();
            if (elem != null) {
                List nodeList = xp.selectNodes(elem);
                return (OMElement[]) nodeList.toArray(new OMElement[nodeList.size()]);
            }
        } catch (JaxenException e) {
            throw new RuntimeException("XPath expression " + xPath + " failed", e);
        }
        return new OMElement[0];
    }
}
