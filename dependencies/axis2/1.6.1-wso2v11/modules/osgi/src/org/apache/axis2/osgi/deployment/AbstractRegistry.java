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
package org.apache.axis2.osgi.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @see org.apache.axis2.osgi.deployment.Registry
 */
public abstract class AbstractRegistry<V> implements Registry {

    protected Map<Bundle, List<V>> resolvedBundles = new ConcurrentHashMap<Bundle, List<V>>();

    protected List<Bundle> unreslovedBundles = new ArrayList<Bundle>();

    protected final Lock lock = new ReentrantLock();

    protected BundleContext context;

    protected ConfigurationContext configCtx;

    public AbstractRegistry(BundleContext context, ConfigurationContext configCtx) {
        this.context = context;
        this.configCtx = configCtx;
    }

    public void resolve() {
        //to avoid concurrent modification
        Bundle[] bundles = unreslovedBundles.toArray(new Bundle[unreslovedBundles.size()]);
        for (Bundle bundle : bundles) {
            register(bundle);
        }
    }
}
