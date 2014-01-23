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

package org.apache.axis2.spring;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.extensions.spring.receivers.SpringAppContextAwareObjectSupplier;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import java.io.StringWriter;

public class SpringServiceTest extends UtilServerBasedTestCase {

    protected QName transportName = new QName("http://springExample.org/example1",
                                              "NullTransport");
    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                    //  + "/axis2/services/EchoXMLService/echoOMElement");
                    + "/axis2/services/SpringExample/getValue");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    private QName springServiceName = new QName("SpringExample");

    private QName springOperationName = new QName("getValue");

    public static Test suite() {
        return getTestSetup(new TestSuite(SpringServiceTest.class));
    }

    protected void setUp() throws Exception {

        AxisService service =
                createSpringService(springServiceName, new RawXMLINOutMessageReceiver(),
                                    "org.apache.axis2.extensions.spring.receivers.SpringAppContextAwareObjectSupplier",
                                    "springAwareService",
                                    springOperationName);
        createSpringAppCtx(service.getClassLoader());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(springServiceName);
        UtilServer.unDeployClientService();
    }

    // Can't test SpringServletContextSupplier AFAIK cuz not sure how to get ServletContext
    // with this test harness. The idea here then is to test the basic idea with the 
    // alternative method, SpringAppContextAwareObjectSupplier, whose purpose is to
    // run in a non-servlet container environment
    public void testSpringAppContextAwareObjectSupplier() throws Exception {

        AxisService clientService =
                createSpringServiceforClient(springServiceName, new RawXMLINOutMessageReceiver(),
                                             "org.apache.axis2.extensions.spring.receivers.SpringAppContextAwareObjectSupplier",
                                             "springAwareService",
                                             springOperationName);
        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();
        ServiceClient sender = new ServiceClient(configcontext, clientService);

        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = factory.createOMNamespace(
                "http://springExample.org/example1", "example1");

        OMElement method = factory.createOMElement("getValue", omNs);
        OMElement value = factory.createOMElement("Text", omNs);
        value.addChild(factory.createOMText(value, "Test String "));
        method.addChild(value);

        Options options = new Options();
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setTo(targetEPR);
        options.setAction(springOperationName.getLocalPart());
        sender.setOptions(options);

        OMElement result = sender.sendReceive(springOperationName, method);

        StringWriter writer = new StringWriter();
        result.serialize(XMLOutputFactory.newInstance()
                .createXMLStreamWriter(writer));
        writer.flush();
        String testStr = writer.toString();
        // write to report
        System.out.println("\ntestSpringAppContextAwareObjectSupplier result: " + testStr);
        assertNotSame(new Integer(testStr.indexOf("emerge thyself")), new Integer(-1));
    }

    private AxisService createSpringService(QName springServiceName,
                                            MessageReceiver messageReceiver, String supplierName,
                                            String beanName, QName opName) throws AxisFault {

        AxisService service = new AxisService(springServiceName.getLocalPart());

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_OBJECT_SUPPLIER, supplierName));
        service.addParameter(new Parameter(Constants.SERVICE_TCCL, Constants.TCCL_COMPOSITE));
        service.addParameter(new Parameter(
                SpringAppContextAwareObjectSupplier.SERVICE_SPRING_BEANNAME, beanName));

        AxisOperation axisOp = new InOutAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + opName.getLocalPart(),
                                     axisOp);

        return service;
    }

    public AxisService createSpringServiceforClient(QName springServiceName,
                                                    MessageReceiver messageReceiver,
                                                    String supplierName,
                                                    String beanName,
                                                    QName opName)
            throws AxisFault {
        AxisService service = new AxisService(springServiceName.getLocalPart());

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_OBJECT_SUPPLIER, supplierName));
        service.addParameter(new Parameter(Constants.SERVICE_TCCL, Constants.TCCL_COMPOSITE));
        service.addParameter(new Parameter(
                SpringAppContextAwareObjectSupplier.SERVICE_SPRING_BEANNAME, beanName));

        AxisOperation axisOp = new OutInAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public void createSpringAppCtx(ClassLoader cl)
            throws Exception {

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "/spring/applicationContext.xml" }, false);
        ctx.setClassLoader(cl);
        ctx.refresh();
    }
}
