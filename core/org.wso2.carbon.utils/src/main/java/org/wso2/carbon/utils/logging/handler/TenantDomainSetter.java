package org.wso2.carbon.utils.logging.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.base.MultitenantConstants;

/**
 * This handler is used to set tenant domain as a thread local variable
 * in case where request is coming via NHTTP transport.
 */
public class TenantDomainSetter extends AbstractHandler {

    private static ThreadLocal<String> tenantDomain = new ThreadLocal<String>();

    private static ThreadLocal<String> serviceName = new ThreadLocal<String>();

    public static String getServiceName() {
        return serviceName.get();
    }

    public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {

        //cleaning up any existing thread local variable
        tenantDomain.set(null);
        try {
            EndpointReference epr = messageContext.getTo();
            if (epr != null) {
                String to = epr.getAddress();
                if (to != null && to.indexOf("/t/") != -1) {
                    String str1 = to.substring(to.indexOf("/t/") + 3);
                    String domain = str1.substring(0, str1.indexOf("/"));
                    tenantDomain.set(domain);
                    messageContext.setProperty(MultitenantConstants.TENANT_DOMAIN, domain);
                } else {
                    tenantDomain.set(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    messageContext.setProperty(MultitenantConstants.TENANT_DOMAIN,
                            MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                }
                String service = "";
                if (to.contains("/services/")) {
                    String temp = to.substring(to.indexOf("/services/") + 9);
                    if (temp.indexOf('/') != -1) {
                        temp = temp.substring(0, temp.length());
                        service = temp;
                    }
                    if (service.contains("/t/")) {
                        String temp2[] = service.split("/");
                        if (temp2.length > 3) {
                            service = temp2[3];
                        }
                    }
                    if (service.contains(".")) {
                        service = service.substring(0, service.indexOf('.'));
                    } else if (service.contains("?")) {
                        service = service.substring(0, service.indexOf('?'));
                    }
                }
                service = service.substring(service.indexOf('/') + 1, service.length());
                serviceName.set(service);
            }
        } catch (Throwable ignore) {
            //don't care if anything failed
        }

        return InvocationResponse.CONTINUE;
    }

    /**
     * @return String tenant domain of incoming request
     */
    public static String getTenantDomain() {
        return tenantDomain.get();
    }
}
