package org.apache.axis2.jaxws.description.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;

import junit.framework.TestCase;

public class PostRI216MethodRetrieverImplTests extends TestCase {
    
    public void testMethodRetriever(){
        //Create DBC for implicit SEI
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(EchoMessageService.class);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
        assertNotNull(dbcMap);
        DescriptionBuilderComposite dbc = dbcMap.get(EchoMessageService.class.getName());
        //create EndpointDescription
        List<ServiceDescription> serviceDescList =
            DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        ServiceDescription sd = serviceDescList.get(0);
        EndpointInterfaceDescriptionImpl eid = new EndpointInterfaceDescriptionImpl(dbc, new EndpointDescriptionImpl(null, new QName("http://nonanonymous.complextype.test.org","EchoMessagePort"), (ServiceDescriptionImpl)sd));
        //Lets make sure correct MDC's where created.
        List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionsList();
        assertTrue("Expecting 3 methods in MDC found"+mdcList.size(), mdcList.size()==4);
        
        //Lets make sure static and final modifiers are set on MDC as expected.
        for(MethodDescriptionComposite mdc:mdcList){
            if(mdc.getMethodName().contains("staticMethod")){
                assertTrue(mdc.isStatic());
            }
            if(mdc.getMethodName().contains("finalMethod")){
                assertTrue(mdc.isFinal());
            }
        }
        
        //Let make sure static and final methods are not exposed as webservice.
        PostRI216MethodRetrieverImpl mr = new PostRI216MethodRetrieverImpl(dbc, eid);
        Iterator<MethodDescriptionComposite> iter =mr.retrieveMethods();
        List<MethodDescriptionComposite> list = new ArrayList<MethodDescriptionComposite>();
        while(iter.hasNext()){
            list.add(iter.next());
        }
        assertEquals(list.size(), 2);
        MethodDescriptionComposite mdc = list.get(0);
        assertEquals("echoMessage", mdc.getMethodName());
        mdc = list.get(1);
        assertEquals("<init>", mdc.getMethodName());
    }

    @WebService(serviceName = "EchoMessageService", portName = "EchoMessagePort", targetNamespace = "http://nonanonymous.complextype.test.org", wsdlLocation = "")
    public static class EchoMessageService {
        public String echoMessage(String arg) {
            return arg;
        }
        public static String staticMethod(String arg){
            return arg;
        }
        public final String finalMethod(String arg){
            return arg;
        }
    }
}
