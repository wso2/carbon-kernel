package org.wso2.carbon.hazelcastwrite;

import com.hazelcast.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;


/**
 * This class will use hazelcast instance to write distributed values.
 */
public class HazelCastWriter {

    private static final String MAP_NAME = "$hazelcast_test_topic";
    private static final String MAP_KEY_VALUE = "nbxe58ojhfs23tfxfhmhd4367gfdr43ehjjczwe6ijnhd5";
    private static final String CACHE_KEY_VALUE = "nbxe58ojhfs23tfxfhmhd4367gfdr43ehjjczwe6ijnhd5";

    private BundleContext bundleContext;

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

}
