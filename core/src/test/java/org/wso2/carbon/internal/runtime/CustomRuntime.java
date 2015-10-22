package org.wso2.carbon.internal.runtime;

import org.wso2.carbon.runtime.RuntimeState;
import org.wso2.carbon.runtime.exception.RuntimeServiceException;
import org.wso2.carbon.runtime.spi.Runtime;

public class CustomRuntime implements Runtime {
    @Override
    public void init() throws RuntimeServiceException {

    }

    @Override
    public void start() throws RuntimeServiceException {

    }

    @Override
    public void stop() throws RuntimeServiceException {

    }

    @Override
    public void beginMaintenance() throws RuntimeServiceException {

    }

    @Override
    public void endMaintenance() throws RuntimeServiceException {

    }

    @Override
    public Enum<RuntimeState> getState() {
        return null;
    }

    @Override
    public void setState(RuntimeState runtimeState) {

    }
}
