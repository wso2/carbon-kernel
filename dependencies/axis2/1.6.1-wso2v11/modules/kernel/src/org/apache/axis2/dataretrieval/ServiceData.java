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

package org.apache.axis2.dataretrieval;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

/**
 * This represents the service data for a dialect and identifier if specified.
 * Basically, the Data element defined in the ServiceData.xml packaged in
 * a Web Service's achieve file.
 */

public class ServiceData {
    OMElement data;

    String identifier;

    String dialect;

    String fileName;

    /**
     * Constructor
     *
     * @param in_data an Data element in the ServiceData.
     */

    public ServiceData(OMElement in_data) {
        data = in_data;
        identifier = getAttributeValue(DRConstants.SERVICE_DATA.IDENTIFIER);
        dialect = getAttributeValue(DRConstants.SERVICE_DATA.DIALECT);
        fileName = getAttributeValue(DRConstants.SERVICE_DATA.FILE);
    }

    public String getAttributeValue(String qName) {

        String value = null;
        OMAttribute attribute = data.getAttribute(new QName(qName));
        if (attribute != null) {
            value = attribute.getAttributeValue();
        }

        return value;

    }

    // return identifier for this Data element
    public String getIdentifier() {
        return identifier;
    }

    // return dialect for this Data element
    public String getDialect() {
        return dialect;
    }

    // return the Data ELement
    public OMElement getOMData() {
        return data;
    }


    // Get URL from data Element
    public String getURL() {

        String urlValue = null;
        OMElement url = data.getFirstChildWithName(new QName(
                DRConstants.SERVICE_DATA.URL));
        if (url != null) {
            urlValue = url.getText();
        }

        return urlValue;
    }

    // Get ENDPOINT_REFERENCE from Data Element
    public OMElement getEndpointReference() {
        OMElement epr = data.getFirstChildWithName(new QName(
                DRConstants.SERVICE_DATA.ENDPOINT_REFERENCE));
        return epr;
    }

    // Load the file content of the file specified in the file attribute
    // in the data element.
    public OMElement getFileContent(ClassLoader classloader)
            throws DataRetrievalException {

        OMElement metaElement = null;
        if (fileName != null) {
            DataRetrievalUtil util = DataRetrievalUtil.getInstance();

            metaElement = util.buildOM(classloader, fileName);
        }
        return metaElement;
    }
}
