package org.wso2.carbon.sample.startuporder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A dummy class to check the OrderResolving logic
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

    public void listenerInvoked(String listenerName) {
        int currentCount = invocationCounter.incrementAndGet();
        invocationOrderMap.put(listenerName, currentCount);
    }

    public Map<String, Integer> getInvocationOrderMap() {
        return invocationOrderMap;
    }

    public void clearInvocationCounter() {
        invocationCounter.set(0);
    }
}
