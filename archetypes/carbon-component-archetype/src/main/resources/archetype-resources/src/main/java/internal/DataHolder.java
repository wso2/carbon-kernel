package ${package}.internal;

import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.logging.Logger;

/**
 * DataHolder to hold org.wso2.carbon.kernel.CarbonRuntime instance referenced through
 * org.wso2.carbon.helloworld.internal.ServiceComponent.
 *
 * @since ${version}
 */
public class DataHolder {
    Logger logger = Logger.getLogger(DataHolder.class.getName());

    private static DataHolder instance = new DataHolder();
    private CarbonRuntime carbonRuntime;

    private DataHolder() {

    }

    /**
     * This returns the DataHolder instance.
     *
     * @return The DataHolder instance of this singleton class
     */
    public static DataHolder getInstance() {
        return instance;
    }

    /**
     * Returns the CarbonRuntime service which gets set through a service component.
     *
     * @return CarbonRuntime Service
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is for setting the CarbonRuntime service. This method is used by
     * ServiceComponent.
     *
     * @param carbonRuntime The reference being passed through ServiceComponent
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }
}
