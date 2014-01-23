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

import java.util.HashMap;

import org.apache.axiom.mime.MultipartWriterFactory;
import org.apache.axiom.mime.impl.axiom.AxiomMultipartWriterFactory;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.om.util.StAXWriterConfiguration;
import org.apache.axiom.om.util.XMLStreamWriterFilter;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Formats options for OM Output.
 * <p/>
 * Setting of all the properties in a OMOutputFormat should be done before calling the
 * getContentType() method. It is advised to set all the properties at the creation time of the
 * OMOutputFormat and not to change them later.
 */
public class OMOutputFormat {
    
    private static Log log = LogFactory.getLog(OMOutputFormat.class);
    
    private String mimeBoundary;
    private String rootContentId;
    private int nextid;
    private boolean doOptimize;
    private boolean doingSWA;
    private boolean isSoap11;
    private int optimizedThreshold;
    
    /** Field DEFAULT_CHAR_SET_ENCODING. Specifies the default character encoding scheme to be used. */
    public static final String DEFAULT_CHAR_SET_ENCODING = "utf-8";

    private String charSetEncoding;
    private String xmlVersion;
    private String contentType;
    
    /**
     * Flag set if {@link #contentType} has been set explicitly through
     * {@link #setContentType(String)}. If this attribute is <code>false</code> and
     * {@link #contentType} is non null, then it was calculated by {@link #getContentType()}.
     */
    private boolean contentTypeSet;
    
    private boolean ignoreXMLDeclaration;
    private boolean autoCloseWriter;

    public static final String ACTION_PROPERTY = "action";
    
    private XMLStreamWriterFilter xmlStreamWriterFilter = null;
    
    private StAXWriterConfiguration writerConfiguration;
    
    private MultipartWriterFactory multipartWriterFactory;

    // The value of this property is a Boolean.  
    // A missing value indicates the default action, which is Boolean.FALSE
    // If Boolean.TRUE, attachments that are "non textual" are written out with 
    // a content-transfer-encoding type of base64.
    // @See CommonUtils.isTextualPart for the textual part definition.
    // 
    // Example:
    //   An attachment with a content-type of "image/gif" is a non-textual attachment.
    //   An attachment with a content-type of "application/soap+xml" is an textual attachment
    //
    public static final String USE_CTE_BASE64_FOR_NON_TEXTUAL_ATTACHMENTS = 
        "org.apache.axiom.om.OMFormat.use.cteBase64.forNonTextualAttachments";
    
    // The old default behavior for the swa output attachment order was the 
    // natural order of the content ids.
    //
    // There are some customers who want the output order to match the 
    // input order for swa attachments.
    public static final String RESPECT_SWA_ATTACHMENT_ORDER =
        "org.apache.axiom.om.OMFormat.respectSWAAttachmentOrder";
    
    public static final Boolean RESPECT_SWA_ATTACHMENT_ORDER_DEFAULT =
        Boolean.TRUE;
    
    
    private HashMap map;  // Map of generic properties


    public OMOutputFormat() {
        isSoap11 = true;
    }
    
    /**
     * Constructs a new instance by copying the configuration from an existing instance. Note that
     * this will only copy configuration data, but not information that is subject to
     * auto-generation, such as the root content ID or the MIME boundary.
     * 
     * @param format
     *            the existing instance
     */
    public OMOutputFormat(OMOutputFormat format) {
        doOptimize = format.doOptimize;
        doingSWA = format.doingSWA;
        isSoap11 = format.isSoap11;
        optimizedThreshold = format.optimizedThreshold;
        charSetEncoding = format.charSetEncoding;
        xmlVersion = format.xmlVersion;
        if (format.contentTypeSet) {
            contentTypeSet = true;
            contentType = format.contentType;
        }
        ignoreXMLDeclaration = format.ignoreXMLDeclaration;
        autoCloseWriter = format.autoCloseWriter;
        xmlStreamWriterFilter = format.xmlStreamWriterFilter;
        writerConfiguration = format.writerConfiguration;
        multipartWriterFactory = format.multipartWriterFactory;
        if (format.map != null) {
            map = new HashMap(format.map);
        }
    }
    
