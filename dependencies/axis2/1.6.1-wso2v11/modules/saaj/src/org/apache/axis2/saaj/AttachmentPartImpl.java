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

import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.impl.dom.DocumentImpl;
import org.apache.axiom.om.impl.dom.TextImpl;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.saaj.util.SAAJDataSource;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.activation.DataHandler;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.transform.stream.StreamSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * 
 */
public class AttachmentPartImpl extends AttachmentPart {
    private DataHandler dataHandler;

    private MimeHeaders mimeHeaders = new MimeHeaders();
    private String attachmentFile;

    private OMText omText;
    private boolean isAttachmentReferenced;

    /**
     * Check whether at least one of the headers of this object matches a provided header
     *
     * @param headers
     * @return <b>true</b> if at least one header of this AttachmentPart matches a header in the
     *         provided <code>headers</code> parameter, <b>false</b> if none of the headers of this
     *         AttachmentPart matches at least one of the header in the provided
     *         <code>headers</code> parameter
     */
    public boolean matches(MimeHeaders headers) {
        for (Iterator i = headers.getAllHeaders(); i.hasNext();) {
            MimeHeader hdr = (javax.xml.soap.MimeHeader)i.next();
            String values[] = mimeHeaders.getHeader(hdr.getName());
            boolean found = false;
            if (values != null) {
                for (int j = 0; j < values.length; j++) {
                    if (!hdr.getValue().equalsIgnoreCase(values[j])) {
                        continue;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public boolean isAttachmentReferenced() {
        return isAttachmentReferenced;
    }

    public void setAttachmentReferenced(boolean attachmentReferenced) {
        isAttachmentReferenced = attachmentReferenced;
    }

    /**
     * Returns the number of bytes in this <CODE> AttachmentPart</CODE> object.
     *
     * @return the size of this <CODE>AttachmentPart</CODE> object in bytes or -1 if the size cannot
     *         be determined
     * @throws javax.xml.soap.SOAPException if the content of this attachment is corrupted of if
     *                                      there was an exception while trying to determine the
     *                                      size.
     */
    public int getSize() throws SOAPException {
        if (dataHandler == null) {
            return 0;
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            dataHandler.writeTo(bout);
        } catch (Exception ex) {
            throw new SOAPException(ex);
        }
        return bout.size();
    }

    /**
     * Clears out the content of this <CODE> AttachmentPart</CODE> object. The MIME header portion
     * is left untouched.
     */
    public void clearContent() {
        dataHandler = null;
        omText = null;
    }

    /**
     * Gets the content of this <code>AttachmentPart</code> object as a Java object. The type of the
     * returned Java object depends on <ol> <li> the <code>DataContentHandler</code> object that is
     * used to interpret the bytes </li> <li> the <code>Content-Type</code> given in the header</li>
     * </ol>
     * <p/>
     * For the MIME content types "text/plain", "text/html" and "text/xml", the
     * <code>DataContentHandler</code> object does the conversions to and from the Java types
     * corresponding to the MIME types. For other MIME types,the <code>DataContentHandler</code>
     * object can return an <code>InputStream</code> object that contains the content data as raw
     * bytes.
     * <p/>
     * A JAXM-compliant implementation must, as a minimum, return a <code>java.lang.String</code>
     * object corresponding to any content stream with a <code>Content-Type</code> value of
     * <code>text/plain</code>, a <code>javax.xml.transform.StreamSource</code> object corresponding
     * to a content stream with a <code>Content-Type</code> value of <code>text/xml</code>, a
     * <code>java.awt.Image</code> object corresponding to a content stream with a
     * <code>Content-Type</code> value of <code>image/gif</code> or <code>image/jpeg</code>.  For
     * those content types that an installed <code>DataContentHandler</code> object does not
     * understand, the <code>DataContentHandler</code> object is required to return a
     * <code>java.io.InputStream</code> object with the raw bytes.
     *
     * @return a Java object with the content of this <CODE> AttachmentPart</CODE> object
     * @throws javax.xml.soap.SOAPException if there is no content set into this <CODE>AttachmentPart</CODE>
     *                                      object or if there was a data transformation error
     */
    public Object getContent() throws SOAPException {
        if (dataHandler == null) {
            throw new SOAPException("No content is present in this AttachmentPart");
        }
        try {
            String contentType = dataHandler.getContentType();
            if (contentType.equals(HTTPConstants.MEDIA_TYPE_TEXT_XML) ||
                    contentType.equals(HTTPConstants.MEDIA_TYPE_APPLICATION_XML)) {
                StreamSource streamSource = new StreamSource();
                streamSource.setInputStream(dataHandler.getInputStream());
                return streamSource;
            } else if (contentType.equals("text/plain") ||
                    contentType.equals("text/html")) {
                return (String)dataHandler.getContent();
            } else {
                try {
                    return dataHandler.getContent();
                } catch (Exception e) {
                    //If the underlying DataContentHandler can't handle the object contents,
                    //we will return an inputstream of raw bytes representing the content data
                    return dataHandler.getDataSource().getInputStream();
                }
            }
        } catch (IOException e) {
            throw new SOAPException(e.getMessage());
        }
    }

    /**
     * Sets the content of this attachment part to that of the given <CODE>Object</CODE> and sets
     * the value of the <CODE> Content-Type</CODE> header to the given type. The type of the
     * <CODE>Object</CODE> should correspond to the value given for the <CODE>Content-Type</CODE>.
     * This depends on the particular set of <CODE>DataContentHandler</CODE> objects in use.
     *
     * @param object      the Java object that makes up the content for this attachment part
     * @param contentType the MIME string that specifies the type of the content
     * @throws IllegalArgumentException if the contentType does not match the type of the content
     *                                  object, or if there was no <CODE> DataContentHandler</CODE>
     *                                  object for this content object
     * @see #getContent()
     */
    public void setContent(Object object, String contentType) {
        SAAJDataSource source;
        setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);
        if (object instanceof String) {
            try {
                String s = (String)object;
                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(s.getBytes());
                source = new SAAJDataSource(bais,
                                            SAAJDataSource.MAX_MEMORY_DISK_CACHED,
                                            contentType, true);
                extractFilename(source);
                this.dataHandler = new DataHandler(source);
            } catch (java.io.IOException io) {
                throw new java.lang.IllegalArgumentException("Illegal Argument");
            }
        } else if (object instanceof java.io.InputStream) {
            try {

                source = new SAAJDataSource((java.io.InputStream)object,
                                            SAAJDataSource.MIN_MEMORY_DISK_CACHED,
                                            contentType, true);
                extractFilename(source);
                this.dataHandler = new DataHandler(source);
            } catch (java.io.IOException io) {
                throw new java.lang.IllegalArgumentException("Illegal Argument");
            }
        } else if (object instanceof StreamSource) {
            try {
                source = new SAAJDataSource(((StreamSource)object).getInputStream(),
                                            SAAJDataSource.MAX_MEMORY_DISK_CACHED,
                                            contentType, true);
                extractFilename(source);
                this.dataHandler = new DataHandler(source);
            } catch (java.io.IOException io) {
                throw new java.lang.IllegalArgumentException("Illegal Argument");
            }
        } else if (object instanceof BufferedImage) {
            try {
                this.dataHandler = new DataHandler(object, contentType);
            } catch (Exception e) {
                throw new java.lang.IllegalArgumentException(e.getMessage());
            }
        } else if (object instanceof byte[]) {
            try {
                java.io.ByteArrayInputStream bais =
                        new java.io.ByteArrayInputStream((byte[])object);
                source = new SAAJDataSource(bais,
                                            SAAJDataSource.MAX_MEMORY_DISK_CACHED,
                                            contentType, true);
                extractFilename(source);

                this.dataHandler = new DataHandler(source);
            } catch (Exception e) {
                throw new java.lang.IllegalArgumentException(e.getMessage());
            }
        } else {
            throw new java.lang.IllegalArgumentException("Illegal Argument");
        }
    }

    /**
     * Gets the <CODE>DataHandler</CODE> object for this <CODE> AttachmentPart</CODE> object.
     *
     * @return the <CODE>DataHandler</CODE> object associated with this <CODE>AttachmentPart</CODE>
     *         object
     * @throws javax.xml.soap.SOAPException if there is no data in this <CODE>AttachmentPart</CODE>
     *                                      object
     */
    public DataHandler getDataHandler() throws SOAPException {
        //if (getContent() == null) {
        //    throw new SOAPException("No Content present in the Attachment part");
        //}
        //commented to fix AXIS2-778
        if (dataHandler == null) {
            throw new SOAPException("No Content present in the Attachment part");
        }

        return dataHandler;
    }

    /**
     * Sets the given <CODE>DataHandler</CODE> object as the data handler for this
     * <CODE>AttachmentPart</CODE> object. Typically, on an incoming message, the data handler is
     * automatically set. When a message is being created and populated with content, the
     * <CODE>setDataHandler</CODE> method can be used to get data from various data sources into the
     * message.
     *
     * @param datahandler <CODE>DataHandler</CODE> object to be set
     * @throws IllegalArgumentException if there was a problem with the specified <CODE>
     *                                  DataHandler</CODE> object
     */
    public void setDataHandler(DataHandler datahandler) {
        if (datahandler != null) {
            this.dataHandler = datahandler;
            setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, datahandler.getContentType());
            omText = DOOMAbstractFactory.getOMFactory().createOMText(datahandler, true);
        } else {
            throw new IllegalArgumentException("Cannot set null DataHandler");
        }
    }

    /**
     * Removes all MIME headers that match the given name.
     *
     * @param header - the string name of the MIME header/s to be removed
     */
    public void removeMimeHeader(String header) {
        mimeHeaders.removeHeader(header);
    }

    /** Removes all the MIME header entries. */
    public void removeAllMimeHeaders() {
        mimeHeaders.removeAllHeaders();
    }

    /**
     * Gets all the values of the header identified by the given <CODE>String</CODE>.
     *
     * @param name the name of the header; example: "Content-Type"
     * @return a <CODE>String</CODE> array giving the value for the specified header
     * @see #setMimeHeader(String, String) setMimeHeader(java.lang.String, java.lang.String)
     */
    public String[] getMimeHeader(String name) {
        return mimeHeaders.getHeader(name);
    }

    /**
     * Changes the first header entry that matches the given name to the given value, adding a new
     * header if no existing header matches. This method also removes all matching headers but the
     * first.
     * <p/>
     * <P>Note that RFC822 headers can only contain US-ASCII characters.</P>
     *
     * @param name  a <CODE>String</CODE> giving the name of the header for which to search
     * @param value a <CODE>String</CODE> giving the value to be set for the header whose name
     *              matches the given name
     * @throws IllegalArgumentException if there was a problem with the specified mime header name
     *                                  or value
     */
    public void setMimeHeader(String name, String value) {
        mimeHeaders.setHeader(name, value);
    }

    /**
     * Adds a MIME header with the specified name and value to this <CODE>AttachmentPart</CODE>
     * object.
     * <p/>
     * <P>Note that RFC822 headers can contain only US-ASCII characters.</P>
     *
     * @param name  a <CODE>String</CODE> giving the name of the header to be added
     * @param value a <CODE>String</CODE> giving the value of the header to be added
     * @throws IllegalArgumentException if there was a problem with the specified mime header name
     *                                  or value
     */
    public void addMimeHeader(String name, String value) {
        mimeHeaders.addHeader(name, value);
    }

    /**
     * Retrieves all the headers for this <CODE> AttachmentPart</CODE> object as an iterator over
     * the <CODE> MimeHeader</CODE> objects.
     *
     * @return an <CODE>Iterator</CODE> object with all of the Mime headers for this
     *         <CODE>AttachmentPart</CODE> object
     */
    public Iterator getAllMimeHeaders() {
        return mimeHeaders.getAllHeaders();
    }

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects that match a name in the given array.
     *
     * @param names a <CODE>String</CODE> array with the name(s) of the MIME headers to be returned
     * @return all of the MIME headers that match one of the names in the given array as an
     *         <CODE>Iterator</CODE> object
     */
    public Iterator getMatchingMimeHeaders(String names[]) {
        return mimeHeaders.getMatchingHeaders(names);
    }

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects whose name does not match a name in the given
     * array.
     *
     * @param names a <CODE>String</CODE> array with the name(s) of the MIME headers not to be
     *              returned
     * @return all of the MIME headers in this <CODE> AttachmentPart</CODE> object except those that
     *         match one of the names in the given array. The nonmatching MIME headers are returned
     *         as an <CODE>Iterator</CODE> object.
     */
    public Iterator getNonMatchingMimeHeaders(String names[]) {
        return mimeHeaders.getNonMatchingHeaders(names);
    }

    public InputStream getBase64Content() throws SOAPException {
        byte[] rawData = getRawContentBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Base64.encode(rawData, 0, rawData.length, out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new SOAPException(e);
        }
    }


    /**
     * Gets the content of this AttachmentPart object as an InputStream as if a call had been made
     * to getContent and no DataContentHandler had been registered for the content-type of this
     * AttachmentPart.Note that reading from the returned InputStream would result in consuming the
     * data in the stream. It is the responsibility of the caller to reset the InputStream
     * appropriately before calling a Subsequent API. If a copy of the raw attachment content is
     * required then the getRawContentBytes() API should be used instead.
     *
     * @return an InputStream from which the raw data contained by the AttachmentPart can be
     *         accessed.
     * @throws SOAPException - if there is no content set into this AttachmentPart object or if
     *                       there was a data transformation error.
     * @since SAAJ 1.3
     */
    public InputStream getRawContent() throws SOAPException {
        try {
            if (dataHandler == null) {
                throw new SOAPException("No content set");
            }
            return dataHandler.getInputStream();
        } catch (IOException e) {
            throw new SOAPException(e);
        }
    }

    /**
     * Gets the content of this AttachmentPart object as a byte[] array as if a call had been made
     * to getContent and no DataContentHandler had been registered for the content-type of this
     * AttachmentPart.
     *
     * @return a byte[] array containing the raw data of the AttachmentPart.
     * @throws SOAPException - if there is no content set into this AttachmentPart object or if
     *                       there was a data transformation error.
     * @since SAAJ 1.3
     */
    public byte[] getRawContentBytes() throws SOAPException {
        if (dataHandler == null) {
            throw new SOAPException("Content is null");
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            dataHandler.writeTo(bout);
        } catch (Exception ex) {
            throw new SOAPException(ex);
        }
        return bout.toByteArray();
    }


    /**
     * Sets the content of this attachment part from the Base64 source InputStream and sets the
     * value of the Content-Type header to the value contained in contentType, This method would
     * first decode the base64 input and write the resulting raw bytes to the attachment. A
     * subsequent call to getSize() may not be an exact measure of the content size.
     *
     * @param content - the base64 encoded data to add to the attachment part contentType - the
     *                value to set into the Content-Type header
     * @throws SOAPException - if there is an error in setting the content java.lang.NullPointerException
     *                       - if content is null
     */
    public void setBase64Content(InputStream content, String contentType) throws SOAPException {
        if (content == null) {
            throw new SOAPException("Content is null");
        }
        OutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = content.read(buffer, 0, buffer.length)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            String contentString = outputStream.toString();
            if (Base64.isValidBase64Encoding(contentString)) {
                setContent(Base64.decode(contentString), contentType);
            } else {
                throw new SOAPException("Not a valid Base64 encoding");
            }
        } catch (IOException ex) {
            throw new SOAPException(ex);
        }
    }

    /**
     * Sets the content of this attachment part to that contained by the InputStream content and
     * sets the value of the Content-Type header to the value contained in contentType.A subsequent
     * call to getSize() may not be an exact measure of the content size.
     *
     * @param content - the raw data to add to the attachment part contentType - the value to set
     *                into the Content-Type header
     * @throws SOAPException - if there is an error in setting the content java.lang.NullPointerException
     *                       - if content is null
     */
    public void setRawContent(InputStream content, String contentType) throws SOAPException {
        if (content == null) {
            throw new SOAPException("content is null");
        }
        setContent(content, contentType);
    }


    /**
     * Sets the content of this attachment part to that contained by the byte[] array content and
     * sets the value of the Content-Type header to the value contained in contentType.
     *
     * @param content - the raw data to add to the attachment part contentType - the value to set
     *                into the Content-Type header offset - the offset in the byte array of the
     *                content len - the number of bytes that form the content
     * @throws SOAPException - if an there is an error in setting the content or content is null
     * @since SAAJ 1.3
     */

    public void setRawContentBytes(byte[] content, int offset, int len, String contentType)
            throws SOAPException {
        //TODO - how to use offset & len?
        if (content == null) {
            throw new SOAPException("Content is null");
        }
        setContent(content, contentType);
    }

    /**
     * Retrieve the OMText
     *
     * @return the OMText
     * @throws SOAPException If omText is not available
     */
    public OMText getOMText() throws SOAPException {
        if (omText == null) {
            throw new SOAPException("OMText set to null");
        }
        return omText;
    }

    public TextImpl getText(DocumentImpl doc) {
        return new TextImpl(doc, omText.getText(), doc.getOMFactory());
    }

    /**
     * Set the filename of this attachment part.
     *
     * @param path the new file path
     */
    protected void setAttachmentFile(String path) {
        attachmentFile = path;
    }

    /**
     * Detach the attachment file from this class, so it is not cleaned up. This has the side-effect
     * of making subsequent calls to getAttachmentFile() return <code>null</code>.
     */
    public void detachAttachmentFile() {
        attachmentFile = null;
    }

    /**
     * Get the filename of this attachment.
     *
     * @return the filename or null for an uncached file
     */
    public String getAttachmentFile() {
        return attachmentFile;
    }

    private void extractFilename(SAAJDataSource source) {
        if (source.getDiskCacheFile() != null) {
            String path = source.getDiskCacheFile().getAbsolutePath();
            setAttachmentFile(path);
        }
    }
}
