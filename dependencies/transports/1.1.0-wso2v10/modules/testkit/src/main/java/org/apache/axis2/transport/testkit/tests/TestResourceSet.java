/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.tests;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestResourceSet {
    enum Status { UNRESOLVED, RESOLVED, SETUP, RECYCLED };
    
    private static Log log = LogFactory.getLog(TestResourceSet.class);
    
    private final TestResourceSet parent;
    private final List<TestResource> unresolvedResources = new LinkedList<TestResource>();
    final List<TestResource> resolvedResources = new LinkedList<TestResource>();
    Status status = Status.UNRESOLVED;
    
    public TestResourceSet(TestResourceSet parent) {
        this.parent = parent;
    }
    
    public TestResourceSet() {
        this(null);
    }

    public void addResource(Object resource) {
        if (status != Status.UNRESOLVED) {
            throw new IllegalStateException();
        }
        unresolvedResources.add(new TestResource(resource));
    }
    
    public void addResources(Object... resources) {
        for (Object resource : resources) {
            addResource(resource);
        }
    }
    
    public Object[] getResources() {
        if (status == Status.UNRESOLVED) {
            throw new IllegalStateException();
        }
        Object[] result = new Object[resolvedResources.size()];
        int i = 0;
        for (TestResource resource : resolvedResources) {
            result[i++] = resource.getInstance();
        }
        return result;
    }
    
    public void resolve() {
        if (status == Status.UNRESOLVED) {
            while (!unresolvedResources.isEmpty()) {
                resolveResource(unresolvedResources.get(0));
            }
            status = Status.RESOLVED;
        }
    }
    
    private void resolveResource(TestResource resource) {
        unresolvedResources.remove(resource);
        resource.resolve(this);
        resolvedResources.add(resource);
    }
    
    TestResource[] findResources(Class<?> clazz, boolean allowAutoCreate) {
        List<TestResource> result = new LinkedList<TestResource>();
        if (parent != null) {
            result.addAll(Arrays.asList(parent.findResources(clazz, false)));
        }
        for (TestResource resource : resolvedResources) {
            if (clazz.isInstance(resource.getInstance())) {
                result.add(resource);
            }
        }
        List<TestResource> unresolvedMatchingResources = new LinkedList<TestResource>();
        for (TestResource resource : unresolvedResources) {
            if (clazz.isInstance(resource.getInstance())) {
                unresolvedMatchingResources.add(resource);
            }
        }
        for (TestResource resource : unresolvedMatchingResources) {
            resolveResource(resource);
            result.add(resource);
        }
        if (allowAutoCreate && result.isEmpty()) {
            TestResource resource;
            try {
                Field field = clazz.getField("INSTANCE");
                resource = new TestResource(field.get(null));
            } catch (Throwable ex) {
                resource = null;
            }
            if (resource != null) {
                unresolvedResources.add(resource);
                resolveResource(resource);
                result.add(resource);
            }
        }
        return result.toArray(new TestResource[result.size()]);
    }
    
    public void setUp() throws Exception {
        resolve();
        if (status != Status.RESOLVED) {
            throw new IllegalStateException();
        }
        setUp(resolvedResources);
        status = Status.SETUP;
    }
    
    static List<TestResource> filterOnHasLifecycle(List<TestResource> resources) {
        List<TestResource> result = new ArrayList<TestResource>(resources.size());
        for (TestResource resource : resources) {
            if (resource.hasLifecycle()) {
                result.add(resource);
            }
        }
        return result;
    }
    
    static void setUp(List<TestResource> resources) throws Exception {
        resources = filterOnHasLifecycle(resources);
        if (!resources.isEmpty()) {
            log.info("Setting up: " + resources);
            for (TestResource resource : resources) {
                resource.setUp();
            }
        }
    }
    
    public void tearDown() throws Exception {
        if (status != Status.SETUP) {
            throw new IllegalStateException();
        }
        tearDown(resolvedResources);
    }
    
    static void tearDown(List<TestResource> resources) throws Exception {
        resources = filterOnHasLifecycle(resources);
        if (!resources.isEmpty()) {
            log.info("Tearing down: " + resources);
            for (ListIterator<TestResource> it = resources.listIterator(resources.size()); it.hasPrevious(); ) {
                it.previous().tearDown();
            }
        }
    }

    @Override
    public String toString() {
        return resolvedResources.toString();
    }
}
