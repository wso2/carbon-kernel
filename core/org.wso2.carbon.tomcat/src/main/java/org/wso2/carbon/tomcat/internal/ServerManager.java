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
package org.wso2.carbon.tomcat.internal;


import org.apache.catalina.LifecycleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.w3c.dom.*;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.SecurityConstants;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.wso2.securevault.secret.SecretManager;
import org.xml.sax.SAXException;

import javax.naming.Context;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Paths;

/**
 * Configuring,initialization and stopping the carbon tomcat instance
 */
public class ServerManager {

    private static Log log = LogFactory.getLog(ServerManager.class);
    private static CarbonTomcat tomcat;
    private InputStream inputStream;
    private SecretResolver resolver;
    static ClassLoader bundleCtxtClassLoader;
    private static final String SVNS = "svns";
    private static final String CARBON_URL_CONTEXT_FACTORY_PKG_PREFIX = "org.wso2.carbon.tomcat.jndi";
    private static final int ENTITY_EXPANSION_LIMIT = 0;


    /**
     * initialization code goes here.i.e : configuring tomcat instance using catalina-server.xml
     */
    public void init() {

        bundleCtxtClassLoader = Thread.currentThread().getContextClassLoader();
        String carbonHome = System.getProperty(CarbonBaseConstants.CARBON_HOME);
        String catalinaHome;
        String configPath = System.getProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        String catalinaXML;
        if (configPath == null) {
            catalinaXML = Paths.get(carbonHome, "repository", "conf", "tomcat", "catalina-server.xml").toString();
        } else {
            catalinaXML = Paths.get(configPath, "tomcat", "catalina-server.xml").toString();
        }
        try {
            inputStream = new FileInputStream(new File(catalinaXML));
        } catch (FileNotFoundException e) {
            log.error("could not locate the file catalina-server.xml", e);
        }
        //setting catalina.base system property. tomcat configurator refers this property while tomcat instance
        // creation.
        //you can override the property in wso2server.sh
        String internalLibPath = System.getProperty(CarbonBaseConstants.CARBON_INTERNAL_LIB_DIR_PATH);
        if (internalLibPath == null) {
            if (System.getProperty("catalina.base") == null) {
                System.setProperty("catalina.base", Paths.get(carbonHome, "lib", "tomcat").toString());
            }
            catalinaHome = Paths.get(carbonHome, "lib", "tomcat").toString();
        } else {
            System.setProperty("catalina.base", Paths.get(internalLibPath, "tomcat").toString());
            catalinaHome = Paths.get(internalLibPath, "tomcat").toString();
        }

        String value = CARBON_URL_CONTEXT_FACTORY_PKG_PREFIX;
        String oldValue = System.getProperty(Context.URL_PKG_PREFIXES);
        if (oldValue != null) {
            if (oldValue.contains(CARBON_URL_CONTEXT_FACTORY_PKG_PREFIX)) {
                value = oldValue;
            } else {
                value = CARBON_URL_CONTEXT_FACTORY_PKG_PREFIX + ":" + oldValue;
            }
        }
        System.setProperty(Context.URL_PKG_PREFIXES, value);

        tomcat = new CarbonTomcat();

        Element config = inputStreamToDOM(inputStream);
        if (SecretManager.getInstance().isInitialized()) {
            //creates DOM from input stream
            //creates Secret resolver
            resolver = SecretResolverFactory.create(config, true);
            //resolves protected passwords
            resolveSecuredConfig(config, null);
        }
        if (config.getAttributes().getNamedItem(XMLConstants.XMLNS_ATTRIBUTE + SecurityConstants.NS_SEPARATOR +
                SVNS) != null) {
            config.getAttributes().removeNamedItem(XMLConstants.XMLNS_ATTRIBUTE + SecurityConstants.NS_SEPARATOR +
                    SVNS);
        }
        // creates new input stream from processed DOM element
        InputStream newStream = domToInputStream(config);

        tomcat.configure(catalinaHome, newStream);
    }

