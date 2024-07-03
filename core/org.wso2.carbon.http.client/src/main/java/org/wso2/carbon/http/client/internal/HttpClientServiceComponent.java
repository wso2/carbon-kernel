package org.wso2.carbon.http.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.http.client.services.HttpClientService;
import org.wso2.carbon.http.client.services.HttpClientServiceImpl;

@Component(
        name = "http.client.component",
        immediate = true
)
public class HttpClientServiceComponent {

    private static final Log LOGGER = LogFactory.getLog(HttpClientServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        context.getBundleContext().registerService(HttpClientService.class,
                new HttpClientServiceImpl(), null);
        LOGGER.info("HttpClient bundle is activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOGGER.debug("HttpClient service component deactivated");
    }


}
