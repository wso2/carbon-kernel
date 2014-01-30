package org.wso2.carbon.runtime.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.runtime.RuntimeState;
import org.wso2.carbon.runtime.spi.Runtime;


public class CustomRuntime implements Runtime {
    private static Log log = LogFactory.getLog(CustomRuntime.class);

    private RuntimeState state = RuntimeState.PENDING;


    @Override
    public void init() {
        log.info("Initializing Runtime");
        state = RuntimeState.INACTIVE;
    }

    @Override
    public void start() {
        log.info("Starting Runtime");
        state = RuntimeState.ACTIVE;
    }

    @Override
    public void stop() {
        log.info("Stopping Runtime");
        state = RuntimeState.INACTIVE;
    }

    @Override
    public void startMaintenance() {
        log.info("Stopping Runtime");
        state = RuntimeState.MAINTENANCE;
    }

    @Override
    public void stopMaintenance() {
        log.info("Stopping Runtime");
        state = RuntimeState.INACTIVE;
    }

    @Override
    public Enum<RuntimeState> getState() {
        return state;
    }

    @Override
    public void setState(RuntimeState runtimeState) {
        this.state = runtimeState;
    }
}
