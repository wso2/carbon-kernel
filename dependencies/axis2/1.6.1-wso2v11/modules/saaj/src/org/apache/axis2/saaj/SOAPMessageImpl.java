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

package org.apache.axis2.saaj;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.OMMultipartWriter;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class SOAPMessageImpl extends SOAPMessage {

    private SOAPPart soapPart;
    private Collection<AttachmentPart> attachmentParts = new ArrayList<AttachmentPart>();
    private MimeHeaders mimeHeaders;

    private Map<String,Object> props = new Hashtable<String,Object>();
    private boolean saveRequired;

    public SOAPMessageImpl(SOAPEnvelopeImpl soapEnvelope) {
        this.mimeHeaders = new MimeHeaders();
        if (soapEnvelope.getOMFactory() instanceof SOAP11Factory) {
            this.mimeHeaders.addHeader("content-type", HTTPConstants.MEDIA_TYPE_TEXT_XML);
        } else if (soapEnvelope.getOMFactory() instanceof SOAP12Factory) {
            this.mimeHeaders.addHeader("content-type",
                    HTTPConstants.MEDIA_TYPE_APPLICATION_SOAP_XML);
        }
        soapPart = new SOAPPartImpl(this, soapEnvelope);
    }

    public SOAPMessageImpl(InputStream inputstream, MimeHeaders mimeHeaders, boolean processMTOM)
            throws SOAPException {
        String contentType = null;
        String tmpContentType = "";
        if (mimeHeaders != null) {
            String contentTypes[] = mimeHeaders.getHeader(HTTPConstants.CONTENT_TYPE);
            if (contentTypes != null && contentTypes.length > 0) {
                tmpContentType = contentTypes[0];
                contentType = SAAJUtil.normalizeContentType(tmpContentType);
            }
        }
        if ("multipart/related".equals(contentType)) {
            try {
                Attachments attachments =
                        new Attachments(inputstream, tmpContentType, false, "", "");
                
                // Axiom doesn't give us access to the MIME headers of the individual
                // parts of the SOAP message package. We need to reconstruct them from
                // the available information.
                MimeHeaders soapPartHeaders = new MimeHeaders();
                soapPartHeaders.addHeader(HTTPConstants.CONTENT_TYPE,
                        attachments.getSOAPPartContentType());
                String soapPartContentId = attachments.getSOAPPartContentID();
                soapPartHeaders.addHeader("Content-ID", "<" + soapPartContentId + ">");
                
                soapPart = new SOAPPartImpl(this, attachments.getSOAPPartInputStream(),
                        soapPartHeaders, processMTOM ? attachments : null);
                
                for (String contentId : attachments.getAllContentIDs()) {
                    if (!contentId.equals(soapPartContentId)) {
                        AttachmentPart ap =
                                createAttachmentPart(attachments.getDataHandler(contentId));
                        ap.setContentId("<" + contentId + ">");
                        attachmentParts.add(ap);
                    }
                }
            } catch (OMException e) {
                throw new SOAPException(e);
            }
        } else {
            initCharsetEncodingFromContentType(tmpContentType);
            soapPart = new SOAPPartImpl(this, inputstream, mimeHeaders, null);
        }

        this.mimeHeaders = (mimeHeaders == null) ?
                new MimeHeaders() :
                SAAJUtil.copyMimeHeaders(mimeHeaders);
    }

    /**
     * Retrieves a description of this <CODE>SOAPMessage</CODE> object's content.
     *
     * @return a <CODE>String</CODE> describing the content of this message or <CODE>null</CODE> if
     *         no description has been set
     * @see #setContentDescription(String) setContentDescription(java.lang.String)
     */
    public String getContentDescription() {
        String values[] = mimeHeaders.getHeader(HTTPConstants.HEADER_CONTENT_DESCRIPTION);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    /**
     * Sets the description of this <CODE>SOAPMessage</CODE> object's content with the given
     * description.
     *
     * @param description a <CODE>String</CODE> describing the content of this message
     * @see #getContentDescription() getContentDescription()
     */
    public void setContentDescription(String description) {
        mimeHeaders.setHeader(HTTPConstants.HEADER_CONTENT_DESCRIPTION, description);
    }

    /**
     * Gets the SOAP part of this <CODE>SOAPMessage</CODE> object.
     * <p/>
     * <p/>
     * <P>If a <CODE>SOAPMessage</CODE> object contains one or more attachments, the SOAP Part must
     * be the first MIME body part in the message.</P>
     *
     * @return the <CODE>SOAPPart</CODE> object for this <CODE> SOAPMessage</CODE> object
     */
    public SOAPPart getSOAPPart() {
        return soapPart;
    }

    /**
     * Removes all <CODE>AttachmentPart</CODE> objects that have been added to this
     * <CODE>SOAPMessage</CODE> object.
     * <p/>
     * <P>This method does not touch the SOAP part.</P>
     */
    public void removeAllAttachments() {
        attachmentParts.clear();
    }

    /**
     * Gets a count of the number of attachments in this message. This count does not include the
     * SOAP part.
     *
     * @return the number of <CODE>AttachmentPart</CODE> objects that are part of this
     *         <CODE>SOAPMessage</CODE> object
     */
    public int countAttachments() {
        return attachmentParts.size();
    }

    /**
     * Retrieves all the <CODE>AttachmentPart</CODE> objects that are part of this
     * <CODE>SOAPMessage</CODE> object.
     *
     * @return an iterator over all the attachments in this message
     */
    public Iterator getAttachments() {
        return attachmentParts.iterator();
    }

    /**
     * Retrieves all the AttachmentPart objects that have header entries that match the specified
     * headers. Note that a returned attachment could have headers in addition to those specified.
     *
     * @param headers a {@link javax.xml.soap.MimeHeaders} object containing the MIME headers for
     *                which to search
     * @return an iterator over all attachments({@link javax.xml.soap.AttachmentPart}) that have a
     *         header that matches one of the given headers
     */
    public Iterator getAttachments(javax.xml.soap.MimeHeaders headers) {
        Collection<AttachmentPart> matchingAttachmentParts = new ArrayList<AttachmentPart>();
        Iterator iterator = getAttachments();
        {
            AttachmentPartImpl part;
            while (iterator.hasNext()) {
                part = (AttachmentPartImpl)iterator.next();
                if (part.matches(headers)) {
                    matchingAttachmentParts.add(part);
                }
            }
        }
        return matchingAttachmentParts.iterator();
    }

    /**
     * Adds the given <CODE>AttachmentPart</CODE> object to this <CODE>SOAPMessage</CODE> object. An
     * <CODE> AttachmentPart</CODE> object must be created before it can be added to a message.
     *
     * @param attachmentPart an <CODE> AttachmentPart</CODE> object that is to become part of this
     *                       <CODE>SOAPMessage</CODE> object
     * @throws IllegalArgumentException
     *
     */
    public void addAttachmentPart(AttachmentPart attachmentPart) {
        if (attachmentPart != null) {
            attachmentParts.add(attachmentPart);
            mimeHeaders.setHeader(HTTPConstants.CONTENT_TYPE, "multipart/related");
        }
    }

    /**
     * Creates a new empty <CODE>AttachmentPart</CODE> object. Note that the method
     * <CODE>addAttachmentPart</CODE> must be called with this new <CODE>AttachmentPart</CODE>
     * object as the parameter in order for it to become an attachment to this
     * <CODE>SOAPMessage</CODE> object.
     *
     * @return a new <CODE>AttachmentPart</CODE> object that can be populated and added to this
     *         <CODE>SOAPMessage</CODE> object
     */
    public AttachmentPart createAttachmentPart() {
        return new AttachmentPartImpl();
    }

    /**
     * Returns all the transport-specific MIME headers for this <CODE>SOAPMessage</CODE> object in a
     * transport-independent fashion.
     *
     * @return a <CODE>MimeHeaders</CODE> object containing the <CODE>MimeHeader</CODE> objects
     */
    public javax.xml.soap.MimeHeaders getMimeHeaders() {
        return mimeHeaders;
    }

    /**
     * Updates this <CODE>SOAPMessage</CODE> object with all the changes that have been made to it.
     * This method is called automatically when a message is sent or written to by the methods
     * <CODE>ProviderConnection.send</CODE>, <CODE> SOAPConnection.call</CODE>, or <CODE>
     * SOAPMessage.writeTo</CODE>. However, if changes are made to a message that was received or to
     * one that has already been sent, the method <CODE>saveChanges</CODE> needs to be called
     * explicitly in order to save the changes. The method <CODE>saveChanges</CODE> also generates
     * any changes that can be read back (for example, a MessageId in profiles that support a
     * message id). All MIME headers in a message that is created for sending purposes are
     * guaranteed to have valid values only after <CODE>saveChanges</CODE> has been called.
     * <p/>
     * <P>In addition, this method marks the point at which the data from all constituent
     * <CODE>AttachmentPart</CODE> objects are pulled into the message.</P>
     *
     * @throws SOAPException if there was a problem saving changes to this message.
     */
    public void saveChanges() throws SOAPException {
        saveRequired = false;
        // TODO not sure of the implementation
    }

    public void setSaveRequired() {
        this.saveRequired = true;
    }

    /**
     * Indicates whether this <CODE>SOAPMessage</CODE> object has had the method {@link
     * #saveChanges()} called on it.
     *
     * @return <CODE>true</CODE> if <CODE>saveChanges</CODE> has been called on this message at
     *         least once; <CODE> false</CODE> otherwise.
     */
    public boolean saveRequired() {
        return saveRequired;
    }

    /**
     * Writes this <CODE>SOAPMessage</CODE> object to the given output stream. The externalization
     * format is as defined by the SOAP 1.1 with Attachments specification.
     * <p/>
     * <P>If there are no attachments, just an XML stream is written out. For those messages that
     * have attachments, <CODE>writeTo</CODE> writes a MIME-encoded byte stream.</P>
     *
     * @param out the <CODE>OutputStream</CODE> object to which this <CODE>SOAPMessage</CODE> object
     *            will be written
     * @throws SOAPException if there was a problem in externalizing this SOAP message
     * @throws IOException   if an I/O error occurs
     */
    public void writeTo(OutputStream out) throws SOAPException, IOException {
        try {
            OMOutputFormat format = new OMOutputFormat();
            String enc = (String)getProperty(CHARACTER_SET_ENCODING);
            format.setCharSetEncoding(enc != null ? enc : OMOutputFormat.DEFAULT_CHAR_SET_ENCODING);
            String writeXmlDecl = (String)getProperty(WRITE_XML_DECLARATION);
            if (writeXmlDecl == null || writeXmlDecl.equals("false")) {

                //SAAJ default case doesn't send XML decl
                format.setIgnoreXMLDeclaration(true);
            }

            SOAPEnvelope envelope = ((SOAPEnvelopeImpl)soapPart.getEnvelope()).getOMEnvelope();
            if (attachmentParts.isEmpty()) {
                envelope.serialize(out, format);
            } else {
                format.setSOAP11(((SOAPEnvelopeImpl)soapPart.getEnvelope()).getOMFactory()
                        instanceof SOAP11Factory);
                OMMultipartWriter mpw = new OMMultipartWriter(out, format);
                OutputStream rootPartOutputStream = mpw.writeRootPart();
                envelope.serialize(rootPartOutputStream);
                rootPartOutputStream.close();
                for (AttachmentPart ap : attachmentParts) {
                    mpw.writePart(ap.getDataHandler(), ap.getContentId());
                }
                mpw.complete();
            }
            saveChanges();
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Associates the specified value with the specified property. If there was already a value
     * associated with this property, the old value is replaced.
     * <p/>
     * The valid property names include <code>WRITE_XML_DECLARATION</code> and
     * <code>CHARACTER_SET_ENCODING</code>. All of these standard SAAJ properties are prefixed by
     * "javax.xml.soap". Vendors may also add implementation specific properties. These properties
     * must be prefixed with package names that are unique to the vendor.
     * <p/>
     * Setting the property <code>WRITE_XML_DECLARATION</code> to <code>"true"</code> will cause an
     * XML Declaration to be written out at the start of the SOAP message. The default value of
     * "false" suppresses this declaration.
     * <p/>
     * The property <code>CHARACTER_SET_ENCODING</code> defaults to the value <code>"utf-8"</code>
     * which causes the SOAP message to be encoded using UTF-8. Setting
     * <code>CHARACTER_SET_ENCODING</code> to <code>"utf-16"</code> causes the SOAP message to be
     * encoded using UTF-16.
     * <p/>
     * Some implementations may allow encodings in addition to UTF-8 and UTF-16. Refer to your
     * vendor's documentation for details.
     *
     * @param property the property with which the specified value is to be associated
     * @param value    the value to be associated with the specified property
     */
    public void setProperty(String property, Object value) {
        props.put(property, value);
    }

    /**
     * Retrieves value of the specified property.
     *
     * @param property the name of the property to retrieve
     * @return the value of the property or <code>null</code> if no such property exists
     * @throws SOAPException if the property name is not recognized
     */
    public Object getProperty(String property) throws SOAPException {
        return props.get(property);
    }

    /**
     * Returns an AttachmentPart object that is associated with an attachment that is referenced by
     * this SOAPElement or null if no such attachment exists. References can be made via an href
     * attribute as described in SOAP Messages with Attachments (http://www.w3.org/TR/SOAPattachments#SOAPReferenceToAttachements)
     * , or via a single Text child node containing a URI as described in the WS-I Attachments
     * Profile 1.0 for elements of schema type ref:swaRef(ref:swaRef (http://www.wsi.org/Profiles/AttachmentsProfile-1.0-2004-08-24.html")
     * ). These two mechanisms must be supported. The support for references via href attribute also
     * implies that this method should also be supported on an element that is an xop:Include
     * element (XOP (http://www.w3.org/2000/xp/Group/3/06/Attachments/XOP.html) ). other reference
     * mechanisms may be supported by individual implementations of this standard. Contact your
     * vendor for details.
     *
     * @param element - The SOAPElement containing the reference to an Attachment
     * @return the referenced AttachmentPart or null if no such AttachmentPart exists or no
     *         reference can be found in this SOAPElement.
     * @throws SOAPException - if there is an error in the attempt to access the attachment
     */
    public AttachmentPart getAttachment(SOAPElement soapelement) throws SOAPException {
        //TODO read strings from constants
        Iterator iterator = getAttachments();
        {
            AttachmentPartImpl attachmentPart;
            while (iterator.hasNext()) {
                attachmentPart = (AttachmentPartImpl)iterator.next();
                String[] contentIds = attachmentPart.getMimeHeader("Content-Id");

                //References can be made via an href attribute as described in SOAP Messages
                //with Attachments or via a single Text child node containing a URI
                String reference = soapelement.getAttribute("href");
                if (reference == null || reference.trim().length() == 0) {
                    reference = soapelement.getValue();
                    if (reference == null || reference.trim().length() == 0) {
                        return null;
                    }
                }

                for (int a = 0; a < contentIds.length; a++) {
                    //eg: cid:gifImage scenario
                    String idPart = reference.substring(reference.indexOf(":") + 1);
                    idPart = "<" + idPart + ">";
                    if (idPart.equals(contentIds[a])) {
                        return attachmentPart;
                    }
                }

                String[] contentLocations = attachmentPart.getMimeHeader("Content-Location");
                if (!(contentLocations == null)) {
                    //uri scenario
                    for (int b = 0; b < contentLocations.length; b++) {
                        if (reference.equals(contentLocations[b])) {
                            return attachmentPart;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Removes all the AttachmentPart objects that have header entries that match the specified
     * headers. Note that the removed attachment could have headers in addition to those specified.
     *
     * @param headers - a MimeHeaders object containing the MIME headers for which to search
     * @since SAAJ 1.3
     */
    public void removeAttachments(MimeHeaders headers) {
        Collection<AttachmentPart> newAttachmentParts = new ArrayList<AttachmentPart>();
        for (AttachmentPart attachmentPart : attachmentParts) {
            //Get all the headers
            for (Iterator iterator = headers.getAllHeaders(); iterator.hasNext();) {
                MimeHeader mimeHeader = (MimeHeader)iterator.next();
                String[] headerValues = attachmentPart.getMimeHeader(mimeHeader.getName());
                //if values for this header name, do not remove it
                if (headerValues.length != 0) {
                    if (!(headerValues[0].equals(mimeHeader.getValue()))) {
                        newAttachmentParts.add(attachmentPart);
                    }
                }
            }
        }
        attachmentParts.clear();
        this.attachmentParts = newAttachmentParts;
    }

    /**
     * Gets the SOAP Header contained in this <code>SOAPMessage</code> object.
     *
     * @return the <code>SOAPHeader</code> object contained by this <code>SOAPMessage</code> object
     * @throws javax.xml.soap.SOAPException if the SOAP Header does not exist or cannot be
     *                                      retrieved
     */
    public SOAPHeader getSOAPHeader() throws SOAPException {
        return this.soapPart.getEnvelope().getHeader();
    }

    /**
     * Gets the SOAP Body contained in this <code>SOAPMessage</code> object.
     *
     * @return the <code>SOAPBody</code> object contained by this <code>SOAPMessage</code> object
     * @throws javax.xml.soap.SOAPException if the SOAP Body does not exist or cannot be retrieved
     */
    public SOAPBody getSOAPBody() throws SOAPException {
        return this.soapPart.getEnvelope().getBody();
    }

    /**
     * Set the character encoding based on the <code>contentType</code> parameter
     *
     * @param contentType
     */
    private void initCharsetEncodingFromContentType(final String contentType) {
        if (contentType != null) {
            int delimiterIndex = contentType.lastIndexOf("charset");
            if (delimiterIndex > 0) {
                String charsetPart = contentType.substring(delimiterIndex);
                int charsetIndex = charsetPart.indexOf('=');
                String charset = charsetPart.substring(charsetIndex + 1).trim();
                if ((charset.startsWith("\"") || charset.startsWith("\'"))) {
                    charset = charset.substring(1, charset.length());
                }
                if ((charset.endsWith("\"") || charset.endsWith("\'"))) {
                    charset = charset.substring(0, charset.length() - 1);
                }
                int index = charset.indexOf(';');
                if (index != -1) {
                    charset = charset.substring(0, index);
                }
                setProperty(SOAPMessage.CHARACTER_SET_ENCODING, charset);
            }
        }
    }
}
