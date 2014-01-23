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

package org.apache.axis2.rmi.databind;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.databind.dto.Activate;
import org.apache.axis2.rmi.databind.dto.CancelWorkflowRequest;
import org.apache.axis2.rmi.exception.OMElementCreationException;
import org.apache.axis2.rmi.metadata.Parameter;


public class OMElementCreatorTest extends TestCase {

     public void testCreateActivateObject(){
        Activate activate = new Activate();
        CancelWorkflowRequest cancelWorkflowRequest = new CancelWorkflowRequest();
        cancelWorkflowRequest.setIWFID(5);
        cancelWorkflowRequest.setRestart(10);

        activate.setCancelWorkflowRequest(cancelWorkflowRequest);

        Configurator configurator = new Configurator();

        configurator.addPackageToNamespaceMaping("org.apache.axis2.rmi.databind.dto",
                "http://phoenix.esb.infrastructure.biztalk.schemas.cancelworkflowrequest/");

        Parameter parameter = new Parameter(Activate.class, "Activate");
        parameter.setNamespace("http://phoenix.co.il/ESB/Infrastructure");

        try {
            OMElementCreator omElementCreator = new OMElementCreator();
            OMElement omElement = omElementCreator.getOMElement(activate,parameter,configurator);
            System.out.println("OM Element ==> " + omElement.toString());
        } catch (OMElementCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
     }
}
