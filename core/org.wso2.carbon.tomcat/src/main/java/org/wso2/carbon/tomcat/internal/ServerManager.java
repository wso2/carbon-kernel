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
import org.w3c.dom.*;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.secret.SecretManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Configuring,initialization and stopping the carbon tomcat instance
 */
public class ServerManager {

    private static Log log = LogFactory.getLog(ServerManager.class);
    private static CarbonTomcat tomcat;
    private InputStream inputStream;
    private SecretResolver resolver;
    static ClassLoader bundleCtxtClassLoader;


    /**
     * initialization code goes here.i.e : configuring tomcat instance using catalina-server.xml
     */
    public void init() {
        bundleCtxtClassLoader = Thread.currentThread().getContextClassLoader();
        String carbonHome = System.getProperty("carbon.home");
        String catalinaHome = new File(carbonHome).getAbsolutePath() + File.separator + "lib" +
                File.separator + "tomcat";
        String catalinaXML = new File(carbonHome).getAbsolutePath() + File.separator +
                "repository" + File.separator + "conf" + File.separator +
                "tomcat" + File.separator + "catalina-server.xml";
        try {
            inputStream = new FileInputStream(new File(catalinaXML));
        } catch (FileNotFoundException e) {
            log.error("could not locate the file catalina-server.xml", e);
        }
        //setting catalina.base system property. tomcat configurator refers this property while tomcat instance creation.
        //you can override the property in wso2server.sh
        if (System.getProperty("catalina.base") == null) {
            System.setProperty("catalina.base", System.getProperty("carbon.home") + File.separator +
                    "lib" + File.separator + "tomcat");
        }

        tomcat = new CarbonTomcat();

        if(SecretManager.getInstance().isInitialized()){
            //creates DOM from input stream
            Element config = inputStreamToDOM(inputStream);
            //creates Secret resolver
            resolver = SecretResolverFactory.create(config, true);
            //resolves protected passwords
            resolveSecuredConfig(config, null);
            // creates new input stream from processed DOM element
            InputStream newStream = domToInputStream(config);
            
            tomcat.configure(catalinaHome, newStream);
        } else {
            tomcat.configure(catalinaHome, inputStream);    
        }
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
                if(node != null){
                    String attributeName = node.getNodeName();
                    token = tempToken + "." + attributeName;
                    if(resolver.isTokenProtected(token)){
                        node.setNodeValue(resolver.resolve(token));
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

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

