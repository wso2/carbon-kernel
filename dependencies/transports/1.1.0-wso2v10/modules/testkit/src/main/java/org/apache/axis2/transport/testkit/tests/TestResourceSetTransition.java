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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.axis2.transport.testkit.tests.TestResourceSet.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TestResourceSetTransition {
    private static final Log log = LogFactory.getLog(TestResourceSetTransition.class);
    
    private final TestResourceSet old;
    private final TestResourceSet target;
    private final List<TestResource> oldResourcesToTearDown;
    private final List<TestResource> resourcesToSetUp;
    private final List<TestResource> resourcesToKeep;
    
    public TestResourceSetTransition(TestResourceSet old, TestResourceSet target) {
        this.old = old;
        this.target = target;
        target.resolve();
        if (target.status != TestResourceSet.Status.RESOLVED) {
            throw new IllegalStateException();
        }
        if (old.status != Status.SETUP) {
            throw new IllegalStateException();
        }
        oldResourcesToTearDown = new LinkedList<TestResource>();
        resourcesToSetUp = new LinkedList<TestResource>(target.resolvedResources);
        resourcesToKeep = new LinkedList<TestResource>();
        outer: for (TestResource oldResource : TestResourceSet.filterOnHasLifecycle(old.resolvedResources)) {
            for (Iterator<TestResource> it = resourcesToSetUp.iterator(); it.hasNext(); ) {
                TestResource resource = it.next();
                if (resource.equals(oldResource)) {
                    it.remove();
                    resource.recycle(oldResource);
                    resourcesToKeep.add(oldResource);
                    continue outer;
                }
            }
            oldResourcesToTearDown.add(oldResource);
        }
    }
    
    public void tearDown() throws Exception {
        if (old.status != Status.SETUP) {
            throw new IllegalStateException();
        }
        TestResourceSet.tearDown(oldResourcesToTearDown);
        old.status = Status.RECYCLED;
    }
    
    public void setUp() throws Exception {
        if (target.status != TestResourceSet.Status.RESOLVED) {
            throw new IllegalStateException();
        }
        log.debug("Keeping: " + resourcesToKeep);
        TestResourceSet.setUp(resourcesToSetUp);
        target.status = Status.SETUP;
    }
}
