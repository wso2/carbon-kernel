/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.osgi.deployment.tracker;

import org.apache.axis2.osgi.deployment.Registry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BundleTracker listen to bundle events and class the registered Registry
 * objects to deal with them. One be able to add Registry objects to BundleTracker and remove
 * them as necessary. When open() is called BundleTracker will open the tracking and when close()
 * is called close the tracking.
 */
public class BundleTracker {

    private List<Registry> registryList = new ArrayList<Registry>();

    private List<Bundle> bundleList = new ArrayList<Bundle>();

    private boolean open;

    private BundleContext context;

    private BundleListener bundleListener;

    private static Log log = LogFactory.getLog(BundleTracker.class);

    private final Lock lock = new ReentrantLock();

    public BundleTracker(final BundleContext context) {
        this.context = context;
        bundleListener = new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                lock.lock();
                try {
                    if (!open) {
                        return;
                    }
                    Bundle bundle = event.getBundle();
                    switch (event.getType()) {
                        case BundleEvent.STARTED:
                            if (!bundleList.contains(event.getBundle())) {
                                bundleList.add(event.getBundle());
                                // logic
                                for (Registry registry : registryList) {
                                    registry.register(bundle);
                                }
                            }
                            break;
                        case BundleEvent.STOPPED:
                            if (context.getBundle() != bundle) {
                                if (bundleList.contains(event.getBundle())) {
                                    bundleList.remove(event.getBundle());
                                    //logic
                                    for (Registry registry : registryList) {
                                        registry.unRegister(bundle, false);
                                    }

                                }
                            }
                            break;
                        case BundleEvent.UNINSTALLED:
                            if (context.getBundle() != bundle) {
                                if (bundleList.contains(event.getBundle())) {
                                    bundleList.remove(event.getBundle());
                                    //logic
                                    for (Registry registry : registryList) {
                                        registry.remove(bundle);
                                    }
                                }
                            }
                            break;
                    }
                } finally {
                    lock.unlock();
                }

            }
        };


    }

    public void addRegistry(Registry registry) {
        registryList.add(registry);
    }

    public void open() {
        lock.lock();
        try {
            if (!open) {
                open = true;
                log.info("Bundle tracker is opened");
                Bundle[] bundles = context.getBundles();
                for (Bundle bundle : bundles) {
                    if (bundle.getState() == Bundle.ACTIVE) {
                        bundleList.add(bundle);
                        for (Registry registry : registryList) {
                            registry.register(bundle);
                        }
                    }
                }
                context.addBundleListener(bundleListener);
            }
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            if (open) {
                open = false;
                context.removeBundleListener(bundleListener);
                Bundle[] bundles = bundleList.toArray(new Bundle[bundleList.size()]);
                for (Bundle bundle : bundles) {
                    if (bundleList.remove(bundle)) {
                        for (Registry registry : registryList) {
                            registry.remove(bundle);
                        }
                    }
                }
                log.info("Bundle tracker is closed");
            }
        } finally {
            lock.unlock();
        }
    }


}
