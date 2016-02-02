package org.wso2.carbon.axis2.runtime.bridge.http;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.axis2.runtime.internal.DataHolder;

/**
 * This is HttpCarbonAxis2Bridge.
 *
 * @since 1.0.0
 */
public class HttpCarbonAxis2TransportListener implements TransportListener {
    private static final Logger logger = LoggerFactory.getLogger(HttpCarbonAxis2TransportListener.class);

    @Override
    public void init(ConfigurationContext configurationContext, TransportInDescription transportInDescription)
            throws AxisFault {

    }

    @Override
    public void start() throws AxisFault {
        logger.info("HttpCarbonAxis2TransportListener is started");
    }

    @Override
    public void stop() throws AxisFault {
        logger.info("HttpCarbonAxis2TransportListener is stopped");
    }

    @Override
    public EndpointReference[] getEPRsForService(String s, String s1) throws AxisFault {
        return new EndpointReference[0];
    }

    @Override
    public SessionContext getSessionContext(MessageContext messageContext) {
        return null;
    }

    @Override
    public void destroy() {

    }
}
