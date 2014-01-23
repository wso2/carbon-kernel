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

package org.apache.axis2.dataretrieval.client;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.description.AxisService;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.URL;

public class MexClient extends ServiceClient {

    public MexClient(ConfigurationContext configContext, AxisService axisService)
            throws AxisFault {
        super(configContext, axisService);
    }

    public MexClient(ConfigurationContext configContext,
                     Definition wsdl4jDefinition, QName wsdlServiceName, String portName)
            throws AxisFault {
        super(configContext, wsdl4jDefinition, wsdlServiceName, portName);
    }

    public MexClient(ConfigurationContext configContext, URL wsdlURL,
                     QName wsdlServiceName, String portName) throws AxisFault {
        super(configContext, wsdlURL, wsdlServiceName, portName);
    }

    public MexClient() throws AxisFault {
    }

    /**
     * Builds OMElement that makes up of SOAP body.
     */
    public OMElement setupGetMetadataRequest(String dialect,
                                             String identifier) throws AxisFault {

        // Attempt to engage MEX module
        /*    try{
          super.engageModule("metadataExchange");
       }
       catch (Exception e){
         throw new AxisFault ("Unable to proceed with GetMetadata Request!", e);
       } */

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(DRConstants.SPEC.NS_URI,
                                                 DRConstants.SPEC.NS_PREFIX);

        OMElement method = fac.createOMElement(DRConstants.SPEC.GET_METADATA, omNs);
        if (dialect != null) {
            OMElement dialectElem = fac.createOMElement(DRConstants.SPEC.DIALECT, omNs);

            dialectElem.setText(dialect);
            method.addChild(dialectElem);
        }
        // create Identifier element
        if (identifier != null) {
            OMElement id_Elem = fac.createOMElement(DRConstants.SPEC.IDENTIFIER, omNs);
            id_Elem.setText(identifier);
            method.addChild(id_Elem);
        }
        return method;
    }
}
