/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.context.externalize;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAllAxisServicesBuilder;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 * Validate the deserialization activation methods
 */
public class ActivateUtilsTest extends TestCase {
    /**
     * Try to find an AxisService in which the name matches and extraName is null.  This simulates
     * deserializaing and activating an older version a MessageContext in which the extraName
     * on the AxisService was not specified. 
     * @throws AxisFault 
     */
    public void testFindServiceNoExtraNameFound() throws AxisFault {
        AxisConfiguration axisCfg = new AxisConfiguration();

        AxisService svc1 = new AxisService();
        QName svc1QN = new QName("http://service.name/space/1", "service1");
        String portName = "port1";
        setupAxisService(svc1, svc1QN, portName);
        axisCfg.addService(svc1);
        
        AxisService foundService = ActivateUtils.findService(axisCfg, AxisService.class.getName(), 
                generateAxisServiceName(svc1QN, portName), null);
        assertSame("Did not find expected AxisService using null extraName", svc1, foundService);
        
    }
    
    /**
     * Test that with no extra name information, an axis service can not be found if the names
     * don't match
     * @throws AxisFault 
     */
    public void testFindServiceNoExtraNameNotFound() throws AxisFault {
        AxisConfiguration axisCfg = new AxisConfiguration();

        AxisService svc1 = new AxisService();
        QName svc1QN = new QName("http://service.name/space/1", "service1");
        String portName = "port1";
        setupAxisService(svc1, svc1QN, portName);
        axisCfg.addService(svc1);
        
        AxisService foundService = ActivateUtils.findService(axisCfg, AxisService.class.getName(), 
                generateAxisServiceName(svc1QN, portName + "_NoMatch"), null);
        assertNull("Should not have found a matching AxisService", foundService);
        
    }
    
    /**
     * Test that with extra name information, an axis service can be found even if the 
     * AxisService names don't match
     * @throws AxisFault 
     */
    public void testFindServiceWithExtraName() throws AxisFault {
        AxisConfiguration axisCfg = new AxisConfiguration();

        AxisService svc1 = new AxisService();
        QName svc1QN = new QName("http://service.name/space/1", "service1");
        String portName = "port1";
        setupAxisService(svc1, svc1QN, portName);
        axisCfg.addService(svc1);
        
        String extraName = ActivateUtils.getAxisServiceExternalizeExtraName(svc1);
        AxisService foundService = ActivateUtils.findService(axisCfg, AxisService.class.getName(), 
                generateAxisServiceName(svc1QN, portName + "_NoMatch"), extraName);
        assertSame("Did not find expected AxisService using null extraName", svc1, foundService);
        
    }
    
    /**
     * Test that with extra name information that does not match the service, an axis service 
     * is not found even if the service names DO match.  This test makes sure an AxisService that 
     * happens to have the same name as a previously serialized one BUT is actually different since
     * the Service QNames and Port Names are different is not found.
     * @throws AxisFault 
     */
    public void testFindServiceWithExtraNameMistmatchNotFound() throws AxisFault {
        AxisConfiguration axisCfg = new AxisConfiguration();

        AxisService svc1 = new AxisService();
        QName svc1QN = new QName("http://service.name/space/1", "service1");
        String portName = "port1";
        setupAxisService(svc1, svc1QN, portName);
        axisCfg.addService(svc1);
        
        String extraName = ActivateUtils.getAxisServiceExternalizeExtraName(svc1) + "_NoMatch";
        AxisService foundService = ActivateUtils.findService(axisCfg, AxisService.class.getName(), 
                generateAxisServiceName(svc1QN, portName), extraName);
        assertNull("Should not have found matching service without matching extraname", foundService);
    }
    
    /**
     * Test that with extra name information but no Parameters on the AxisService that the  
     * service is found. 
     * @throws AxisFault 
     */
    public void testFindServiceWithNoParametersFound() throws AxisFault {
        AxisConfiguration axisCfg = new AxisConfiguration();

        AxisService svc1 = new AxisService();
        QName svc1QN = new QName("http://service.name/space/1", "service1");
        String portName = "port1";
        // We don't use the setup method since we do not want the parameters set on this
        // service
        // setupAxisService(svc1, svc1QN, portName);
        svc1.setName(generateAxisServiceName(svc1QN, portName));
        axisCfg.addService(svc1);
        
        String extraName = ActivateUtils.getAxisServiceExternalizeExtraName(svc1) + "_NoMatch";
        AxisService foundService = ActivateUtils.findService(axisCfg, AxisService.class.getName(), 
                generateAxisServiceName(svc1QN, portName), extraName);
        assertSame("Should have found matching service without matching extraname", svc1, foundService);
    }
    

    private void setupAxisService(AxisService axisService, QName serviceQN, String portName) throws AxisFault {
        axisService.setName(generateAxisServiceName(serviceQN, portName));
        axisService.addParameter(WSDL11ToAllAxisServicesBuilder.WSDL_SERVICE_QNAME, serviceQN);
        axisService.addParameter(WSDL11ToAllAxisServicesBuilder.WSDL_PORT, portName);
    }

    private String generateAxisServiceName(QName serviceQN, String portName) {
        return serviceQN.getLocalPart() + "." + portName;
    }
}
