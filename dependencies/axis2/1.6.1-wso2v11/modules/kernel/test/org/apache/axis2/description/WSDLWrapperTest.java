/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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

package org.apache.axis2.description;

import junit.framework.TestCase;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.wsdl.util.WSDLDefinitionWrapper;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class WSDLWrapperTest extends TestCase {

    private static final String JAVAX_WSDL_VERBOSE_MODE_KEY = "javax.wsdl.verbose";

    //private Definition wsdlDef = null;
    private WSDLLocator customWSLD4JResolver = null;
    private String baseUri = null;

    private AxisConfiguration axisCfg = null;

    private String axis2xml = AbstractTestCase.basedir +
                      "/target/test-resources/deployment/axis2_a.xml";

    private String wsdl1 = AbstractTestCase.basedir + 
                      "/target/test-resources/wsdl/actionTests.wsdl"; 

    private String wsdl2 = AbstractTestCase.basedir + 
                      "/target/test-resources/wsdl/test1.wsdl"; 

    private String wsdl3 = AbstractTestCase.basedir + 
                      "/target/test-resources/wsdl/test2.wsdl"; 



    public WSDLWrapperTest(String name) {
		super(name);
    }


    public void testWsdlWrapper() {
        try {

            //----------------------------------------------------------------
            // load the wsdl
            //----------------------------------------------------------------

            Definition def1 = null;

            File testResourceFile1 = new File(wsdl1);

            if (testResourceFile1.exists()) {
                try {
                    System.out.println("WSDL file 1: " + testResourceFile1.getName());
                    def1 = readInTheWSDLFile(new FileInputStream(testResourceFile1)); 
                } 
                catch (Exception e1) {
                    System.out.println("Error in WSDL : " + testResourceFile1.getName());
                    System.out.println("Exception: " + e1.toString());
                    throw e1;
                }
            }

            //wsdlDef = def1;
            //System.out.println("-------------------------------------------------");
            //System.out.println("wsdlDef 1 = [");
            //System.out.println(wsdlDef);
            //System.out.println("]");
            //System.out.println("-------------------------------------------------");


            //----------------------------------------------------------------
            // setup the wrapper
            //----------------------------------------------------------------

            axisCfg = ConfigurationContextFactory
                      .createConfigurationContextFromFileSystem(null, axis2xml)
                      .getAxisConfiguration();

            WSDLDefinitionWrapper passthru = new WSDLDefinitionWrapper(def1, testResourceFile1.toURL(), false);

            Definition def_passthru = passthru.getUnwrappedDefinition();
            String def_passthru_str = def_passthru.toString();
            QName def_passthru_qn = def_passthru.getQName();
            String def_passthru_namespace = def_passthru.getTargetNamespace();
            Types def_passthru_types = def_passthru.getTypes();

            WSDLDefinitionWrapper serialize = new WSDLDefinitionWrapper(def1, testResourceFile1.toURL(), axisCfg); 

            Definition def_serialize = serialize.getUnwrappedDefinition();
            String def_serialize_str = def_serialize.toString();
            QName def_serialize_qn = def_serialize.getQName();
            String def_serialize_namespace = def_serialize.getTargetNamespace();
            Types def_serialize_types = def_serialize.getTypes();

            WSDLDefinitionWrapper reload = new WSDLDefinitionWrapper(def1, testResourceFile1.toURL(), 2); 

            Definition def_reload = reload.getUnwrappedDefinition();
            String def_reload_str = def_reload.toString();
            QName def_reload_qn = def_reload.getQName();
            String def_reload_namespace = def_reload.getTargetNamespace();
            Types def_reload_types = def_reload.getTypes();

        }
        catch (Exception exx) {
            fail("This can not fail with this Exception " + exx);
        }
    }


    private Definition readInTheWSDLFile(InputStream in) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // switch off the verbose mode for all usecases
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE_KEY, false);

        // if the custem resolver is present then use it
        if (customWSLD4JResolver != null) {
            return reader.readWSDL(customWSLD4JResolver);
        } 
        else {
            Document doc;
            try {
                doc = newDocument(in);
            } 
            catch (ParserConfigurationException e) {
                throw new WSDLException(WSDLException.PARSER_ERROR,
                                        "Parser Configuration Error", e);
            } 
            catch (SAXException e) {
                throw new WSDLException(WSDLException.PARSER_ERROR,
                                        "Parser SAX Error", e);

            } 
            catch (IOException e) {
                throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error",
                                        e);
            }
            return reader.readWSDL(getBaseUri(), doc);
        }
    }


    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }


    private Document newDocument(InputStream inp)
            throws ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory dbf = getDOMFactory();
        DocumentBuilder db;

        synchronized (dbf) {
            try {
                db = dbf.newDocumentBuilder();
            }
            catch (Exception e){
                // Under some peculiar conditions (classloader issues), just scrap the old dbf, create a new one and try again.
                dbf = getDOMFactory();
                db = dbf.newDocumentBuilder();
            }
        }
        db.setEntityResolver(new DefaultEntityResolver());
        db.setErrorHandler(new ParserErrorHandler());
        return (db.parse(inp));
    }

    private DocumentBuilderFactory getDOMFactory() {
        DocumentBuilderFactory dbf;
        try {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        }
        catch (Exception e) {
            //log.error(Messages.getMessage("exception00"), e );
            dbf = null;
        }
        return (dbf);
    }



    //------------------------------------------------------------------------
    // internal class: ParserErrorHandler
    //------------------------------------------------------------------------

    public class ParserErrorHandler implements ErrorHandler {
        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            return "URI=" + systemId +
                    " Line=" + spe.getLineNumber() +
                    ": " + spe.getMessage();
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }

    //------------------------------------------------------------------------
    // internal class: DefaultEntityResolver
    //------------------------------------------------------------------------

    public class DefaultEntityResolver implements org.xml.sax.EntityResolver {

        private String empty = "";
        private ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());

        public DefaultEntityResolver() {
        }

        public InputSource resolveEntity(String publicId, String systemId) {

            return new InputSource(bais);
        }

    }
}
