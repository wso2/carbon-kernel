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

package org.apache.axis2.jaxws.catalog;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.catalog.impl.OASISCatalogManager;
import org.apache.axis2.jaxws.util.CatalogURIResolver;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Tests the use of the Apache Commons Resolver API to resolve URIs.
 */
public class XMLCatalogTests extends TestCase {
	private static final String TEST_RESOURCES = "test-resources/catalog/";
	private static final String BASIC_CATALOG = "/" + TEST_RESOURCES + "basic-catalog.xml";
	private static final String IMPORT_BASE = TEST_RESOURCES + "importBase.xsd";
	private static final String IMPORT_BAD = TEST_RESOURCES + "importBad.xsd";	

    /**
     * Verify that all the expected conditions are met (the control case).
     * @throws Exception
     */
    public void testSchemaImportNoCatalogNoNeed() throws Exception{
        File file = new File(IMPORT_BASE);
        //create a DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc = documentBuilderFactory.newDocumentBuilder().
                parse(file.toURL().toString());

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(doc,file.toURL().toString(),null);
        assertNotNull(schema);

        assertNotNull(schema.getTypeByName(new QName("http://soapinterop.org/xsd2","SOAPStruct")));
        assertNotNull(schema.getElementByName(new QName("http://soapinterop.org/xsd2","SOAPWrapper")));
    }
    
    /**
     * Verify that the element is not present when using IMPORT_BAD in the 
     * absence of a CatalogManager.
     * @throws Exception
     */
    public void testSchemaImportCatalogNeedNotPresent() throws Exception{
        File file = new File(IMPORT_BAD);
        //create a DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc = documentBuilderFactory.newDocumentBuilder().
                parse(file.toURL().toString());

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(doc,file.toURL().toString(),null);
        assertNotNull(schema);

        assertNotNull(schema.getTypeByName(new QName("http://soapinterop.org/xsd2","SOAPStruct")));
        assertNull(schema.getElementByName(new QName("http://soapinterop.org/xsd2","SOAPWrapper")));
    }
    
    /**
     * Verify that the element is present using IMPORT_BAD if the XML Resolver 
     * is used.  This test is for a simple, single-file catalog.
     * @throws Exception
     */
    public void testSchemaImportBasicCatalog() throws Exception{
		OASISCatalogManager catalogManager = new OASISCatalogManager();
		catalogManager.setCatalogFiles(getURLFromLocation(BASIC_CATALOG).toString());
		
        File file = new File(IMPORT_BAD);
        //create a DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc = documentBuilderFactory.newDocumentBuilder().
                parse(file.toURL().toString());

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        schemaCol.setSchemaResolver(new CatalogURIResolver(catalogManager));
        XmlSchema schema = schemaCol.read(doc,file.toURL().toString(),null);
        assertNotNull(schema);

        assertNotNull(schema.getTypeByName(new QName("http://soapinterop.org/xsd2","SOAPStruct")));
        assertNotNull(schema.getElementByName(new QName("http://soapinterop.org/xsd2","SOAPWrapper")));
    }

    /**
     * Given a String representing a file location, return a URL.
     * @param wsdlLocation
     * @return
     */
    private URL getURLFromLocation(String wsdlLocation) {
        URL url = null;
        try {
            try{
                String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
                wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
            }catch(Exception e){
                e.printStackTrace();
                fail();
            }
               File file = new File(wsdlLocation);
               url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail();
        }
	    
        return url;
    }
}
