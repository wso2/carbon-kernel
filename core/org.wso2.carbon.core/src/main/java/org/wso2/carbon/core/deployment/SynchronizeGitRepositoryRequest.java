package org.wso2.carbon.core.deployment;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.core.CarbonAxisConfigurator;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.multitenancy.TenantAxisConfigurator;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.UUID;

/**
 * GroupManagementCommand for sending a git based deployment repository synchronization request
 */
public class SynchronizeGitRepositoryRequest extends GroupManagementCommand {

    private transient static final Log log = LogFactory.getLog(SynchronizeGitRepositoryRequest.class);
    private int tenantId;
    private String  tenantDomain;
    private UUID messageId;

    public SynchronizeGitRepositoryRequest() {
    }

    public SynchronizeGitRepositoryRequest(int tenantId, String tenantDomain, UUID messageId) {
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.messageId = messageId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void execute(ConfigurationContext configContext) throws ClusteringFault{
        log.info("Received [" + this + "] ");
        // Run only if the tenant is loaded
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID ||
                TenantAxisUtils.getTenantConfigurationContexts(configContext).get(tenantDomain) != null) {
            log.info("Going to synchronize artifacts... ");
            updateDeploymentRepository(configContext);
            log.info("Artifact synchronization complete... ");
            doDeployment(configContext);
        }
    }

    private void doDeployment(ConfigurationContext configContext) {
        AxisConfigurator axisConfigurator = configContext.getAxisConfiguration().getConfigurator();
        if(axisConfigurator instanceof CarbonAxisConfigurator) {
            ((CarbonAxisConfigurator) axisConfigurator).runDeployment();
        } else if (axisConfigurator instanceof TenantAxisConfigurator){
            ((TenantAxisConfigurator) axisConfigurator).runDeployment();
        }
    }

    private void updateDeploymentRepository(ConfigurationContext configContext) {
        BundleContext bundleContext = CarbonCoreDataHolder.getInstance().getBundleContext();
        ServiceReference reference =
                bundleContext.getServiceReference(DeploymentSynchronizer.class.getName());
        if (reference != null) {
            ServiceTracker serviceTracker =
                    new ServiceTracker(bundleContext, DeploymentSynchronizer.class.getName(), null);
            try {
                serviceTracker.open();
                for (Object obj : serviceTracker.getServices()) {
                    // if the update is for worker node with ghost ON, then we will update the
                    // whole repo for now. See CARBON-13899
                    if (GhostDeployerUtils.isGhostOn() && CarbonUtils.isWorkerNode() &&
                            tenantId > 0) {
                        String repoPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
                        ((DeploymentSynchronizer) obj).update(repoPath, repoPath, 3);
                    } else {
                        ((DeploymentSynchronizer) obj).update(tenantId);
                    }
                }
            } catch (Exception e) {
                log.error("Repository update failed for tenant " + tenantId, e);
                setRepoUpdateFailed(configContext);
            } finally {
                serviceTracker.close();
            }
        }
    }

    private void setRepoUpdateFailed(ConfigurationContext configContext) {
        AxisConfigurator axisConfigurator = configContext.getAxisConfiguration().getConfigurator();
        if(axisConfigurator instanceof CarbonAxisConfigurator) {
            ((CarbonAxisConfigurator) axisConfigurator).setRepoUpdateFailed();
        } else if (axisConfigurator instanceof TenantAxisConfigurator){
            ((TenantAxisConfigurator) axisConfigurator).setRepoUpdateFailed();
        }
    }

    public GroupManagementCommand getResponse(){
        return null;
    }


    @Override
    public String toString() {
        return "SynchronizeGitRepositoryRequest{" +
                "tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", messageId=" + messageId +
                '}';
    }
}
