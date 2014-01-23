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

import org.apache.axiom.om.OMNode;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Axis 2 Data Locator responsibles for retrieving Schema metadata. The class is
 * created as model for schema specific data locator; and also easier for any
 * future implementation schema specific data retrieval logic.
 */

public class SchemaDataLocator extends BaseAxisDataLocator implements
        AxisDataLocator {

    private String requestIdentifier = null;

    private String serviceEPR = null;

    /**
     * 
     */
    private static final Log LOG = LogFactory.getLog(SchemaDataLocator.class
            .getClass().getName());

    protected SchemaDataLocator() {

    }

    /**
     * Constructor
     */
    protected SchemaDataLocator(ServiceData[] data) {
        dataList = data;
    }

    public Data[] getData(DataRetrievalRequest request,
            MessageContext msgContext) throws DataRetrievalException {

        requestIdentifier = request.getIdentifier();
        serviceEPR = msgContext.getTo().getAddress();

        OutputForm outputForm = request.getOutputForm();
        if (outputForm == null) {
            outputForm = OutputForm.INLINE_FORM;
        }

        Data[] data;

        if (outputForm == OutputForm.INLINE_FORM) {
            data = outputInlineForm(msgContext, dataList);

        } else if (outputForm == OutputForm.LOCATION_FORM) {
            data = outputLocationForm(dataList);

        } else {
            data = outputReferenceForm(msgContext, dataList);
        }

        return data;
    }

    protected Data[] outputInlineForm(MessageContext msgContext,
            ServiceData[] serviceData) throws DataRetrievalException {

        Data[] data = super.outputInlineForm(msgContext, serviceData);

        if (data.length != 0) {
            return data;
        }

        AxisService axisService = msgContext.getAxisService();
        ArrayList schemaList = axisService.getSchema();

        ArrayList results = new ArrayList();
        XmlSchema schema;

        for (Iterator iterator = schemaList.iterator(); iterator.hasNext();) {
            schema = (XmlSchema) iterator.next();

            if (requestIdentifier != null) {
                if (requestIdentifier.equals(schema.getTargetNamespace())) {
                    results.add(new Data(convertToOM(schema), requestIdentifier));
                }
            } else {
                results.add(new Data(convertToOM(schema), null));
            }
        }

        return (Data[]) results.toArray(new Data[results.size()]);
    }

    protected Data[] outputLocationForm(ServiceData[] serviceData)
            throws DataRetrievalException {

        Data[] data = super.outputLocationForm(serviceData);

        if (data != null && data.length != 0) {
            return data;
        }
        return new Data[] { new Data(serviceEPR + "?xsd", requestIdentifier) };
    }

    private OMNode convertToOM(XmlSchema schema) throws DataRetrievalException {
        StringWriter writer = new StringWriter();
        schema.write(writer);

        StringReader reader = new StringReader(writer.toString());
        try {
            return XMLUtils.toOM(reader);
        } catch (XMLStreamException e) {
            throw new DataRetrievalException(
                    "Can't convert XmlSchema object to an OMElement", e);
        }
    }
}
