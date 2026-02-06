/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.InstanceManager;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ui.deployment.UIBundleDeployer;
import org.wso2.carbon.ui.deployment.beans.CustomUIDefenitions;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import static org.wso2.carbon.CarbonConstants.PRODUCT_XML;
import static org.wso2.carbon.CarbonConstants.PRODUCT_XML_PROPERTIES;
import static org.wso2.carbon.CarbonConstants.PRODUCT_XML_PROPERTY;
import static org.wso2.carbon.CarbonConstants.PRODUCT_XML_WSO2CARBON;
import static org.wso2.carbon.CarbonConstants.WSO2CARBON_NS;

/**
 * ServletContextListener that initializes the Carbon UI ServletContext with required attributes.
 * This listener is registered with the OSGi HTTP Whiteboard to ensure attributes are set
 * when the ServletContext is created by the container.
 */
public class CarbonServletContextInitializer implements ServletContextListener {

    private static final Log log = LogFactory.getLog(CarbonServletContextInitializer.class);

    private final InstanceManager instanceManager;
    private final RegistryService registryService;
    private final ServerConfigurationService serverConfig;
    private final ConfigurationContext configurationContext;
    private final ConfigurationContext clientConfigurationContext;
    private final BundleContext bundleContext;
    private final String serverURL;
    private final String indexPageURL;
    private final CustomUIDefenitions customUIDefenitions;
    private final ClassLoader bundleClassLoader;
    private final UIBundleDeployer uiBundleDeployer;

    public CarbonServletContextInitializer(InstanceManager instanceManager,
                                           RegistryService registryService,
                                           ServerConfigurationService serverConfig,
                                           ConfigurationContext configurationContext,
                                           ConfigurationContext clientConfigurationContext,
                                           BundleContext bundleContext,
                                           String serverURL,
                                           String indexPageURL,
                                           CustomUIDefenitions customUIDefenitions,
                                           ClassLoader bundleClassLoader,
                                           UIBundleDeployer uiBundleDeployer) {
        this.instanceManager = instanceManager;
        this.registryService = registryService;
        this.serverConfig = serverConfig;
        this.configurationContext = configurationContext;
        this.clientConfigurationContext = clientConfigurationContext;
        this.bundleContext = bundleContext;
        this.serverURL = serverURL;
        this.indexPageURL = indexPageURL;
        this.customUIDefenitions = customUIDefenitions;
        this.bundleClassLoader = bundleClassLoader;
        this.uiBundleDeployer = uiBundleDeployer;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        // Set all required attributes when the ServletContext is initialized
        servletContext.setAttribute(InstanceManager.class.getName(), instanceManager);
        servletContext.setAttribute("registry", registryService);
        servletContext.setAttribute(CarbonConstants.SERVER_CONFIGURATION, serverConfig);
        servletContext.setAttribute(CarbonConstants.CLIENT_CONFIGURATION_CONTEXT, clientConfigurationContext);
        servletContext.setAttribute(CarbonConstants.CONFIGURATION_CONTEXT, configurationContext);
        servletContext.setAttribute(CarbonConstants.BUNDLE_CLASS_LOADER, bundleClassLoader);
        servletContext.setAttribute(CarbonConstants.SERVER_URL, serverURL);
        servletContext.setAttribute(CarbonConstants.INDEX_PAGE_URL, indexPageURL);
        servletContext.setAttribute(CarbonConstants.UI_BUNDLE_CONTEXT, bundleContext);
        servletContext.setAttribute(CustomUIDefenitions.CUSTOM_UI_DEFENITIONS, customUIDefenitions);

        // Read and set product XML attributes
        try {
            readProductXML(servletContext, uiBundleDeployer);
        } catch (Exception e) {
            log.error("Error reading product.xml", e);
        }

        // Register the ServletContext as an OSGi service for other bundles to use
        bundleContext.registerService(ServletContext.class.getName(), servletContext, null);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }

    /**
     * Read the product.xml file and set properties as servlet context attributes.
     * This contains info specific to the product.
     *
     * @param jspServletContext the servlet context
     * @param uiBundleDeployer the UI bundle deployer
     * @throws IOException if an I/O error occurs
     * @throws XMLStreamException if an XML parsing error occurs
     */
    private void readProductXML(ServletContext jspServletContext, UIBundleDeployer uiBundleDeployer)
            throws IOException, XMLStreamException {
        Enumeration<URL> e = bundleContext.getBundle().findEntries("META-INF", PRODUCT_XML, true);
        if (e != null) {
            URL url = e.nextElement();
            InputStream inputStream = url.openStream();
            XMLStreamReader streamReader =
                    XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(streamReader);
            OMElement document = builder.getDocumentElement();
            OMElement propsEle =
                    document.getFirstChildWithName(
                            new QName(WSO2CARBON_NS, PRODUCT_XML_PROPERTIES));
            if (propsEle != null) {
                Iterator<OMElement> properties = propsEle.getChildrenWithName(
                        new QName(WSO2CARBON_NS, PRODUCT_XML_PROPERTY));
                while (properties.hasNext()) {
                    OMElement property = properties.next();
                    String propertyName = property.getAttributeValue(new QName("name"));
                    String value = property.getText();
                    if (log.isDebugEnabled()) {
                        log.debug(PRODUCT_XML + ": " + propertyName + "=" + value);
                    }
                    //process collapsed menus in a different manner than other properties
                    if ("collapsedmenus".equals(propertyName)) {
                        ArrayList<String> collapsedMenuItems = new ArrayList<>();
                        if (value != null && value.indexOf(',') > -1) {
                            //multiple menu items provided.Tokenize & add iteratively
                            StringTokenizer st = new StringTokenizer(value, ",");
                            while (st.hasMoreTokens()) {
                                collapsedMenuItems.add(st.nextToken());
                            }
                        } else {
                            //single menu item specified.add this
                            collapsedMenuItems.add(value);
                        }
                        jspServletContext.setAttribute(PRODUCT_XML_WSO2CARBON + propertyName, collapsedMenuItems);

                        /*
                        Sometimes the values loaded to the jspServletContext is not available.
                        i.e. when the request is sent to /carbon
                        it works only if the request takes the pattern such as /carbon/admin/index.jsp
                        in the case of /carbon the params are read from utils hashmap which is saved at this point.
                         */
                        CarbonUIUtil.setProductParam(PRODUCT_XML_WSO2CARBON + propertyName, collapsedMenuItems);
                    } else {
                        jspServletContext.setAttribute(PRODUCT_XML_WSO2CARBON + propertyName, value);
                        CarbonUIUtil.setProductParam(PRODUCT_XML_WSO2CARBON + propertyName, value);
                    }
                }
            }
        }
    }
}
