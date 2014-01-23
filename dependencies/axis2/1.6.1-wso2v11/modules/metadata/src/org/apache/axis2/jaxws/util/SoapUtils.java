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

package org.apache.axis2.jaxws.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;

import javax.xml.stream.XMLStreamReader;

public class SoapUtils {


    public static OMElement getOMElement(SOAPEnvelope response) {
        XMLStreamReader parser = response.getXMLStreamReader();

        StAXOMBuilder builder =
                new StAXOMBuilder(OMAbstractFactory.getOMFactory(), parser);

        return builder.getDocumentElement();

    }

    public static SOAPFactory getSoapFactory(String soapVersionURI) {
        if ("http://www.w3.org/2003/05/soap-envelope".equals(soapVersionURI))
            return OMAbstractFactory.getSOAP12Factory();
        else
            return OMAbstractFactory.getSOAP11Factory();
    }

}
