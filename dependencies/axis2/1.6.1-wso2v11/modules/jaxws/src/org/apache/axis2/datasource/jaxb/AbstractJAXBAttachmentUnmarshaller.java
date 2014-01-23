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

package org.apache.axis2.datasource.jaxb;

import org.apache.axiom.om.OMException;
import org.apache.axiom.util.stax.xop.MimePartProvider;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract base class for {@link JAXBAttachmentUnmarshaller} and
 * {@link org.apache.axis2.jaxws.message.attachments.JAXBAttachmentUnmarshaller}. This is a partial
 * implementation of the {@link AttachmentUnmarshaller} class. It handles the attachments provided
 * by Axiom through the {@link MimePartProvider} interface. It should be noted that while Axiom only
 * handles attachments referenced using XOP, {@link AttachmentUnmarshaller} is also used to retrieve
 * attachments from SwA messages. Hence the {@link #getDataHandlerForSwA(String)} method.
 */
public abstract class AbstractJAXBAttachmentUnmarshaller extends AttachmentUnmarshaller {

    private static final Log log = LogFactory.getLog(AbstractJAXBAttachmentUnmarshaller.class);

    private final MimePartProvider mimePartProvider;

    public AbstractJAXBAttachmentUnmarshaller(MimePartProvider mimePartProvider) {
        this.mimePartProvider = mimePartProvider;
    }

    public final boolean isXOPPackage() {
        
        // Any message that is received might contain MTOM.
        // So always return true.
        boolean value = true;
    
        if (log.isDebugEnabled()){ 
            log.debug("isXOPPackage returns " + value);
        }
        return value;
    }

    public final byte[] getAttachmentAsByteArray(String cid) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve attachment [" + cid + "] as a byte[]");
        }
        DataHandler dh = getAttachmentAsDataHandler(cid);
        if (dh != null) {
            try {
                return convert(dh);
            } catch (IOException ioe) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred while getting the byte[] " + ioe);
                }
                throw new OMException(ioe);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("returning null byte[]");
        }
        return null;
    }

    public final DataHandler getAttachmentAsDataHandler(String cid) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve attachment [" + cid + "] as a DataHandler");
        }

        DataHandler dh = getDataHandler(cid);
        if (dh != null) {
            return dh;
        } else {
            String cid2 = getNewCID(cid);
            if (log.isDebugEnabled()) {
                log.debug("A dataHandler was not found for [" + cid + "] trying [" + cid2 + "]");
            }
            dh = getDataHandler(cid2);
            if (dh != null) {
                return dh;
            }
        }
        // No Data Handler found
        throw new OMException(Messages.getMessage("noDataHandler", cid));
    }
    
    /**
     * @param cid
     * @return cid with translated characters
     */
    private String getNewCID(String cid) {
        String cid2 = cid;

        try {
            cid2 = java.net.URLDecoder.decode(cid, "UTF-8");
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("getNewCID decoding " + cid + " as UTF-8 decoding error: " + e);
            }
        }
        return cid2;
    }

    /**
     * Read the bytes from the DataHandler
     * 
     * @param dh
     * @return byte[]
     * @throws IOException
     */
    private byte[] convert(DataHandler dh) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Reading byte[] from DataHandler " + dh);
        }
        InputStream is = dh.getInputStream();
        if (log.isDebugEnabled()) {
            log.debug("DataHandler InputStream " + is);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int num = is.read(b);
        if (log.isDebugEnabled()) {
            if (num <= 0) {
                log.debug("DataHandler InputStream contains no data. num=" + num);
            }
        }
        while (num > 0) {
            baos.write(b, 0, num);
            num = is.read(b);
        }
        return baos.toByteArray();
    }
    
    private DataHandler getDataHandler(String cid) {
        String blobcid = cid;
        if (blobcid.startsWith("cid:")) {
            blobcid = blobcid.substring(4);
        }
        DataHandler dh;
        try {
            dh = mimePartProvider.getDataHandler(blobcid);
        } catch (IllegalArgumentException ex) {
            dh = null;
        } catch (IOException ex) {
            throw new OMException("Failed to load attachment with content ID " + blobcid, ex);
        }
        if (dh == null) {
            dh = getDataHandlerForSwA(blobcid);
        }
        if (dh != null) {
            JAXBAttachmentUnmarshallerMonitor.addBlobCID(blobcid);
        }
        return dh;
    }
    
    protected abstract DataHandler getDataHandlerForSwA(String blobcid);
}
