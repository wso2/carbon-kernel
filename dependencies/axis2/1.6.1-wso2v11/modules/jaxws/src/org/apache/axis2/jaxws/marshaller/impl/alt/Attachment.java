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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.AttachmentDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.utility.ConvertUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePartDataSource;
import javax.xml.transform.Source;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * The Attachment object has a similar function as the 
 * org.apache.axis2.jaxws.marshaller.impl.alt.Element object.
 * The Element object is used to 
 *    1) Get the "element rendered" object that is marshalled/unmarshalled from the wire
 *    2) Get the "type rendered" object that is used as the parameter value in the signature.
 *    
 * The Attachment object has a similar role.  
 * The DataHandler object is used for marshalling/unmarshalling.
 * And the getValue method is used to get the signature representation.
 * 
 *
 */
class Attachment {
    private static final Log log = LogFactory.getLog(Attachment.class);
    DataHandler dh = null;
    String cid = null;
    AttachmentDescription aDesc = null;
    Object sigValue = null;
    Class sigClass = null;
    String partName = null;

    /**
     * Constructor used to set Attachment from wire unmarshalling
     * @param dh
     * @param cid
     */
    public Attachment(DataHandler dh, String cid) {
        if (log.isDebugEnabled()) {
            System.out.println("Construct with dh=" + dh.getContentType() + " cid=" + cid);
        }
        this.dh = dh;
        this.cid = cid;
    }

    /**
     * Constructor used to create Attachment from signature data
     * @param sigValue
     * @param sigClass
     */
    public Attachment(Object sigValue, Class sigClass, AttachmentDescription aDesc, String partName) {
        this.sigValue = sigValue;
        this.sigClass = sigClass;
        this.aDesc = aDesc;
        this.partName = partName;
    }


    /**
     * @return DataHandler
     */
    public DataHandler getDataHandler() {
        if (dh == null) {
        	//if null DH was specified explicitly, return it, don't create something else.
            if (sigValue == null) {
                // Create a contentID and null DataHandler
                getContentID(); // Force setting of content id
                dh = (DataHandler) null;
            } else {
                // Normal case: create a DataHandler from the sigValue object
                dh = createDataHandler(sigValue, sigClass, aDesc.getMimeTypes(), getContentID());
            }
        }
        return dh;
    }

    /**
     * @return ContentID
     */
    public String getContentID() {
        if (cid == null) {
            cid = UUIDGenerator.getUUID();
            // Per spec, use the partName in the content-id
            // http://www.ws-i.org/Profiles/AttachmentsProfile-1.0.html#Value-space_of_Content-Id_Header
            if (partName != null) {
                cid = partName + "=" + cid;
            }
        }
        return cid;
    }

    private static DataHandler createDataHandler(Object value, Class cls, String[] mimeTypes,
                                                 String cid) {
        if (log.isDebugEnabled()) {
            System.out.println("Construct data handler for " + cls + " cid=" + cid);
        }
        DataHandler dh = null;
        if (cls.isAssignableFrom(DataHandler.class)) {
            dh = (DataHandler) value;
            if(dh == null)  
            {
                return dh; //return if DataHandler is null
            }

            try {
                Object content = dh.getContent();
                // If the content is a Source, convert to a String due to 
                // problems with the DataContentHandler
                if (content instanceof Source) {
                    if (log.isDebugEnabled()) {
                        System.out.println("Converting DataHandler Source content to "
                                + "DataHandlerString content");
                    }
                    byte[] bytes = (byte[]) ConvertUtils.convert(content, byte[].class);
                    String newContent = new String(bytes);
                    return new DataHandler(newContent, mimeTypes[0]);
                }
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        } else {
            try {
                byte[] bytes = createBytes(value, cls, mimeTypes);
                // Create MIME Body Part
                InternetHeaders ih = new InternetHeaders();
                ih.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, mimeTypes[0]);
                MimeBodyPart mbp = new MimeBodyPart(ih, bytes);

                //Create a data source for the MIME Body Part
                MimePartDataSource ds = new MimePartDataSource(mbp);

                dh = new DataHandler(ds);
                mbp.setHeader(HTTPConstants.HEADER_CONTENT_ID, cid);
            } catch (Exception e) {
                throw ExceptionFactory.makeWebServiceException(e);
            }
        }
        return dh;
    }

    private static byte[] createBytes(Object value, Class cls, String[] mimeTypes) {
        if (cls.isAssignableFrom(byte[].class)) {
            return (byte[]) value;
        } else if (cls.isAssignableFrom(String.class)) {
            return ((String) value).getBytes();
        } else if (cls.isAssignableFrom(Image.class)) {
            return createBytesFromImage((Image) value, mimeTypes[0]);
        } else if (ConvertUtils.isConvertable(value, byte[].class)) {
            return (byte[]) ConvertUtils.convert(value, byte[].class);
        } else {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("convertProblem",
                                                                               cls.getName(),
                                                                               "byte[]"));
        }
    }
    private static byte[] createBytesFromImage(Image image, String mimeType)  {
        try {
            ImageWriter imageWriter = null;
            BufferedImage bufferedImage = (BufferedImage) image;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Iterator iterator = javax.imageio.ImageIO.getImageWritersByMIMEType(mimeType);
            if (iterator.hasNext()) {
                imageWriter = (ImageWriter) iterator.next();
            }
            ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(baos);
            imageWriter.setOutput(ios);
            imageWriter.write(new IIOImage(bufferedImage, null, null));
            ios.flush();
            imageWriter.dispose();
            return baos.toByteArray();
        } catch (IOException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }

    }
}
