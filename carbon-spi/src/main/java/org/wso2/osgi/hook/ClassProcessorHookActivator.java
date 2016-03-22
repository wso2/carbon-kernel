package org.wso2.osgi.hook;

import org.eclipse.osgi.internal.hookregistry.ActivatorHookFactory;
import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

import org.wso2.osgi.spi.processor.ConsumerProcessor;

public class ClassProcessorHookActivator implements HookConfigurator, ActivatorHookFactory {

    @Override
    public BundleActivator createActivator() {
        return new BundleActivator() {

            private ServiceRegistration weavingHookService = null;
            public void start(BundleContext context) throws Exception {
                System.out.println("Hook Activator Starting");
                ConsumerProcessor consumerProcessor = new ConsumerProcessor();
                weavingHookService = context.registerService(WeavingHook.class, consumerProcessor, null);
                System.out.println("Hook Activator Started");
            }

            public void stop(BundleContext context) throws Exception {
                weavingHookService.unregister();
                System.out.println("hook Bundle Stopped");
            }
        };
    }

    @Override
    public void addHooks(HookRegistry hookRegistry) {
        System.out.println("ActivatorHook registered");
        hookRegistry.addActivatorHookFactory(this);
    }
}
