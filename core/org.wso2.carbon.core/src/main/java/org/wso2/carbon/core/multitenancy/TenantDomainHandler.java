/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.core.multitenancy;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Handles tenant domain
 *
 */
public class TenantDomainHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        EndpointReference epr = msgContext.getTo();
        if (epr != null) {
            String toAddress = epr.getAddress();
            if (toAddress != null) {
                int tenantDelimiterIndex = toAddress.indexOf("/a/");
                if (tenantDelimiterIndex != -1) {
                    String temp = toAddress.substring(tenantDelimiterIndex + 3);
                    tenantDomain = temp.substring(0, temp.indexOf('/'));
                    if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        carbonContext.setTenantDomain(tenantDomain);
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
                    }
                    //remove the domain details so that it can be dispatched
                    String newAddress = toAddress.replace("/a/" + tenantDomain, "");
                    EndpointReference newEpr = new EndpointReference(newAddress);
                    msgContext.setTo(newEpr);
                    msgContext.getOptions().setTo(newEpr);
                }else {
                    if (carbonContext.getTenantId() == MultitenantConstants.INVALID_TENANT_ID) {
                        carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                    }
                }
            }
        }

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            // try to dispatch from the soap header.
            SOAPHeader soapHeader = msgContext.getEnvelope().getHeader();
            if (soapHeader != null) {
                OMElement tenantDomainElement =
                        soapHeader.getFirstChildWithName(
                                new QName(MultitenantConstants.TENANT_DOMAIN_HEADER_NAMESPACE,
                                        MultitenantConstants.TENANT_DOMAIN_HEADER_NAME));
                if (tenantDomainElement != null) {
                    tenantDomain = tenantDomainElement.getText();
                }
            }

        }

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            Map<String, String> transportHeaders =
                    (Map<String, String>) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
            if (transportHeaders != null) {
                String tenantDomainHeader = transportHeaders.get(MultitenantConstants.TENANT_DOMAIN_HEADER_NAME);
                if (tenantDomainHeader != null) {
                    tenantDomain = tenantDomainHeader;
                }
            }
        }
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            carbonContext.setTenantDomain(tenantDomain);
        }
        return InvocationResponse.CONTINUE;
    }
}
