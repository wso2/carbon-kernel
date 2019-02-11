/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.pagination;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaginationUtils {

   public final static Log log = LogFactory.getLog(PaginationUtils.class);

    /**
     * Copy the pagination context details to ServiceClient.
     *
     * @param serviceClient ServiceClient
     * @throws org.apache.axis2.AxisFault if failed to pagination details
     */
    public static void copyPaginationContext(ServiceClient serviceClient) throws AxisFault {
        //set start value
        serviceClient.addStringHeader(new QName(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE,
                PaginationConstants.PAGINATION_HEADER_CONTEXT_START,
                PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX),
                Integer.toString(PaginationContext.getInstance().getStart()));
        //set count value
        serviceClient.addStringHeader(new QName(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE,
                PaginationConstants.PAGINATION_HEADER_CONTEXT_COUNT,
                PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX),
                Integer.toString(PaginationContext.getInstance().getCount()));
        //set sortOrder value
        serviceClient.addStringHeader(new QName(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE,
                PaginationConstants.PAGINATION_HEADER_CONTEXT_SORT_ORDER,
                PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX),
                PaginationContext.getInstance().getSortOrder());
        //set sortBy value
        serviceClient.addStringHeader(new QName(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE,
                PaginationConstants.PAGINATION_HEADER_CONTEXT_SORT_BY,
                PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX),
                PaginationContext.getInstance().getSortBy());

        serviceClient.addStringHeader(new QName(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE,
                PaginationConstants.PAGINATION_HEADER_CONTEXT_LIMIT,
                PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX),
                Integer.toString(PaginationContext.getInstance().getLimit()));
    }

    /**
     * Create PaginationContext from MessageContext
     *
     * @param messageContext MessageContext
     * @return PaginationContext
     */
    public static PaginationContext initPaginationContext(MessageContext messageContext) {
        Map<String, String> map = new HashMap<String, String>();

        SOAPHeader header = MessageContext.getCurrentMessageContext().getEnvelope().getHeader();
        ArrayList elements = header.getHeaderBlocksWithNSURI(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE);

        for (Object element : elements) {
            map.put(((OMElement) element).getLocalName(), ((OMElement) element).getText());
        }

        return PaginationContext.init(
                Integer.parseInt(map.get(PaginationConstants.PAGINATION_HEADER_CONTEXT_START)),
                Integer.parseInt(map.get(PaginationConstants.PAGINATION_HEADER_CONTEXT_COUNT)),
                map.get(PaginationConstants.PAGINATION_HEADER_CONTEXT_SORT_ORDER),
                map.get(PaginationConstants.PAGINATION_HEADER_CONTEXT_SORT_BY),
                Integer.parseInt(map.get(PaginationConstants.PAGINATION_HEADER_CONTEXT_LIMIT)));
    }

    /**
     * Check the availability of the pagination details in messageContext.
     * @param messageContext MessageContext
     * @return  true if pagination headers exist.
     */
    public static boolean isPaginationHeadersExist(MessageContext messageContext){
        SOAPHeader header = messageContext.getEnvelope().getHeader();
        if(header == null){
            return false;
        }
        ArrayList elements = header.getHeaderBlocksWithNSURI(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE);

        return elements != null && elements.size() > 0;
    }

    /**
     * Check the resource get method is paginated or not.
     * @param annotation method annotation  name
     * @return true if the method id paginated.
     */
    public static boolean isPaginationAnnotationFound(String annotation) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Method[] methods = Class.forName(element.getClassName()).getMethods();
                for (Method method : methods) {
                    if (method.getName().equals(element.getMethodName()) &&
                            method.isAnnotationPresent(Paginate.class) &&
                            method.getAnnotation(Paginate.class).value().equals(annotation)) {
                        return true;
                    }
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
        return false;
    }

    /**
     * Set row count to messageContext as property.
     * @param messageContext  MessageContext
     * @param count row count for pagination
     */
    public static void setRowCount(MessageContext messageContext,String count)  {
        try {
            messageContext.getOperationContext().
                    getMessageContext("Out").setProperty(PaginationConstants.PAGINATION_HEADER_CONTEXT_ROW_COUNT, count);
        } catch (AxisFault axisFault) {
            log.error("Failed to set row count "+ axisFault);
        }

    }

    public static int getRowCount(ServiceClient client) throws AxisFault {
        int rowCount = 0;
        try {
            ArrayList elements = client.getLastOperationContext().getMessageContext("In").
                    getEnvelope().getHeader().
                    getHeaderBlocksWithNSURI(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE);
            if (elements != null && elements.size() > 0) {
                for (Object element : elements) {
                    if (((OMElement) element).getLocalName().equals(PaginationConstants.PAGINATION_HEADER_CONTEXT_ROW_COUNT)) {
                        rowCount = Integer.parseInt(((OMElement) element).getText());
                        break;
                    }
                }
            }
        } finally {
            PaginationContext.destroy();
        }
        return rowCount;
    }

}
