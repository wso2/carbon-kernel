/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.registry.core.utils;

import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;

@Deprecated
public class SchemaValidator {

    private static final String XMLSCHEMA_XSD_LOCATION =
            "/org/wso2/carbon/registry/core/utils/XMLSchema.xsd";
    private static final String XSD_VALIDATION_ERROR = "org.wso2.scheam.validationMessage";

    private static final String XSD_STATUS = "org.wso2.wsdl.scheam.status";
    private static final String XSD_VALID = "Schema is valid";
    private static final String XSD_IN_VALID = "Schema is invalid ";
    private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

    /**
     * This will valide the given schema againts the w3c.XMLSchema.
     *
     * @param xsdContent : Input stream representing XSD content
     *
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          : If there is a problem in the XSD then that will throw as the exception
     */
    public void validate(InputStream xsdContent, Resource resource) throws RegistryException {
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(XMLSCHEMA_XSD_LOCATION);
            Source scoure = new SAXSource(reader, new InputSource(in));
            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(xsdContent);
            Schema schema = factory.newSchema(schemaFile);
            // create a Validator instance, which can be used to validate an instance document
            Validator validator = schema.newValidator();
            // validate the DOM tree
            validator.validate(scoure);
            resource.setProperty(XSD_STATUS, XSD_VALID);
        } catch (Exception e) {
            resource.setProperty(XSD_STATUS, XSD_IN_VALID);
            resource.addProperty(XSD_VALIDATION_ERROR, e.getMessage());
            throw new RegistryException(e.getMessage());
        }
    }
}
