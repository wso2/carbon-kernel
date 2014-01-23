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

package org.apache.axiom.om;

import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class OMOutputFormatTest extends TestCase {
    
    public void testAPI_getProperty() throws Exception {
        Method m = OMOutputFormat.class.getMethod("getProperty", new Class[] {String.class});
        assertTrue(m != null);
        
        Class returnType = m.getReturnType();
        assertTrue(returnType == Object.class);
    }
    
    public void testAPI_setProperty() throws Exception {
        Method m = OMOutputFormat.class.getMethod("setProperty", new Class[] {String.class, Object.class});
        assertTrue(m != null);
        
        Class returnType = m.getReturnType();
        assertTrue(returnType == Object.class);
    }
    
    public void testAPI_publicProperties() throws Exception {
        Field f = OMOutputFormat.class.getField("ACTION_PROPERTY");
        assertTrue(f != null);        
    }

    public void testGetContentTypeDefault() {
        OMOutputFormat format = new OMOutputFormat();
        String contentType = format.getContentType();
        assertTrue(contentType.equals(SOAP11Constants.SOAP_11_CONTENT_TYPE));
    }
    
    public void testGetContentTypeSOAP12() {
        OMOutputFormat format = new OMOutputFormat();
        format.setSOAP11(false);
        String contentType = format.getContentType();
        assertTrue(contentType.equals(SOAP12Constants.SOAP_12_CONTENT_TYPE));
    }
    
    public void testGetContentTypeSOAP11MTOM() {
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        String contentType = format.getContentType();
        
        // This is rudimentary.  We can add a more complete test that checks
        // sub items in the future.
        assertTrue(contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)!=-1);
        assertTrue(contentType.indexOf(MTOMConstants.MTOM_TYPE)!=-1);
        
        // Test for a double quoted boundary value.
        // The Basic Profile 2.0 Specification, Rule R1109 says,
        // "Parameters on the Content-Type MIME header field-value 
        // in a request MESSAGE MUST be a quoted string."
        assertTrue(contentType.indexOf("boundary=\"")!=-1);
    }
    
    public void testGetContentTypeSOAP11SWA() {
        OMOutputFormat format = new OMOutputFormat();
        format.setSOAP11(true);
        format.setDoingSWA(true);
        String contentType = format.getContentType();
        
        // This is rudimentary.  We can add a more complete test that checks
        // sub items in the future.
        assertTrue(contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)>=0);
        assertTrue(contentType.indexOf("multipart/related")>=0);
        assertTrue(contentType.indexOf(MTOMConstants.MTOM_TYPE) < 0);
        
        // Sometimes the OMOutputFormat has both "optimized" and "doing swa".
        // In such cases, the winner should be swa.
        
        format = new OMOutputFormat();
        format.setSOAP11(true);
        format.setDoingSWA(true);
        format.setDoOptimize(true);
        contentType = format.getContentType();
        
        // This is rudimentary.  We can add a more complete test that checks
        // sub items in the future.
        assertTrue(contentType.indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)>=0);
        assertTrue(contentType.indexOf("multipart/related")>=0);
        assertTrue(contentType.indexOf(MTOMConstants.MTOM_TYPE) < 0);
        
        // Test for a double quoted boundary value.
        // The Basic Profile 2.0 Specification, Rule R1109 says,
        // "Parameters on the Content-Type MIME header field-value 
        // in a request MESSAGE MUST be a quoted string."
        assertTrue(contentType.indexOf("boundary=\"")!=-1);
    }
    
    public void testGetContentTypeSOAP12MTOM() {
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(false);
        String contentType = format.getContentType();
        
        // This is rudimentary.  We can add a more complete test that checks
        // sub items in the future.
        assertTrue(contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE)!=-1);
        assertTrue(contentType.indexOf(MTOMConstants.MTOM_TYPE)!=-1);
    }
    
    public void testGetContentTypeSOAP12MTOMWithAction() {
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setSOAP11(false);
        format.setProperty(OMOutputFormat.ACTION_PROPERTY, "testSoapAction");
        String contentType = format.getContentType();
        
        // This is rudimentary.  We can add a more complete test that checks
        // sub items in the future.
        assertTrue(contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE)!=-1);
        assertTrue(contentType.indexOf(MTOMConstants.MTOM_TYPE)!=-1);
        assertTrue(contentType.indexOf("action=\\\"testSoapAction\\\"")!=-1);
    }
}
