package org.wso2.carbon.kernel.internal.transports;

import org.wso2.carbon.kernel.transports.CarbonTransport;

/**
 * This class acts as a dummy command transport for the test case
 * org.wso2.carbon.kernel.internal.transports.TransportMgtCommandProviderTest.
 *
 * @since 5.0.0
 */
public class DummyTransport extends CarbonTransport {
    private Boolean started = false;
    private Boolean stopped = false;
    private Boolean beganMaintenance = false;
    private Boolean endedMaintenance = false;

    public DummyTransport(String id) {
        super(id);
    }

    @Override
    protected void start() {
        started = true;
    }

    @Override
    protected void stop() {
        stopped = true;
    }

    @Override
    protected void beginMaintenance() {
        beganMaintenance = true;
    }

    @Override
    protected void endMaintenance() {
        endedMaintenance = true;
    }

    public Boolean getStarted() {
        return started;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public Boolean getBeganMaintenance() {
        return beganMaintenance;
    }

    public Boolean getEndedMaintenance() {
        return endedMaintenance;
    }
}