    /**
     * starting the a tomcat instance in a new thread. Otherwise activator gets blocked.
     */
    public synchronized void start() {
        new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setContextClassLoader(bundleCtxtClassLoader);
                try {
                    tomcat.start();
                } catch (LifecycleException e) {
                    log.error("tomcat life-cycle exception", e);
                }

            }
        }).start();
    }

    /**
     * stopping the tomcat instance
     */
    public void stop() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            log.error("Error while stopping tomcat", e);
        }

    }

    /**
     * we are not expecting others to access this service. The only use case would be activator.
     * hence package private access modifier
     *
     * @return
     */
    CarbonTomcat getTomcatInstance() {
        return tomcat;
    }

    /**
     * Resolves the secured attributes in the configuration
     * Here we have assumed that secured attributes in each connector
     * has the same secret value. As an example, if two connectors has
     * keystore password value, it must be same value.
     *
     * @param root <code>Element</code>
     * @param tempToken  secured attribute alias as <code>String</code> 
     */
    private void resolveSecuredConfig(Node root,  String tempToken){

        String token = null;
        NamedNodeMap nodeMap = root.getAttributes();
        if(tempToken == null){
            tempToken = root.getNodeName();
        } else {
            tempToken = tempToken + "." + root.getNodeName();
        }
        if (nodeMap != null) {
            for (int j = 0; j < nodeMap.getLength(); j++) {
                Node node = nodeMap.item(j);
                if (node != null) {
                    String alias = MiscellaneousUtil.getProtectedToken(node.getNodeValue());
                    if (alias != null && alias.length() > 0) {
                        node.setNodeValue(MiscellaneousUtil.resolve(alias, resolver));
                    } else {
                        String attributeName = node.getNodeName();
                        token = tempToken + "." + attributeName;
                        if (resolver.isTokenProtected(token)) {
                            node.setNodeValue(resolver.resolve(token));
                            try {
                                nodeMap.removeNamedItem(
                                        SVNS + SecurityConstants.NS_SEPARATOR + SecurityConstants.SECURE_VAULT_ALIAS);
                            } catch (DOMException e) {
                                String msg =
                                        "Error while removing " + SVNS + SecurityConstants.NS_SEPARATOR + SecurityConstants.SECURE_VAULT_ALIAS;
                                // log is ignored
                                log.debug(msg, e);
                            }
                        }
                    }
                }
            }
        }

        NodeList nodeList = root.getChildNodes();
        for(int i = 0;  i < nodeList.getLength(); i++){
            resolveSecuredConfig(nodeList.item(i), tempToken);
        }
    }

    /**
     * Creates DOM from InputStream
     * 
     * @param inputStream    <code>InputStream</code>
     * @return  <code>Element</code>  or null
     */
    public static Element inputStreamToDOM(InputStream inputStream) {

        DocumentBuilderFactory factory = getSecuredDocumentBuilder();
        DocumentBuilder docBuilder;
        Document doc;
        Element element = null;

        try {
            docBuilder = factory.newDocumentBuilder();
            doc = docBuilder.parse(inputStream);
            element = doc.getDocumentElement();
        } catch (SAXException e) {
            log.error("Error while creating DOM element : " , e);
        } catch (IOException e) {
            log.error("Error while creating DOM element : " , e);
        } catch (ParserConfigurationException e) {
            log.error("Error while creating DOM element : " , e);
        } finally {            
            try {
                if(inputStream != null){
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Error while closing input stream : " , e);
            }
        }

        return element;
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    /**
     * Creates InputStream from DOM
     * 
     * @param root <code>Element</code>
     * @return <code>InputStream</code> or null
     */
    private InputStream  domToInputStream(Element root) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(outputStream);

        try{
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(root), outputTarget);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerConfigurationException e) {
            log.error("Error while creating input stream : " , e);
        } catch (TransformerException e) {
            log.error("Error while creating input stream : " , e);
        }

        return null;
    }
}

