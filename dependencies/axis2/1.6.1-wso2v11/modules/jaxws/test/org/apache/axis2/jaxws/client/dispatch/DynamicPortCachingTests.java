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
package org.apache.axis2.jaxws.client.dispatch;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.spi.ClientMetadataTest;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests the caching and isolation of dynamic ports,i.e. those created with
 * Service.addPort(...).  Dynamic ports should
 * 1) Only be visible to services on which an addPort was done
 * 2) Share instances of the description objects (e.g. AxisService) for ports
 * added to different instances of the same service that use the same client
 * configuration
 * 3) Identical ports on services using different client configuration should
 * not be shared 
 */
public class DynamicPortCachingTests extends TestCase {
    static final String namespaceURI = "http://dispatch.client.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    
    static final String dynamicPort1 = "dynamicPort1";
    static final String bindingID1 = null;
    static final String epr1 = null;

    /**
     * Two different instances of the same service should share the same
     * description information (e.g. AxisService) if the same port is added
     * to both 
     */
    public void _testSamePortsSameService() {
        try {
            ClientMetadataTest.installCachingFactory();
            QName svcQN = new QName(namespaceURI, svcLocalPart);
            
            Service svc1 = Service.create(svcQN);
            assertNotNull(svc1);
            ServiceDelegate svcDlg1 = DescriptionTestUtils2.getServiceDelegate(svc1);
            assertNotNull(svcDlg1);
            ServiceDescription svcDesc1 = svcDlg1.getServiceDescription();
            assertNotNull(svcDesc1);

            Service svc2 = Service.create(svcQN);
            assertNotNull(svc2);
            ServiceDelegate svcDlg2 = DescriptionTestUtils2.getServiceDelegate(svc2);
            assertNotNull(svcDlg2);
            ServiceDescription svcDesc2 = svcDlg2.getServiceDescription();
            assertNotNull(svcDesc2);
            
            assertNotSame("Service instances should not be the same", svc1, svc2);
            assertNotSame("Service delegates should not be the same", svcDlg1, svcDlg2);
            assertSame("Instance of ServiceDescription should be the same", svcDesc1, svcDesc2);
            
            // Add a port to 1st service, should not be visible under the 2nd service
            svc1.addPort(new QName(namespaceURI, dynamicPort1),
                         bindingID1,
                         epr1);
            assertEquals(1, getList(svc1.getPorts()).size());
            assertEquals(0, getList(svc2.getPorts()).size());
            
            // Add the same port to 2nd service, should now have same ports and description
            // objects
            svc2.addPort(new QName(namespaceURI, dynamicPort1),
                         bindingID1,
                         epr1);
            assertEquals(1, getList(svc1.getPorts()).size());
            assertEquals(1, getList(svc2.getPorts()).size());
            
            
            
            
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
        
    }
    
    public void testAddPortOOM() {
        QName svcQN = new QName(namespaceURI, svcLocalPart);
        try {
            ClientMetadataTest.installCachingFactory();
            for (int i = 0; i < 5000 ; i++) {
                Service svc1 = Service.create(svcQN);
                svc1.addPort(new QName(namespaceURI, dynamicPort1 + "_" /*+ i*/),
                             bindingID1,
                             epr1);
            }
        } catch (Throwable t) {
            fail("Caught throwable " + t);
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    private List getList(Iterator it) {
        List returnList = new ArrayList();
        while (it != null && it.hasNext()) {
            returnList.add(it.next());
        }
        return returnList;
    }
}