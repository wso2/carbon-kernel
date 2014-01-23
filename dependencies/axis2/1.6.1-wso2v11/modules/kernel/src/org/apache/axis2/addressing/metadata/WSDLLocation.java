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

package org.apache.axis2.addressing.metadata;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class WSDLLocation {
    private static final Log log = LogFactory.getLog(WSDLLocation.class);
    
    // Support both WSDLI namespaces on inbound messages to allow interop with earlier versions of axis2 
    private static final QName WSDLI = new QName("http://www.w3.org/2006/01/wsdl-instance", "wsdlLocation", "wsdli");
    private static final QName FINAL_WSDLI = new QName("http://www.w3.org/ns/wsdl-instance", "wsdlLocation", "wsdli");
    
    private String targetNamespace;
    private String wsdlURL;
    
    public WSDLLocation() {
    }
    
    public WSDLLocation(String targetNamespace, String wsdlURL) {
        this.targetNamespace = targetNamespace;
        this.wsdlURL = wsdlURL;
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }
    public String getLocation() {
        return wsdlURL;
    }
    public void setLocation(String wsdlURL) {
        this.wsdlURL = wsdlURL;
    }
    
    /**
     * Convenience method to convert an object of this type to an <code>OMAttribute</code>
     * <p>
     * &lt;... xmlns:wsdli="http://www.w3.org/ns/wsdl-instance" wsdli:wsdlLocation="targetNamespace wsdlURL" ...&gt
     * </p>
     * @param factory <code>OMFactory</code> to use when generating <code>OMElement</code>s
     * 
     * @return an <code>OMAttribute</code> that can be added to an <code>EndpointReference</code>
     */
    public OMAttribute toOM(OMFactory factory) {
        String value = new StringBuffer(targetNamespace).append(" ").append(wsdlURL).toString();
        OMNamespace wsdliNs = factory.createOMNamespace(FINAL_WSDLI.getNamespaceURI(), FINAL_WSDLI.getPrefix());
        OMAttribute omAttribute = factory.createOMAttribute(FINAL_WSDLI.getLocalPart(), wsdliNs, value);

        return omAttribute;
    }
    
    /**
     * Convenience method for converting an OMAttribute to an instance of either of these types.
     * <p>
     * &lt;... xmlns:wsdli="http://www.w3.org/2006/01/wsdl-instance" wsdli:wsdlLocation="targetNamespace wsdlURL" ...&gt
     * </p>
     * <p>
     * &lt;... xmlns:wsdli="http://www.w3.org/ns/wsdl-instance" wsdli:wsdlLocation="targetNamespace wsdlURL" ...&gt
     * </p>
     * @param omAttribute the <code>OMAttribute</code> that holds the wsdl location.
     * @throws AxisFault
     */
    public void fromOM(OMAttribute omAttribute) throws AxisFault {
        QName qname = omAttribute.getQName();
        if (WSDLI.equals(qname) || FINAL_WSDLI.equals(qname)) {
           String value = omAttribute.getAttributeValue().trim();
           String[] values = value.split("\\s", 2);
           
           //Don't set any values if split doesn't
           //give us the correct number of elements.
           if (values.length != 2)
               return;
           
           targetNamespace = values[0];
           wsdlURL = values[1];
           
           if (log.isDebugEnabled()) {
               log.debug("fromOM: Extracted WSDLLocation targetNamespace = " + targetNamespace + " and wsdlURL = " + wsdlURL + " from an OMAttribute with QName = " + qname);
           }
        }
        else {
            throw new AxisFault("Unrecognized element.");
        }
    }
    
    
    /**
     * Static method to test whether an <code>OMElement</code> is recognized
     * as a ServiceName element. If this method returns <code>true</code> then
     * {@link #fromOM(OMAttribute)} is guaranteed not to throw an exception.
     * 
     * @param omAttribute the <code>OMElement</code> to test.
     * @return <code>true</code> if the element is a ServiceName element,
     * <code>false</code> otherwise.
     */
    public static boolean isWSDLLocationAttribute(OMAttribute omAttribute) {
        boolean result = false;
        QName qname = omAttribute.getQName();
        
        if (WSDLI.equals(qname) || FINAL_WSDLI.equals(qname))
            result = true;

        if (log.isDebugEnabled()) {
            log.debug("isWSDLLocationAttribute: OMAttribute QName = " + qname + ", result = " + result);
        }
        
        return result;
    }
}
