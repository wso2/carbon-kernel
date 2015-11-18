package ${package}.internal;

import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.logging.Logger;

/**
 * DataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private CarbonRuntime carbonRuntime;

    private DataHolder() {

    }

    public static DataHolder getInstance() {
        return instance;
    }

    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }
}
