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

package org.apache.axis2.rpc;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.integration.RPCLocalTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RPCCallTest extends RPCLocalTestCase {

    private SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //  0123456789 0 123456789

    protected boolean finish = false;

    protected void setUp() throws Exception {
		super.setUp();
		deployClassAsService("EchoXMLService", RPCServiceClass.class);
	}

    public void testEditBean() throws AxisFault {
        RPCServiceClient sender = getRPCClient("EchoXMLService", "editBean");

        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);


        ArrayList args = new ArrayList();
        args.add(bean);
        args.add("159");

        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "editBean", "req"), args.toArray());
        MyBean resBean = (MyBean)BeanUtil.deserialize(MyBean.class, response.getFirstElement(),
                                                      new DefaultObjectSupplier(), null);
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
    }

    public void testEchoBean() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoBean");

        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);

        ArrayList args = new ArrayList();
        args.add(bean);


        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoBean", "req"), args.toArray());
        MyBean resBean = (MyBean)BeanUtil.deserialize(MyBean.class,
                                                      response.getFirstElement(),
                                                      new DefaultObjectSupplier(), null);
//        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserilze();
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 100);
    }

    public void testechoMail() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoMail");

        Mail mail = new Mail();
        mail.setBody("My Body");
        mail.setContentType("ContentType");
        mail.setFrom("From");
        mail.setId("ID");
        mail.setSubject("Subject");
        mail.setTo("To");

        ArrayList args = new ArrayList();
        args.add(mail);


        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoMail", "req"), args.toArray());
        Mail resBean = (Mail)BeanUtil.deserialize(Mail.class, response.getFirstElement(),
                                                  new DefaultObjectSupplier(), null);
//        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserilze();
        assertNotNull(resBean);
        assertEquals(resBean.getBody(), "My Body");
    }


    public void testEchoString() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoString");

        ArrayList args = new ArrayList();
        args.add("foo");
        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoString", "req"), args.toArray());
        assertEquals(response.getFirstElement().getText(), "foo");
    }

    public void testEchoInt() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoInt");

        ArrayList args = new ArrayList();
        args.add("100");

        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoInt", "req"), args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 100);
    }

    public void testAdd() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "add");
    	
        ArrayList args = new ArrayList();
        args.add("100");
        args.add("200");

        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "add", "req"), args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 300);
    }

    public void testByteArray() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "testByteArray");
    	
        ArrayList args = new ArrayList();
        String hello = "hello";
        args.add(hello.getBytes());

        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "testByteArray", "req"), args.toArray());
        assertEquals(response.getFirstElement().getText(), hello);
    }

    //

    public void testDivide() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "divide");

        ArrayList args = new ArrayList();
        args.add("10");
        args.add("0");
        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "divide", "req"), args.toArray());
        assertEquals(response.getFirstElement().getText(), "INF");
    }

    public void testEchoBool() throws AxisFault {
        RPCServiceClient sender = getRPCClient("EchoXMLService", "echoBool");

        ArrayList args = new ArrayList();
        args.add("true");

        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoBool", "req"), args.toArray());
        assertEquals(Boolean.valueOf(response.getFirstElement().getText()).booleanValue(), true);
    }

    public void testEchoByte() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoByte");

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoByte", "req"), args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getText()), 1);
    }

    public void testCompany() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoCompany");

        Company com = new Company();
        com.setName("MyCompany");

        ArrayList ps = new ArrayList();

        Person p1 = new Person();
        p1.setAge(10);
        p1.setName("P1");
        ps.add(p1);

        Person p2 = new Person();
        p2.setAge(15);
        p2.setName("P2");
        ps.add(p2);

        Person p3 = new Person();
        p3.setAge(20);
        p3.setName("P3");
        ps.add(p3);

        com.setPersons(ps);
        ArrayList args = new ArrayList();
        args.add(com);
        sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoCompany", "req"), args.toArray());
    }


    public void testtestCompany() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "testCompanyArray");

        Company com = new Company();
        com.setName("MyCompany");

        ArrayList ps = new ArrayList();

        Person p1 = new Person();
        p1.setAge(10);
        p1.setName("P1");
        ps.add(p1);

        Person p2 = new Person();
        p2.setAge(15);
        p2.setName("P2");
        ps.add(p2);

        Person p3 = new Person();
        p3.setAge(20);
        p3.setName("P3");
        ps.add(p3);

        com.setPersons(ps);
        ArrayList args = new ArrayList();
        args.add(com);
        args.add(com);
        args.add(com);
        args.add(com);

        ArrayList req = new ArrayList();
        req.add(args.toArray());
        OMElement value = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "testCompanyArray", "req"), req.toArray());
        assertEquals("4", value.getFirstElement().getText());
    }

    public void testCompanyArray() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "CompanyArray");

        Company com = new Company();
        com.setName("MyCompany");

        ArrayList ps = new ArrayList();

        Person p1 = new Person();
        p1.setAge(10);
        p1.setName("P1");
        ps.add(p1);

        Person p2 = new Person();
        p2.setAge(15);
        p2.setName("P2");
        ps.add(p2);

        Person p3 = new Person();
        p3.setAge(20);
        p3.setName("P3");
        ps.add(p3);

        com.setPersons(ps);
        ArrayList args = new ArrayList();
        args.add(com);
        args.add(com);
        args.add(com);
        args.add(com);

        ArrayList req = new ArrayList();
        req.add(args.toArray());
        ArrayList resobj = new ArrayList();
        resobj.add(Company.class);
        resobj.add(Company.class);
        resobj.add(Company.class);
        resobj.add(Company.class);
        Object [] value = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "CompanyArray", "req"), req.toArray(),
                                                (Class[])resobj.toArray(new Class[resobj.size()]));
        assertEquals(4, value.length);
        assertEquals(((Company)value[0]).getName(), "MyCompany");
    }


    public void testEchoOM() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoOM");

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoOM", "req"), args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getFirstElement().getText()), 1);
    }

    public void testCalender() throws AxisFault {
        RPCServiceClient sender = getRPCClient("EchoXMLService", "echoCalander");

        ArrayList args = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        args.add(ConverterUtil.convertToString(calendar));
        OMElement response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoCalander", "req"), args.toArray());
        assertEquals(response.getFirstElement().getText(), ConverterUtil.convertToString(calendar));
    }


    ////////////////////////////////////////////////// Invoking by Passing Return types //////////
    public void testechoBean2() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoBean");

        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);

        ArrayList args = new ArrayList();
        args.add(bean);

        ArrayList ret = new ArrayList();
        ret.add(MyBean.class);

        Object [] response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoBean", "req"), args.toArray(),
                                                   (Class[])ret.toArray(new Class[ret.size()]));
        MyBean resBean = (MyBean)response[0];
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 100);
    }

    public void testechoInt2() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "echoInt");

        ArrayList args = new ArrayList();
        args.add("100");

        ArrayList ret = new ArrayList();
        ret.add(Integer.class);

        Object [] response = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "echoInt", "req"), args.toArray(),
                                                   (Class[])ret.toArray(new Class[ret.size()]));
        assertEquals(((Integer)response[0]).intValue(), 100);
    }

