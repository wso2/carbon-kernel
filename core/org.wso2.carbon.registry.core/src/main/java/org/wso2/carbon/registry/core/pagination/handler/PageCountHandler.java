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

package org.wso2.carbon.registry.core.pagination.handler;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.registry.core.pagination.PaginationConstants;

/**
 This handler used to set the total count of the artifacts.
 */
public class PageCountHandler extends AbstractHandler implements Handler {
    @Override
    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
        String rowCount = (String) messageContext.getProperty(
                PaginationConstants.PAGINATION_HEADER_CONTEXT_ROW_COUNT);

        if (rowCount != null) {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            messageContext.getEnvelope().getHeader().addHeaderBlock(
                    PaginationConstants.PAGINATION_HEADER_CONTEXT_ROW_COUNT,
                    fac.createOMNamespace(PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE,
                            PaginationConstants.PAGINATION_HEADER_ELEMENT_NAMESPACE_PREFIX)).
                    setText(rowCount);
        }
        return InvocationResponse.CONTINUE;
    }
}
