package org.wso2.carbon.core.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.core.deployment.CarbonDeploymentSchedulerExtender;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;

public class DeploymentUtils {
    private static final Log log = LogFactory.getLog(DeploymentUtils.class);

    public static void invokeCarbonDeploymentSchedulerExtenders(AxisConfiguration axisConfig) {
        if(log.isDebugEnabled()){
            log.debug("Start invoking CarbonDeploymentSchedulerExtenders..");
        }
        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        ServiceReference reference = bundleContext.getServiceReference(CarbonDeploymentSchedulerExtender.class.getName());
        if(reference != null){
            ServiceTracker serviceTracker =
                    new ServiceTracker(bundleContext, CarbonDeploymentSchedulerExtender.class.getName(), null);
            try{
                serviceTracker.open();
                Object[] services = serviceTracker.getServices();
                if (services != null) {
                    for (Object service : services) {
                        ((CarbonDeploymentSchedulerExtender) service).invoke(axisConfig);
                    }
                }
            } catch (Exception e) {
                log.error("Error occurred in invoking CarbonDeploymentSchedulerExtenders.. "+
                          e.getMessage());
                e.printStackTrace();
            } finally {
                serviceTracker.close();
            }
        }
    }

}
