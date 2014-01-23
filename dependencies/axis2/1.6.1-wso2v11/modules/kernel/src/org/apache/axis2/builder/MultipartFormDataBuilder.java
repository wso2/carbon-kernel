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

package org.apache.axis2.builder;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.MultipleEntryHashMap;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class MultipartFormDataBuilder implements Builder {

    /**
     * @return Returns the document element.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        MultipleEntryHashMap parameterMap;
        HttpServletRequest request = (HttpServletRequest) messageContext
                .getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);

        // TODO: Do check ContentLength for the max size,
        //       but it can't be configured anywhere.
        //       I think that it cant be configured at web.xml or axis2.xml.

        /**
         * When we are building a request context without the use of Servlets, we require charset encoding and content length
         * parameters to be set in the transports.
         */
        String charSetEncoding = (String)messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
        Integer contentLength = 0;

        Map<String, String> transportHeaders = (Map) messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        String contentLengthValue = (String) transportHeaders.get(HTTPConstants.HEADER_CONTENT_LENGTH);
        if (contentLengthValue != null) {
            try {
                contentLength = new Integer(contentLengthValue);
            } catch (NumberFormatException e) {
                // TODO handle this better in case we cannot find the contentLength
            }
        }

        RequestContextImpl nRequest;

        if (request == null) { // on regular transport
            if(charSetEncoding == null || contentLength == null) {
               throw new AxisFault("multipart/form-data builder could not find charset encoding or content length in messageContext TRANSPORT_HEADERS. Please set these in the respective transport in use.");
            }
            nRequest = new RequestContextImpl(inputStream, contentType, charSetEncoding, contentLength);
        } else { // from servlet transport
            nRequest = new RequestContextImpl(inputStream, request.getContentType(), request.getCharacterEncoding(), request.getContentLength());
        }

        try {
            parameterMap = getParameterMap(nRequest, charSetEncoding);
            return BuilderUtil.buildsoapMessage(messageContext, parameterMap,
                   OMAbstractFactory.getSOAP12Factory());

        } catch (FileUploadException e) {
            throw AxisFault.makeFault(e);
        }

    }

    private MultipleEntryHashMap getParameterMap(RequestContext request,
                                                 String charSetEncoding)
            throws FileUploadException {

        MultipleEntryHashMap parameterMap = new MultipleEntryHashMap();

        List items = parseRequest(request);
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            DiskFileItem diskFileItem = (DiskFileItem)iter.next();

            boolean isFormField = diskFileItem.isFormField();

            Object value;
            try {
                if (isFormField) {
                    value = getTextParameter(diskFileItem, charSetEncoding);
                } else {
                    value = getFileParameter(diskFileItem);
                }
            } catch (Exception ex) {
                throw new FileUploadException(ex.getMessage());
            }
            parameterMap.put(diskFileItem.getFieldName(), value);
        }

        return parameterMap;
    }

    private static List parseRequest(RequestContext requestContext)
            throws FileUploadException {
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // Parse the request
        return upload.parseRequest(requestContext);
    }

    private String getTextParameter(DiskFileItem diskFileItem,
                                    String characterEncoding) throws Exception {

        String encoding = diskFileItem.getCharSet();

        if (encoding == null) {
            encoding = characterEncoding;
        }

        String textValue;
        if (encoding == null) {
            textValue = new String(diskFileItem.get());
        } else {
            textValue = new String(diskFileItem.get(), encoding);
        }

        return textValue;
    }

    private DataHandler getFileParameter(DiskFileItem diskFileItem)
            throws Exception {

        DataSource dataSource = new DiskFileDataSource(diskFileItem);
        DataHandler dataHandler = new DataHandler(dataSource);

        return dataHandler;
    }
}
