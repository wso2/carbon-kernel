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
 */package org.apache.axis2.jaxws.swamtom;

import org.apache.axis2.jaxws.framework.AbstractTestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests calling an endpoint that has one operation that
 * uses an MTOM attachment and another operation that uses a SWA
 * attachment.
 */
public class SWAMTOMTests extends AbstractTestCase {
    private static final QName QNAME_SERVICE = new QName("http://swamtomservice.test.org", 
                                                         "SWAMTOMService");
    private static final QName QNAME_PORT    = new QName("http://swamtomservice.test.org", 
                                                         "SWAMTOMPortTypePort");
    private static final String URL_ENDPOINT = "http://localhost:6060/axis2/services/SWAMTOMService.SWAMTOMPortTypePort";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(SWAMTOMTests.class));
    }
    
    public Dispatch<SOAPMessage> getDispatch(String soapAction) {
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING, URL_ENDPOINT);
        Dispatch<SOAPMessage> dispatch = service.createDispatch(QNAME_PORT,
                                                                SOAPMessage.class,
                                                                Service.Mode.MESSAGE);
        BindingProvider p = (BindingProvider) dispatch;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, URL_ENDPOINT);
        p.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        
        return dispatch;
    }
    
    /**
     * Tests calling an endpoint that understands SOAP 1.1 MTOM
     * However we are calling an operation on the endpoint that 
     * pass a single SWA attachment as a request and expects a 
     * single SWA attachment as a response.  (The body contents are empty.)
     * 
     * This test accomplishes the following:
     *   - ensures that a SWA attachment (not an MTOM attachment) is sent.
     *   - ensures that a SWA attachment can be sent and returned from 
     *     an endpoint that has MTOM enabled.
     *   - ensures that the DBC correctly marks the operation and parameters
     *     such that all of the parameters to this operation are mapped
     *     to SWA attachments.
     *   - ensures that the marshalling code can handle the case of an 
     *     empty soap body and all communication via SWA attachments.
     *     
     * This test ensures that the toleration of the attachments with legacy (pre WS-I 1.0) content ids.
     * @throws Exception
     */
    public void testSWAAttachments_LegacyContentID() throws Exception {
        String soapAction = "swaAttachment";
        Dispatch<SOAPMessage> dispatch = getDispatch(soapAction);

        // Obtain a preconfigured SAAJ MessageFactory
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage();

        // soap:body should be empty
        SOAPBody body = request.getSOAPBody();

        // add attachment
        int attachmentSize = 100;
        byte[] bytes = new byte[attachmentSize];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ('a' + ('z' - 'a') * Math.random());
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        AttachmentPart attachment = request.createAttachmentPart();
        // Per WSI Spect, the swa content id must start with the part name
        //http://www.ws-i.org/Profiles/AttachmentsProfile-1.0.html#Value-space_of_Content-Id_Header
        attachment.setContentId("SWAMTOMTestSOAPMessage");
        attachment.addMimeHeader("FVT-source", "STR_BODY_ELEMENT_1");
        attachment.setRawContent(bais, "text/plain");
        request.addAttachmentPart(attachment);

        // invoke
        SOAPMessage reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        Iterator it = reply.getAttachments();
        AttachmentPart ap = null;

        // verify that the attachment is not null or empty
        if ((ap = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        

        // verify that the attachment is not null or empty
        if (it.hasNext()){
            fail("Detected more then 1 attachment");
        }

        SOAPBody sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        bytes = ap.getRawContentBytes();
        if (bytes.length == 0) { 
            fail("Attachment is empty"); 
        }

        // verify that endpoint has been able to modify the attachment
        if (bytes[0] != 'S' || bytes[1] != 'W' || bytes[2] != 'A') { 
            fail("Did not receive a modified attachment"); 
        }
        
        
        // Invoke a second time to verify

        // invoke
        reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        it = reply.getAttachments();
        ap = null;

        // verify that the attachment is not null or empty
        if ((ap = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        

        // verify that the attachment is not null or empty
        if (it.hasNext()){
            fail("Detected more then 1 attachment");
        }

        sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        bytes = ap.getRawContentBytes();
        if (bytes.length == 0) { 
            fail("Attachment is empty"); 
        }

        // verify that endpoint has been able to modify the attachment
        if (bytes[0] != 'S' || bytes[1] != 'W' || bytes[2] != 'A') { 
            fail("Did not receive a modified attachment"); 
        }
    }
    
    /**
     * Tests calling an endpoint that understands SOAP 1.1 MTOM
     * However we are calling an operation on the endpoint that 
     * pass a single SWA attachment as a request and expects a 
     * single SWA attachment as a response.  (The body contents are empty.)
     * 
     * This test accomplishes the following:
     *   - ensures that a SWA attachment (not an MTOM attachment) is sent.
     *   - ensures that a SWA attachment can be sent and returned from 
     *     an endpoint that has MTOM enabled.
     *   - ensures that the DBC correctly marks the operation and parameters
     *     such that all of the parameters to this operation are mapped
     *     to SWA attachments.
     *   - ensures that the marshalling code can handle the case of an 
     *     empty soap body and all communication via SWA attachments.
     *     
     * This test ensures that the endpoint can receive and return compliant (pre WS-I 1.0) content ids.
     * @throws Exception
     */
    public void testSWAAttachments_WSI() throws Exception {
        String soapAction = "swaAttachment";
        Dispatch<SOAPMessage> dispatch = getDispatch(soapAction);

        // Obtain a preconfigured SAAJ MessageFactory
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage();

        // soap:body should be empty
        SOAPBody body = request.getSOAPBody();

        // add attachment
        int attachmentSize = 100;
        byte[] bytes = new byte[attachmentSize];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ('a' + ('z' - 'a') * Math.random());
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        AttachmentPart attachment = request.createAttachmentPart();
        // Per WSI Spect, the swa content id must start with the part name
        //http://www.ws-i.org/Profiles/AttachmentsProfile-1.0.html#Value-space_of_Content-Id_Header
        attachment.setContentId("jpegImageRequest=SWAMTOMTestSOAPMessage");
        attachment.addMimeHeader("FVT-source", "STR_BODY_ELEMENT_1");
        attachment.setRawContent(bais, "text/plain");
        request.addAttachmentPart(attachment);

        // invoke
        SOAPMessage reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        Iterator it = reply.getAttachments();
        AttachmentPart ap = null;

        // verify that the attachment is not null or empty
        if ((ap = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap.getContentId().startsWith("jpegImageResponse=")) {
            fail("Expected content id to start with jpegImageResponse");
        }

        // verify that the attachment is not null or empty
        if (it.hasNext()){
            fail("Detected more then 1 attachment");
        }

        SOAPBody sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        bytes = ap.getRawContentBytes();
        if (bytes.length == 0) { 
            fail("Attachment is empty"); 
        }

        // verify that endpoint has been able to modify the attachment
        if (bytes[0] != 'S' || bytes[1] != 'W' || bytes[2] != 'A') { 
            fail("Did not receive a modified attachment"); 
        }
        
        // Invoke a second time to verify

        // invoke
        reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        it = reply.getAttachments();
        ap = null;

        // verify that the attachment is not null or empty
        if ((ap = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap.getContentId().startsWith("jpegImageResponse=")) {
            fail("Expected content id to start with jpegImageResponse");
        }

        // verify that the attachment is not null or empty
        if (it.hasNext()){
            fail("Detected more then 1 attachment");
        }

        sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        bytes = ap.getRawContentBytes();
        if (bytes.length == 0) { 
            fail("Attachment is empty"); 
        }

        // verify that endpoint has been able to modify the attachment
        if (bytes[0] != 'S' || bytes[1] != 'W' || bytes[2] != 'A') { 
            fail("Did not receive a modified attachment"); 
        }
    }
    
    /**
     * Now invoke the message the a receives and returns 2 attachments
     * 
     * @throws Exception
     */
    public void testSWAAttachments2_WSI() throws Exception {
        String soapAction = "swaAttachment2";
        Dispatch<SOAPMessage> dispatch = getDispatch(soapAction);

        // Obtain a preconfigured SAAJ MessageFactory
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage();

        // soap:body should be empty
        SOAPBody body = request.getSOAPBody();

        // create attachments 1 and 2
        byte[] bytes1 = new byte[1];
        bytes1[0] = '1';
        byte[] bytes2 = new byte[1];
        bytes2[0] = '2';

        // The attachments are sent out of order.
        // The receiver should be smart enough to use the content id
        // to establish the correct order
        ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes2);
        AttachmentPart attachment2 = request.createAttachmentPart();
        attachment2.setContentId("jpegImage2Request=SWAMTOMTestSOAPMessage");
        attachment2.addMimeHeader("FVT-source", "STR_BODY_ELEMENT_1");
        attachment2.setRawContent(bais2, "text/plain");
        request.addAttachmentPart(attachment2);
        
        ByteArrayInputStream bais1 = new ByteArrayInputStream(bytes1);
        AttachmentPart attachment1 = request.createAttachmentPart();
        attachment1.setContentId("jpegImage1Request=SWAMTOMTestSOAPMessage");
        attachment1.addMimeHeader("FVT-source", "STR_BODY_ELEMENT_1");
        attachment1.setRawContent(bais1, "text/plain");
        request.addAttachmentPart(attachment1);

        // invoke
        SOAPMessage reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        Iterator it = reply.getAttachments();
        AttachmentPart ap3 = null;

        // verify that the attachment is not null or empty
        if ((ap3 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap3.getContentId().startsWith("jpegImage1Response=")) {
            fail("Expected content id to start with jpegImage1Response");
        }
        
        AttachmentPart ap4 = null;
        // verify that the second attachment is not null or empty
        if ((ap4 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap4.getContentId().startsWith("jpegImage2Response=")) {
            fail("Expected content id to start with jpegImage2Response");
        }

        if (it.hasNext()){
            fail("Detected more then 2 attachment");
        }

        SOAPBody sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        byte[] bytes3 = ap3.getRawContentBytes();
        if (bytes3.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes3[0] != '3') { 
            fail("The response attachment is not correct"); 
        }
        
        byte[] bytes4 = ap4.getRawContentBytes();
        if (bytes4.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes4[0] != '4') { 
            fail("The response attachment is not correct"); 
        }
        
        
        // Invoke a second time to verify
        reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        it = reply.getAttachments();
        ap3 = null;

        // verify that the attachment is not null or empty
        if ((ap3 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap3.getContentId().startsWith("jpegImage1Response=")) {
            fail("Expected content id to start with jpegImage1Response");
        }
        
        ap4 = null;
        // verify that the second attachment is not null or empty
        if ((ap4 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap4.getContentId().startsWith("jpegImage2Response=")) {
            fail("Expected content id to start with jpegImage2Response");
        }

        if (it.hasNext()){
            fail("Detected more then 2 attachment");
        }

        sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        bytes3 = ap3.getRawContentBytes();
        if (bytes3.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes3[0] != '3') { 
            fail("The response attachment is not correct"); 
        }
        
        bytes4 = ap4.getRawContentBytes();
        if (bytes4.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes4[0] != '4') { 
            fail("The response attachment is not correct"); 
        }
    }
    
    /**
     * Now invoke the message the a receives and returns 2 attachments,
     * but the request attachments don't comply with wsi
     * 
     * @throws Exception
     */
    public void testSWAAttachments2_Legacy() throws Exception {
        String soapAction = "swaAttachment2";
        Dispatch<SOAPMessage> dispatch = getDispatch(soapAction);

        // Obtain a preconfigured SAAJ MessageFactory
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage();

        // soap:body should be empty
        SOAPBody body = request.getSOAPBody();

        // create attachments 1 and 2
        byte[] bytes1 = new byte[1];
        bytes1[0] = '1';
        byte[] bytes2 = new byte[1];
        bytes2[0] = '2';

        
        
        
        ByteArrayInputStream bais1 = new ByteArrayInputStream(bytes1);
        AttachmentPart attachment1 = request.createAttachmentPart();
        attachment1.setContentId("notCompliant1SWAMTOMTestSOAPMessage");
        attachment1.addMimeHeader("FVT-source", "STR_BODY_ELEMENT_1");
        attachment1.setRawContent(bais1, "text/plain");
        request.addAttachmentPart(attachment1);
        
        ByteArrayInputStream bais2 = new ByteArrayInputStream(bytes2);
        AttachmentPart attachment2 = request.createAttachmentPart();
        attachment2.setContentId("notCompliant2SWAMTOMTestSOAPMessage");
        attachment2.addMimeHeader("FVT-source", "STR_BODY_ELEMENT_1");
        attachment2.setRawContent(bais2, "text/plain");
        request.addAttachmentPart(attachment2);

        // invoke
        SOAPMessage reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        Iterator it = reply.getAttachments();
        AttachmentPart ap3 = null;

        // verify that the attachment is not null or empty
        if ((ap3 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap3.getContentId().startsWith("jpegImage1Response=")) {
            fail("Expected content id to start with jpegImage1Response");
        }
        
        AttachmentPart ap4 = null;
        // verify that the second attachment is not null or empty
        if ((ap4 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap4.getContentId().startsWith("jpegImage2Response=")) {
            fail("Expected content id to start with jpegImage2Response");
        }

        if (it.hasNext()){
            fail("Detected more then 2 attachment");
        }

        SOAPBody sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        byte[] bytes3 = ap3.getRawContentBytes();
        if (bytes3.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes3[0] != '3') { 
            fail("The response attachment is not correct"); 
        }
        
        byte[] bytes4 = ap4.getRawContentBytes();
        if (bytes4.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes4[0] != '4') { 
            fail("The response attachment is not correct"); 
        }
        
        // Invoke a second time to verify
        
        // invoke
        reply = dispatch.invoke(request);

        // iterate over the attachments, there should only be one
        it = reply.getAttachments();
        ap3 = null;

        // verify that the attachment is not null or empty
        if ((ap3 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap3.getContentId().startsWith("jpegImage1Response=")) {
            fail("Expected content id to start with jpegImage1Response");
        }
        
        ap4 = null;
        // verify that the second attachment is not null or empty
        if ((ap4 = (AttachmentPart) it.next()) == null){
            fail("Attachment is null");
        }
        
        // Make sure the content id starts with the appropriate SWA name
        if (!ap4.getContentId().startsWith("jpegImage2Response=")) {
            fail("Expected content id to start with jpegImage2Response");
        }

        if (it.hasNext()){
            fail("Detected more then 2 attachment");
        }

        sb = reply.getSOAPBody();
        if (sb.getChildElements().hasNext()) {
            fail("Message contains soap:body payload");
        }

        bytes3 = ap3.getRawContentBytes();
        if (bytes3.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes3[0] != '3') { 
            fail("The response attachment is not correct"); 
        }
        
        bytes4 = ap4.getRawContentBytes();
        if (bytes4.length == 0) { 
            fail("Attachment is empty"); 
        }
        if (bytes4[0] != '4') { 
            fail("The response attachment is not correct"); 
        }
    }
    // TODO:  Add similar code to invoke the mtom enabled operation
}