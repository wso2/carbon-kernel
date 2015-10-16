package org.wso2.carbon.sample.runtime.mss;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * TODO
 */
@Component(
        name = "org.wso2.carbon.sample.runtime.mss.MSSRuntime",
        immediate = true,
        service = org.wso2.carbon.sample.runtime.mgt.Runtime.class
)
public class MSSRuntime implements org.wso2.carbon.sample.runtime.mgt.Runtime {
    @Override
    public void start() {
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
