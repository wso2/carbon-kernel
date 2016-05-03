package org.wso2.carbon.hazelcastread;

import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.caching.impl.DistributedMapProvider;
import org.wso2.carbon.core.clustering.api.CarbonCluster;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class will use hazelcast instance to read distributed values.
 */
public class HazelCastReader {

    private static final String MAP_NAME = "$hfde579okyfcfrtuikjgfdt";
    private static final String MAP_KEY_VALUE = "nbxe58ojhfs23tfxfhmhd4367gfdr43ehjjczwe6ijnhd5";
    private static final String DST_MAP_NAME = "$9754sfyuolmbcry767ijg6iojfxs";
    private static final String DST_MAP_KEY_VALUE = "khfdr47855dt646fdyikj7532sfy86ugd5742svhk865";
    private static final String OUT_FILE_PATH_SYS_PROP = "hazelcastTesterPath";
    private static final String SUCCESS_OUT_MSG = "SUCCESS";

    private BundleContext bundleContext;
    private String myFilePath = System.getProperty(OUT_FILE_PATH_SYS_PROP);
    private ServiceReference hazelcastRef;
    private ServiceReference dstMapProviderRef;

    private static final Log log = LogFactory.getLog(HazelCastReader.class);

    private HazelCastReader() {
    }

    public static HazelCastReader newInstance(BundleContext bundleContext) {
        HazelCastReader hazelCastReader = new HazelCastReader();
        hazelCastReader.bundleContext = bundleContext;
        return hazelCastReader;
    }

    public void start() {
        log.info("Hazelcast reader bundle started");
        bundleContext.addServiceListener(new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent serviceEvent) {
                ServiceReference sr = serviceEvent.getServiceReference();
                if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
                    Object service = bundleContext.getService(sr);
                    if (service != null) {
                        if (service instanceof HazelcastInstance) {
                            hazelcastRef = sr;
                            log.info("HazelcastInstance reference received");
                        } else if (service instanceof DistributedMapProvider) {
                            dstMapProviderRef = sr;
                            log.info("HazelcastInstance reference received");
                        }
                    }
                }
            }
        });
        Executors.newFixedThreadPool(1).submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return null;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("Entering read loop");
                long startTime = System.currentTimeMillis();
                while (hazelcastRef == null ||
                        System.currentTimeMillis() - startTime < TimeUnit.MINUTES.toMillis(1)) {
                    try {
                        if (hazelcastRef == null) {
                            startTime = System.currentTimeMillis();
                        } else {
                            if (isMapValueOk() && isDstMapValueOk()) {
                                log.info("Cluster communication is ok");
                                Files.write(Paths.get(myFilePath), SUCCESS_OUT_MSG.getBytes());
                                log.info("Success file was written @" + myFilePath);
                                break;
                            } else {
                                log.info("Cluster communication is still not ok");
                            }
                        }
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (Exception e) {
                        log.info("Exception in read loop ", e);
                    }
                }
                log.info("Exiting read loop");
            }
        }).start();
    }

    public void stop() {

    }

    private boolean isMapValueOk() {
        if (hazelcastRef == null) {
            log.info("Hazelcast instance reference is null");
            return false;
        }
        HazelcastInstance hazelcastInstance = bundleContext.<HazelcastInstance>getService(hazelcastRef);
        if (hazelcastInstance == null) {
            log.info("Hazelcast instance is null");
            return false;
        }
        return MAP_KEY_VALUE.equals(hazelcastInstance.getMap(MAP_NAME).get(MAP_KEY_VALUE));
    }

    private boolean isDstMapValueOk() {
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
        return DST_MAP_KEY_VALUE.equals(distributedMapProvider.getMap(DST_MAP_NAME, null).get(DST_MAP_KEY_VALUE));
    }

}
