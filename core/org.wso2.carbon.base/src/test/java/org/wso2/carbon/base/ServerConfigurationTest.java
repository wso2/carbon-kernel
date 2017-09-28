/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.base;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kasun on 9/26/17.
 */
public class ServerConfigurationTest {
    private static final Logger logger = Logger.getLogger(ServerConfigurationTest.class);
    private static ServerConfiguration carbonServerConfiguration;
    private static URL resourceURL;

    @BeforeClass
    public static void createInstance() throws ServerConfigurationException, NoSuchFieldException, IllegalAccessException {
        carbonServerConfiguration = ServerConfiguration.getInstance();
        resourceURL = CarbonBaseUtilsTest.class.getClassLoader().getResource("carbon.xml");
        assertFalse(getIsInitialized(carbonServerConfiguration, "isInitialized"));
        InputStream inputStream = readFile("carbon.xml");
        carbonServerConfiguration.forceInit(inputStream);
        assertTrue(getIsInitialized(carbonServerConfiguration, "isInitialized"));
    }

    @Before
    public void setIsInitializedToFalse() throws NoSuchFieldException, IllegalAccessException {
        //settig isInitialized to false before every test
        Field isInitializedField = ServerConfiguration.class.getDeclaredField("isInitialized");
        isInitializedField.setAccessible(true);
        isInitializedField.set(carbonServerConfiguration, false);
    }

    @Test
    public void testForceInitWithLocationOfXMLConfig() throws NoSuchFieldException, IllegalAccessException,
            ServerConfigurationException {
        assertFalse(getIsInitialized(carbonServerConfiguration, "isInitialized"));
        carbonServerConfiguration.forceInit(resourceURL.toString());
        assertTrue(getIsInitialized(carbonServerConfiguration, "isInitialized"));
    }

    @Test
    public void testForceInitWithXMLConfigAndBooleanAsArgs() throws NoSuchFieldException, IllegalAccessException, ServerConfigurationException {
        assertFalse(getIsInitialized(carbonServerConfiguration, "isInitialized"));
        carbonServerConfiguration.forceInit(resourceURL.toString(), false);
        assertTrue(getIsInitialized(carbonServerConfiguration, "isInitialized"));
    }

    @Test
    public void testSetConfigurationProperty() throws Exception {
        carbonServerConfiguration.setConfigurationProperty("ServerKey", "AM");
        //need to update the test after issue #1560 is fixed

    }

    @Test
    public void testOverrideConfigurationProperty() {
        carbonServerConfiguration.overrideConfigurationProperty("RegistryHttpPort", "9764");
        //need to update the test after issue #1560 is fixed

    }

    @Test
    public void testGetFirstProperty() {
        String actualElement1 = carbonServerConfiguration.getFirstProperty("ServiceUserRoles.Role.Description");
        assertEquals("Testing first element of the ServiceUserRoles.Role.Description", "Default Administrator Role",
                actualElement1);
        String actualElement2 = carbonServerConfiguration.getFirstProperty("NotPresentInConfig");
        assertEquals("Testing first element of the NotPresentInConfig", null, actualElement2);
    }

    @Test
    public void testGetProperties() {
        String actualProperties1[] = carbonServerConfiguration.getProperties("HttpGetRequestProcessors.Processor" +
                ".Class");
        String expectedProperties[] = {"org.wso2.carbon.core.transports.util.InfoProcessor", "org" +
                ".wso2.carbon.core.transports.util.Wsdl11Processor", "org.wso2.carbon.core.transports.util" +
                ".Wsdl20Processor", "org.wso2.carbon.core.transports.util.XsdProcessor"};
        assertArrayEquals("Testing properties received for 'HttpGetRequestProcessors.Processor.Class' key",
                expectedProperties, actualProperties1);
        String actualProperties2[] = carbonServerConfiguration.getProperties("NotPresentInConfig");
        assertEquals("Testing properties received for a key not present in config", 0, actualProperties2.length);
    }

    @Test
    public void testGetDocumentElement() throws XMLStreamException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ParserConfigurationException, IOException, SAXException {
        InputStream carbonInputStream = readFile("carbon.xml");
        OMElement documentElement = new StAXOMBuilder(carbonInputStream)
                .getDocumentElement();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        documentElement.serialize(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                outputStream.toByteArray());

        DocumentBuilderFactory factory = getSecuredDocumentBuilder();
        factory.setNamespaceAware(true);
        Element expectedElement = factory.newDocumentBuilder().parse(inputStream)
                .getDocumentElement();
        Element actualElement = carbonServerConfiguration.getDocumentElement();
        assertEquals(expectedElement.toString(),actualElement.toString());
    }

    /**
     * Used for testGetDocumentElement
     *
     * @return
     */
    private DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            logger.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    /**
     * This is used to get the contents of the txt file as an input stream
     *
     * @param path location path to the relevant txt file
     * @return an inputstream containing the contents of the given file
     */
    private static InputStream readFile(String path) {
        InputStream inputStream;
        ClassLoader classLoader = ServerConfigurationTest.class.getClassLoader();
        inputStream = classLoader.getResourceAsStream(path);
        return inputStream;
    }

    private static boolean getIsInitialized(ServerConfiguration carbonServerConfiguration, String name) throws
            NoSuchFieldException, IllegalAccessException {
        boolean isInitialized;
        Field isInitializedField = ServerConfiguration.class.getDeclaredField(name);
        isInitializedField.setAccessible(true);
        isInitialized = (boolean) isInitializedField.get(carbonServerConfiguration);
        return isInitialized;
    }
}

