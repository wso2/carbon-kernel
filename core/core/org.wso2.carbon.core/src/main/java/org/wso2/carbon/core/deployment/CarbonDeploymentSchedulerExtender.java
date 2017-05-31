package org.wso2.carbon.core.deployment;

import org.apache.axis2.engine.AxisConfiguration;

/**
 * This can be used as an Extender in order to schedule some task while the deployment task is
 * running. (Refer OGSI Extender pattern for more information.)
 * Also this task is executed at server shutdown as well. If you do not wish to execute the task
 * at shutdown time, you can do following check  in the implementing method.
 *  <blockquote>
 * <pre>
 *     ServerStatus.STATUS_RUNNING.equals(ServerStatus.getCurrentStatus()
 * </pre>
 * </blockquote>
 */
public interface CarbonDeploymentSchedulerExtender {

    /**
     * invoke the extender methods. Put your logic inside this method.
     *
     * @param axisConfig axisConfiguration
     */
    public void invoke(AxisConfiguration axisConfig);
}
