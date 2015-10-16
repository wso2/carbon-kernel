package org.wso2.carbon.sample.transport.http;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sample.transport.mgt.Transport;

/**
 * TODO
 */
@Component(
        name = "org.wso2.carbon.sample.transport.http.HTTPTransport",
        immediate = true,
        service = Transport.class
)
public class HTTPTransport implements Transport {
    private static final Logger logger = LoggerFactory.getLogger(HTTPTransport.class);

    @Override
    public void start() {
        logger.info("******** service " + this.getClass().getName());
    }

    @Override
    public void stop() {

    }

    @Activate
    public void activate() {

    }

    @Deactivate
    public void deactivate() {

    }
}