//    public void testmultireturn() throws AxisFault {
//        configureSystem("multireturn");
//
//        Options options = new Options();
//        options.setTo(targetEPR);
//        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
//
//        ConfigurationContext configContext =
//                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
//        RPCServiceClient sender = new RPCServiceClient(configContext, null);
//        sender.setOptions(options);
//
//        ArrayList args = new ArrayList();
//        args.add("1");
//
//        ArrayList ret = new ArrayList();
//        ret.add(Integer.class);
//        ret.add(String.class);
//
//        Object [] response = sender.invokeBlocking(operationName, args.toArray(), ret.toArray());
//        assertEquals(((Integer) response[0]).intValue(), 10);
//        assertEquals(response[1], "foo");
//    }

    public void testStringArray() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "handleStringArray");

        ArrayList args = new ArrayList();
        String [] values = new String[] { "abc", "cde", "efg" };
        args.add(values);
        ArrayList ret = new ArrayList();
        ret.add(Boolean.class);
        Object [] objs = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "handleStringArray", "req"), args.toArray(),
                                               (Class[])ret.toArray(new Class[ret.size()]));
        assertNotNull(objs);
        assertEquals(Boolean.TRUE, Boolean.valueOf(objs[0].toString()));
    }

    public void testmultiArrays() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "multiArrays");
    	
        ArrayList args = new ArrayList();
        String [] values = new String[] { "abc", "cde", "efg" };
        args.add(values);

        String [] values2 = new String[] { "abc", "cde", "efg" };
        args.add(values2);
        String [] values3 = new String[] { "abc", "cde", "efg" };
        args.add(values3);
        args.add("10");

        ArrayList ret = new ArrayList();
        ret.add(Integer.class);
        Object [] objs = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "multiArrays", "req"), args.toArray(),
                                               (Class[])ret.toArray(new Class[ret.size()]));
        assertNotNull(objs);
        assertEquals(19, Integer.parseInt(objs[0].toString()));
    }

    public void testmulReturn() throws AxisFault {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "mulReturn");

        ArrayList args = new ArrayList();
        args.add("foo");


        OMElement element = sender.invokeBlocking(new QName("http://rpc.axis2.apache.org", "mulReturn", "req"), args.toArray());
        System.out.println("element = " + element);
//        assertEquals(response.getFirstElement().getText(), "foo");
    }


    public void testhandleArrayList() throws Exception {
    	RPCServiceClient sender = getRPCClient("EchoXMLService", "handleArrayList");

        OMElement elem = sender.sendReceive(getPayload());
        assertEquals(elem.getFirstElement().getText(), "abcdefghiklm10");
    }

    public void testomElementArray() throws Exception {
        RPCServiceClient sender = getRPCClient("EchoXMLService", "omElementArray");
        
        String str = "<req:omElementArray xmlns:req=\"http://rpc.axis2.apache.org\">\n" +
                "    <arg0><abc>vaue1</abc></arg0>\n" +
                "    <arg0><abc>vaue2</abc></arg0>\n" +
                "    <arg0><abc>vaue3</abc></arg0>\n" +
                "    <arg0><abc>vaue4</abc></arg0>\n" +
                "</req:omElementArray>";
        StAXOMBuilder staxOMBuilder;
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new
                ByteArrayInputStream(str.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        staxOMBuilder = new StAXOMBuilder(fac, xmlReader);

        OMElement elem = sender.sendReceive(staxOMBuilder.getDocumentElement());
        assertEquals("4", elem.getFirstElement().getText());
    }

    private OMElement getPayload() throws Exception {
        String str = "<req:handleArrayList xmlns:req=\"http://rpc.axis2.apache.org\">\n" +
                "  <arg0>\n" +
                "    <item0>abc</item0>\n" +
                "    <item0>def</item0>\n" +
                "    <item0>ghi</item0>\n" +
                "    <item0>klm</item0>\n" +
                "  </arg0><arg1>10</arg1>" +
                "</req:handleArrayList>";
        StAXOMBuilder staxOMBuilder;
        XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(
                new ByteArrayInputStream(str.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        staxOMBuilder = new StAXOMBuilder(fac, xmlReader);
        return staxOMBuilder.getDocumentElement();
    }


}
