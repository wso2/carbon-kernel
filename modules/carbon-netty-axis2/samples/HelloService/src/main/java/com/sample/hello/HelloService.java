package com.sample.hello;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.axis2.clustering.ClusteringFault;

import java.util.Iterator;
import java.util.Properties;

/**
 * This is CustomService.
 *
 * @since 1.0.0
 */
public class HelloService {
    private static final String HELLO_SERVICE_NAME = "HelloService.Name";

    public String greet(String name) {
        ServiceContext serviceContext =
                MessageContext.getCurrentMessageContext().getServiceContext();
        serviceContext.setProperty(HELLO_SERVICE_NAME, name);
        try {
            Replicator.replicate(serviceContext, new String[]{HELLO_SERVICE_NAME});
        } catch (ClusteringFault clusteringFault) {
            clusteringFault.printStackTrace();
        }

        if (name != null) {
            return "Hello World, " + name + " !!!";
        } else {
            return "Hello World !!!";
        }
    }
}