    /**
     * @param key String
     * @return property or null
     */
    public Object getProperty(String key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * @param key String
     * @param value Object
     * @return old value or null
     */
    public Object setProperty(String key, Object value) {
        if (map == null) {
            map = new HashMap();
        }
        return map.put(key, value);
    }
    
    /**
     * @param key
     * @return true if known key
     */
    public boolean containsKey(String key) {
        if (map == null) {
            return false;
        } 
        return map.containsKey(key);
    }

    /**
     * Indicates whether the document should be serialized using MTOM.
     * 
     * @return <code>true</code> if the document should be serialized using MTOM; <code>false</code>
     *         otherwise; the return value is always <code>false</code> if {@link #isDoingSWA()}
     *         returns <code>true</code>
     */
    public boolean isOptimized() {
        return doOptimize && !doingSWA;  // optimize is disabled if SWA
    }

    /**
     * Return the content-type value that should be written with the message.
     * (i.e. if optimized, then a multipart/related content-type is returned).
     * @return content-type value
     */
    public String getContentType() {
       
        String ct = null;
        if (log.isDebugEnabled()) {
            log.debug("Start getContentType: " + toString());
        }
        if (contentType == null) {
            if (isSoap11) {
                contentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
            } else {
                contentType = SOAP12Constants.SOAP_12_CONTENT_TYPE;
            }
        }
        // If MTOM or SWA, the returned content-type is an 
        // appropriate multipart/related content type.
        if (isOptimized()) {
            ct = this.getContentTypeForMTOM(contentType);
        } else if (isDoingSWA()) {
            ct = this.getContentTypeForSwA(contentType);
        } else {
            ct = contentType;
        }
        if (log.isDebugEnabled()) {
            log.debug("getContentType= {" + ct + "}   " + toString());
        }
        return ct;
    }
    
    /**
     * Set a raw content-type 
     * (i.e. "text/xml" (SOAP 1.1) or "application/xml" (REST))
     * If this method is not invoked, OMOutputFormat will choose
     * a content-type value consistent with the soap version.
     * @param c
     */
    public void setContentType(String c) {
        contentTypeSet = true;
        contentType = c;
    }

    public String getMimeBoundary() {
        if (mimeBoundary == null) {
            mimeBoundary = UIDGenerator.generateMimeBoundary();
        }
        return mimeBoundary;
    }

    public String getRootContentId() {
        if (rootContentId == null) {
            rootContentId = "0." + UIDGenerator.generateContentId();
        }
        return rootContentId;
    }

    public String getNextContentId() {
        nextid++;
        return nextid + "." + UIDGenerator.generateContentId();
    }

    /**
     * Returns the character set encoding scheme.
     *
     * @return Returns encoding string or null if it has not been set.
     */
    public String getCharSetEncoding() {
        return this.charSetEncoding;
    }

    public void setCharSetEncoding(String charSetEncoding) {
        this.charSetEncoding = charSetEncoding;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public void setSOAP11(boolean b) {
        isSoap11 = b;
    }

    public boolean isSOAP11() {
        return isSoap11;
    }

    public boolean isIgnoreXMLDeclaration() {
        return ignoreXMLDeclaration;
    }

    public void setIgnoreXMLDeclaration(boolean ignoreXMLDeclaration) {
        this.ignoreXMLDeclaration = ignoreXMLDeclaration;
    }

    /**
     * Specifies that the document should be serialized using MTOM. Note that this setting is
     * ignored if SwA is enabled using {@link #setDoingSWA(boolean)}.
     * 
     * @param optimize
     *            <code>true</code> if the document should be serialized using MTOM;
     *            <code>false</code> otherwise
     */
    public void setDoOptimize(boolean optimize) {
        doOptimize = optimize;
    }

    /**
     * Indicates whether the document should be serialized using SwA.
     * 
     * @return <code>true</code> if the document should be serialized using SwA; <code>false</code>
     *         otherwise
     */
    public boolean isDoingSWA() {
        return doingSWA;
    }

    /**
     * Specifies that the document should be serialized using SwA (SOAP with Attachments). When SwA
     * is enabled, then any configuration done using {@link #setDoOptimize(boolean)} is ignored.
     * 
     * @param doingSWA
     *            <code>true</code> if the document should be serialized using SwA;
     *            <code>false</code> otherwise
     */
    public void setDoingSWA(boolean doingSWA) {
        this.doingSWA = doingSWA;
    }

    /**
     * Generates a Content-Type value for MTOM messages.  This is a MIME Multipart/Related
     * Content-Type value as defined by RFC 2387 and the XOP specification.  The generated
     * header will look like the following:
     * 
     *   Content-Type: multipart/related; boundary="[MIME BOUNDARY VALUE]"; 
     *      type="application/xop+xml"; 
     *      start="[MESSAGE CONTENT ID]"; 
     *      start-info="[MESSAGE CONTENT TYPE]";
     * 
     * @param SOAPContentType
     * @return TODO
     */
    public String getContentTypeForMTOM(String SOAPContentType) {
        // If an action was set, we need to include it within the value 
        // for the start-info attribute.  
        if (containsKey(ACTION_PROPERTY)) {
            String action = (String) getProperty(ACTION_PROPERTY);
            if (action != null && action.length() > 0) {
                SOAPContentType = SOAPContentType + "; action=\\\"" + action + "\\\"";   
            }                     
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("multipart/related");
        sb.append("; ");
        sb.append("boundary=");
        // The value of the boundary parameter must be enclosed in double quotation  
        // marks, according to the Basic Profile 2.0 Specification, Rule R1109:
        // "Parameters on the Content-Type MIME header field-value in a request 
        // MESSAGE MUST be a quoted string."
        sb.append("\"");
        sb.append(getMimeBoundary());
        sb.append("\"");
        sb.append("; ");
        sb.append("type=\"" + MTOMConstants.MTOM_TYPE + "\"");
        sb.append("; ");
        sb.append("start=\"<").append(getRootContentId()).append(">\"");
        sb.append("; ");
        sb.append("start-info=\"").append(SOAPContentType).append("\"");
        return sb.toString();
    }

    public String getContentTypeForSwA(String SOAPContentType) {
        StringBuffer sb = new StringBuffer();
        sb.append("multipart/related");
        sb.append("; ");
        sb.append("boundary=");
        // The value of the boundary parameter must be enclosed in double quotation  
        // marks, according to the Basic Profile 2.0 Specification, Rule R1109:
        // "Parameters on the Content-Type MIME header field-value in a request 
        // MESSAGE MUST be a quoted string."
        sb.append("\"");
        sb.append(getMimeBoundary());
        sb.append("\"");
        sb.append("; ");
        sb.append("type=\"").append(SOAPContentType).append("\"");
        sb.append("; ");
        sb.append("start=\"<").append(getRootContentId()).append(">\"");
        return sb.toString();
    }

    /**
     * @deprecated
     */
    public boolean isAutoCloseWriter() {
        return autoCloseWriter;
    }

    /**
     * @deprecated
     */
    public void setAutoCloseWriter(boolean autoCloseWriter) {
        this.autoCloseWriter = autoCloseWriter;
    }

    public void setMimeBoundary(String mimeBoundary) {
        this.mimeBoundary = mimeBoundary;
    }
    public void setRootContentId(String rootContentId) {
		this.rootContentId = rootContentId;
	}

    
    /**
     * Use toString for logging state of the OMOutputFormat
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("OMOutputFormat [");
        
        sb.append(" mimeBoundary =");
        sb.append(mimeBoundary);
        
        sb.append(" rootContentId=");
        sb.append(rootContentId);
        
        sb.append(" doOptimize=");
        sb.append(doOptimize);
        
        sb.append(" doingSWA=");
        sb.append(doingSWA);
        
        sb.append(" isSOAP11=");
        sb.append(isSoap11);
        
        sb.append(" charSetEncoding=");
        sb.append(charSetEncoding);
        
        sb.append(" xmlVersion=");
        sb.append(xmlVersion);
        
        sb.append(" contentType=");
        sb.append(contentType);
        
        sb.append(" ignoreXmlDeclaration=");
        sb.append(ignoreXMLDeclaration);
        
        sb.append(" autoCloseWriter=");
        sb.append(autoCloseWriter);
        
        // TODO Print all properties
        sb.append(" actionProperty=");
        sb.append(getProperty(ACTION_PROPERTY));

        sb.append(" optimizedThreshold=");
        sb.append(optimizedThreshold);
        
        sb.append("]");
        return sb.toString();
        
    }

    public void setOptimizedThreshold(int optimizedThreshold) {
        this.optimizedThreshold = optimizedThreshold;
    }
    
    public int getOptimizedThreshold() {
        return optimizedThreshold;
    }
    
    /**
     * @return the xmlStreamWriterFilter
     */
    public XMLStreamWriterFilter getXmlStreamWriterFilter() {
        return xmlStreamWriterFilter;
    }

    /**
     * @param xmlStreamWriterFilter the xmlStreamWriterFilter to set
     */
    public void setXmlStreamWriterFilter(XMLStreamWriterFilter xmlStreamWriterFilter) {
        this.xmlStreamWriterFilter = xmlStreamWriterFilter;
    }

    /**
     * Get the currently configured StAX writer configuration.
     * 
     * @return the current configuration; {@link StAXWriterConfiguration#DEFAULT} if none has been
     *         set explicitly
     */
    public StAXWriterConfiguration getStAXWriterConfiguration() {
        return writerConfiguration == null ? StAXWriterConfiguration.DEFAULT : writerConfiguration;
    }
     
    /**
     * Set the StAX writer configuration that will be used when requesting an
     * {@link javax.xml.stream.XMLStreamWriter} from {@link org.apache.axiom.om.util.StAXUtils}.
     * 
     * @param writerConfiguration
     *            the configuration
     */
    public void setStAXWriterConfiguration(StAXWriterConfiguration writerConfiguration) {
        this.writerConfiguration = writerConfiguration;
    }

    /**
     * Get the currently configured multipart writer factory.
     * 
     * @return the current factory; if none has been set explicitly, an
     *         {@link AxiomMultipartWriterFactory} instance is returned
     */
    public MultipartWriterFactory getMultipartWriterFactory() {
        return multipartWriterFactory == null
                ? AxiomMultipartWriterFactory.INSTANCE
                : multipartWriterFactory;
    }

    /**
     * Set the multipart writer factory. This factory is used to create MIME packages when MTOM or
     * SwA is enabled.
     * 
     * @param multipartWriterFactory
     *            the factory
     */
    public void setMultipartWriterFactory(MultipartWriterFactory multipartWriterFactory) {
        this.multipartWriterFactory = multipartWriterFactory;
    }
}
