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
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

/**
 * Test class for ServerConfiguration related methods.
 */
public class ServerConfigurationTest {

    @BeforeClass
    public void createInstance() throws ServerConfigurationException, NoSuchFieldException,
            IllegalAccessException {
        assertFalse(getIsInitialized(ServerConfiguration.getInstance(), "isInitialized"));
        InputStream inputStream = readFile("carbon.xml");
        ServerConfiguration.getInstance().forceInit(inputStream);
        assertTrue(getIsInitialized(ServerConfiguration.getInstance(), "isInitialized"));
    }

    @BeforeMethod
    public void setIsInitializedToFalse() throws NoSuchFieldException, IllegalAccessException {
        // Setting isInitialized to false before every test
        Field isInitializedField = ServerConfiguration.class.getDeclaredField("isInitialized");
        isInitializedField.setAccessible(true);
        isInitializedField.set(ServerConfiguration.getInstance(), false);
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testForceInitWithLocationOfXMLConfig() throws NoSuchFieldException, IllegalAccessException,
            ServerConfigurationException {
        assertFalse(getIsInitialized(ServerConfiguration.getInstance(), "isInitialized"));
        ServerConfiguration.getInstance().forceInit(CarbonBaseUtilsTest.class.getClassLoader().getResource("carbon" +
                ".xml").toString());
        assertTrue(getIsInitialized(ServerConfiguration.getInstance(), "isInitialized"));
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testForceInitWithXMLConfigAndBooleanAsArgs() throws NoSuchFieldException, IllegalAccessException,
            ServerConfigurationException {
        assertFalse(getIsInitialized(ServerConfiguration.getInstance(), "isInitialized"));
        ServerConfiguration.getInstance().forceInit(CarbonBaseUtilsTest.class.getClassLoader().getResource("carbon" +
                ".xml").toString(), false);
        assertTrue(getIsInitialized(ServerConfiguration.getInstance(), "isInitialized"));
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testSetConfigurationProperty() throws Exception {
        ServerConfiguration.getInstance().setConfigurationProperty("ServerKey", "AM-test");
        String[] actualElement = ServerConfiguration.getInstance().getProperties("ServerKey");
        String[] expectedElement = {"${product.key}", "AM-test"};
        assertEquals(actualElement, expectedElement);
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testOverrideConfigurationProperty() {
        ServerConfiguration.getInstance().overrideConfigurationProperty("RegistryHttpPort", "9780");
        String actualElement = ServerConfiguration.getInstance().getFirstProperty("RegistryHttpPort");
        String expectedElement = "9780";
        assertEquals(actualElement, expectedElement);
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testGetFirstProperty() {
        String actualElement1 = ServerConfiguration.getInstance().getFirstProperty("ServiceUserRoles.Role.Description");
        assertEquals(actualElement1, "Default Administrator " +
                "Role", "Testing first element of the ServiceUserRoles.Role.Description");
        String actualElement2 = ServerConfiguration.getInstance().getFirstProperty("NotPresentInConfig");
        assertEquals(actualElement2, null, "Testing first element of the NotPresentInConfig");
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testGetProperties() {
        String actualProperties1[] = ServerConfiguration.getInstance().getProperties("HttpGetRequestProcessors" +
                ".Processor.Class");
        String expectedProperties[] = {"org.wso2.carbon.core.transports.util.InfoProcessor", "org" +
                ".wso2.carbon.core.transports.util.Wsdl11Processor", "org.wso2.carbon.core.transports.util" +
                ".Wsdl20Processor", "org.wso2.carbon.core.transports.util.XsdProcessor"};
        assertArrayEquals("Testing properties received for 'HttpGetRequestProcessors.Processor.Class' key",
                expectedProperties, actualProperties1);
        String actualProperties2[] = ServerConfiguration.getInstance().getProperties("NotPresentInConfig");
        assertEquals(actualProperties2.length, 0, "Testing properties received for a key not present in config");
    }

    @Test(groups = {"org.wso2.carbon.base"})
    public void testGetDocumentElement() throws XMLStreamException, ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException, ParserConfigurationException,
            IOException, SAXException {
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
        Element actualElement = ServerConfiguration.getInstance().getDocumentElement();
        assertEquals(actualElement.toString(), expectedElement.toString());
    }

    /**
     * Used for testGetDocumentElement.
     *
     * @return a DocumentBuilderFactory instance
     */
    private DocumentBuilderFactory getSecuredDocumentBuilder() throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
        dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
        dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(0);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }

    /**
     * This returns the contents of the txt file as an input stream.
     *
     * @param path location path to the relevant txt file
     * @return an inputstream containing the contents of the given file
     */
    private InputStream readFile(String path) {
        ClassLoader classLoader = ServerConfigurationTest.class.getClassLoader();
        return classLoader.getResourceAsStream(path);
    }

    /**
     * This checks whether the carbon server is initialized.
     *
     * @param carbonServerConfiguration Carbon server configuration instance
     * @param name                      Property being used for validation
     * @return The server is initialized or not
     */
    private boolean getIsInitialized(ServerConfiguration carbonServerConfiguration, String name) throws
            NoSuchFieldException, IllegalAccessException {
        Field isInitializedField = ServerConfiguration.class.getDeclaredField(name);
        isInitializedField.setAccessible(true);
        return (boolean) isInitializedField.get(carbonServerConfiguration);
    }
}
