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

package org.apache.axis2.jaxws.addressing;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.i18n.Messages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Java class for EndpointReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EndpointReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Address" type="{http://schemas.xmlsoap.org/ws/2004/08/addressing}AttributedURI"/>
 *         &lt;element name="ReferenceProperties" type="{http://schemas.xmlsoap.org/ws/2004/08/addressing}ReferencePropertiesType" minOccurs="0"/>
 *         &lt;element name="ReferenceParameters" type="{http://schemas.xmlsoap.org/ws/2004/08/addressing}ReferenceParametersType" minOccurs="0"/>
 *         &lt;element name="PortType" type="{http://schemas.xmlsoap.org/ws/2004/08/addressing}AttributedQName" minOccurs="0"/>
 *         &lt;element name="ServiceName" type="{http://schemas.xmlsoap.org/ws/2004/08/addressing}ServiceNameType" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "EndpointReference", namespace = SubmissionEndpointReference.NS)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndpointReferenceType", namespace = SubmissionEndpointReference.NS, propOrder = {
    "address",
    "referenceProperties",
    "referenceParameters",
    "portType",
    "serviceName",
    "any"
})
public class SubmissionEndpointReference extends EndpointReference {
    protected static final String NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    protected static JAXBContext jaxbContext;

    @XmlElement(name = "Address", namespace = NS, required = true)
    protected AttributedURI address;
    @XmlElement(name = "ReferenceProperties", namespace = NS)
    protected ReferencePropertiesType referenceProperties;
    @XmlElement(name = "ReferenceParameters", namespace = NS)
    protected ReferenceParametersType referenceParameters;
    @XmlElement(name = "PortType", namespace = NS)
    protected AttributedQName portType;
    @XmlElement(name = "ServiceName", namespace = NS)
    protected ServiceNameType serviceName;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();
    
    static {
        try { 
            jaxbContext =(JAXBContext)
            AccessController.doPrivileged(
               new PrivilegedExceptionAction() {
                                              
                   public Object run() throws JAXBException {
                       return JAXBContext.newInstance(SubmissionEndpointReference.class);
                   }
               });
        }
        catch (Exception e) {
            throw new WebServiceException(Messages.getMessage("jaxbContextFailure", e.toString()));
        }
    }

    protected SubmissionEndpointReference() {
    }
    
    public SubmissionEndpointReference(Source eprInfoset) {
        super();
        
        try {
            Unmarshaller um = jaxbContext.createUnmarshaller();
            SubmissionEndpointReference subEPR =
                (SubmissionEndpointReference) um.unmarshal(eprInfoset);
            
            address = subEPR.address;
            referenceParameters = subEPR.referenceParameters;
            referenceProperties = subEPR.referenceProperties;
            portType = subEPR.portType;
            serviceName = subEPR.serviceName;
            any = subEPR.any;
            otherAttributes.putAll(subEPR.otherAttributes);
        }
        catch (Exception e) {
            throw new WebServiceException(Messages.getMessage("endpointRefConstructionFailure2", 
                                                              e.toString()));
        }        
    }
    
    @Override
    public void writeTo(Result result) {
        if (result == null) {
            throw new IllegalArgumentException(Messages.getMessage("endpointReferenceWriteError1"));
        }
        
        try {
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            m.marshal(this, result);
        }
        catch (Exception e) {
            throw new WebServiceException(Messages.getMessage("endpointReferenceWriteError1", 
                                                              e.toString()));
        }
    }
    
    /**
     * <p>Java class for AttributedURI complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="AttributedURI">
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "AttributedURI", propOrder = {
        "value"
    })
    private static class AttributedURI {

        @XmlValue
        @XmlSchemaType(name = "anyURI")
        protected String value;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();
        
        public AttributedURI() {
        }
    }
    
    /**
     * <p>Java class for ReferenceParametersType complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="ReferenceParametersType">
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;any/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ReferenceParametersType", propOrder = {
        "any"
    })
    private static class ReferenceParametersType {

        @XmlAnyElement(lax = true)
        protected List<Object> any;
        
        public ReferenceParametersType() {
        }
    }
    
    /**
     * <p>Java class for ReferencePropertiesType complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="ReferencePropertiesType">
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;any/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ReferencePropertiesType", propOrder = {
        "any"
    })
    private static class ReferencePropertiesType {

        @XmlAnyElement(lax = true)
        protected List<Object> any;
        
        public ReferencePropertiesType() {
        }
    }
    
    /**
     * <p>Java class for ServiceNameType complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="ServiceNameType">
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>QName">
     *       &lt;attribute name="PortName" type="{http://www.w3.org/2001/XMLSchema}NCName" />
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ServiceNameType", propOrder = {
        "value"
    })
    private static class ServiceNameType {

        @XmlValue
        protected QName value;
        @XmlAttribute(name = "PortName")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        @XmlSchemaType(name = "NCName")
        protected String portName;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();
        
        public ServiceNameType() {
        }
    }
    
    /**
     * <p>Java class for AttributedQName complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType name="AttributedQName">
     *   &lt;simpleContent>
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>QName">
     *     &lt;/extension>
     *   &lt;/simpleContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "AttributedQName", propOrder = {
        "value"
    })
    private static class AttributedQName {

        @XmlValue
        protected QName value;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();
        
        public AttributedQName() {
        }
    }
}
