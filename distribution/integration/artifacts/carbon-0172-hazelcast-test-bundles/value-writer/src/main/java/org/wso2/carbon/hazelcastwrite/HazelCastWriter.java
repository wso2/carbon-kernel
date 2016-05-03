package org.wso2.carbon.hazelcastwrite;

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.caching.impl.DistributedMapProvider;
import org.wso2.carbon.core.clustering.api.CarbonCluster;
import org.wso2.carbon.core.clustering.api.ClusterMember;
import org.wso2.carbon.core.clustering.api.ClusterMembershipListener;

import java.util.concurrent.TimeUnit;


/**
 * This class will use hazelcast instance to write distributed values.
 */
public class HazelCastWriter {

    private static final String MAP_NAME = "$hfde579okyfcfrtuikjgfdt";
    private static final String MAP_KEY_VALUE = "nbxe58ojhfs23tfxfhmhd4367gfdr43ehjjczwe6ijnhd5";
    private static final String DST_MAP_NAME = "$9754sfyuolmbcry767ijg6iojfxs";
    private static final String DST_MAP_KEY_VALUE = "khfdr47855dt646fdyikj7532sfy86ugd5742svhk865";

    private BundleContext bundleContext;
    private ServiceReference dstMapProviderRef;

    private static final Log log = LogFactory.getLog(HazelCastWriter.class);

    private HazelCastWriter() {
    }

    public static HazelCastWriter newInstance(BundleContext bundleContext) {
        HazelCastWriter hazelCastWriter = new HazelCastWriter();
        hazelCastWriter.bundleContext = bundleContext;
        return hazelCastWriter;
    }

    public void start() {
        log.info("Hazelcast writer bundle started");
        bundleContext.addServiceListener(new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent serviceEvent) {
                ServiceReference sr = serviceEvent.getServiceReference();
                if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
                    Object service = bundleContext.getService(sr);
                    if (service != null) {
                        if (service instanceof HazelcastInstance) {
                            HazelcastInstance hazelcastInstance = (HazelcastInstance) service;
                            log.info("Hazelcast instance was resolved");
                            useTopic(hazelcastInstance);
                        } else if (service instanceof CarbonCluster) {
                            CarbonCluster carbonCluster = (CarbonCluster) service;
                            log.info("CarbonCluster instance was resolved");
                            useCarbonCluster(carbonCluster);
                        } else if (service instanceof DistributedMapProvider) {
                            log.info("DistributedMapProvider instance was resolved");
                            dstMapProviderRef = sr;
                        }
                    }
                }
            }
        });
    }

    public void stop() {

    }

    private void useTopic(HazelcastInstance hazelcastInstance) {
        if (hazelcastInstance != null) {
            IMap<Object, Object> map = hazelcastInstance.getMap(MAP_NAME);
            map.put(MAP_KEY_VALUE, MAP_KEY_VALUE);
            log.info("Map key value added to hazelcast instance " + MAP_NAME + ":" + MAP_KEY_VALUE + ":" + MAP_KEY_VALUE);
        } else {
            log.info("Hazelcast instance is null");
        }
    }

    private void useCarbonCluster(CarbonCluster carbonCluster) {
        carbonCluster.addMembershipListener(new ClusterMembershipListener() {
            @Override
            public void memberAdded(ClusterMember member) {
                log.info("Member added: " + member.getId());
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < TimeUnit.MINUTES.toMillis(1)) {
                    if (isDstMapWriteOk()) {
                        return;
                    }
                }
            }

            @Override
            public void memberRemoved(ClusterMember member) {
            }
        });
    }

    private boolean isDstMapWriteOk() {
        if (dstMapProviderRef == null) {
            log.info("DistributedMapProvider instance reference is null");
            return false;
        }
        DistributedMapProvider distributedMapProvider =
                bundleContext.<DistributedMapProvider>getService(dstMapProviderRef);
        if (distributedMapProvider == null) {
            log.info("DistributedMapProvider instance is null");
            return false;
        }
        log.info("DST Map key value added to hazelcast instance " +
                DST_MAP_NAME + ":" + DST_MAP_KEY_VALUE + ":" + DST_MAP_KEY_VALUE);
        distributedMapProvider.getMap(DST_MAP_NAME, null).put(DST_MAP_KEY_VALUE, DST_MAP_KEY_VALUE);
        return true;
    }

}
