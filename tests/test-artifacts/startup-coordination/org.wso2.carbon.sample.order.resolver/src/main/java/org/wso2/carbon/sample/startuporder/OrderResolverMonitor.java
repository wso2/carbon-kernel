package org.wso2.carbon.sample.startuporder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to check the OrderResolving logic using a counter to store the invoked order against the listener name.
 *
 * @since 5.0.0
 */
public class OrderResolverMonitor {
    private Map<String, Integer> invocationOrderMap = new ConcurrentHashMap<>();
    private AtomicInteger invocationCounter = new AtomicInteger();
    private static OrderResolverMonitor orderResolverMonitor = new OrderResolverMonitor();

    private OrderResolverMonitor() {
    }

    public static OrderResolverMonitor getInstance() {
        return orderResolverMonitor;
    }

    public void setListenerInvocationOrder(String listenerName) {
        invocationOrderMap.put(listenerName, invocationCounter.incrementAndGet());
    }

    public int getListenerInvocationOrder(String listenerName) {
        return invocationOrderMap.get(listenerName);
    }

    public void clearInvocationCounter() {
        invocationCounter.set(0);
        invocationOrderMap.clear();
    }
}
