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

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.axis2.transport.testkit.name.Key;
import org.apache.axis2.transport.testkit.name.NameUtils;
import org.apache.axis2.transport.testkit.util.LogManager;

@Key("test")
public abstract class ManagedTestCase extends TestCase {
    private final TestResourceSet resourceSet = new TestResourceSet();
    
    private Map<String,String> nameComponents;
    
    private String id;
    private boolean managed;
    private Class<?> testClass;

    public ManagedTestCase(Object... resources) {
        resourceSet.addResources(resources);
        addResource(LogManager.INSTANCE);
    }

    protected void addResource(Object resource) {
        resourceSet.addResource(resource);
    }
    
    public Map<String,String> getNameComponents() {
        if (nameComponents == null) {
            nameComponents = new LinkedHashMap<String,String>();
            NameUtils.getNameComponents(nameComponents, this);
            resourceSet.resolve();
            for (Object resource : resourceSet.getResources()) {
                NameUtils.getNameComponents(nameComponents, resource);
            }
        }
        return nameComponents;
    }
    
    // TODO: TransportTestCase should be in the same package as TransportTestSuite and this
    //       method should have package access
    public void init(String id, boolean managed, Class<?> testClass) {
        this.id = id;
        this.managed = managed;
        this.testClass = testClass;
    }

    public String getId() {
        return id != null ? id : getName();
    }

    public Class<?> getTestClass() {
        return testClass != null ? testClass : getClass();
    }

    @Override
    public String getName() {
        String testName = super.getName();
        if (testName == null) {
            StringBuilder buffer = new StringBuilder();
            if (id != null) {
                buffer.append(id);
                buffer.append(':');
            }
            boolean first = true;
            for (Map.Entry<String,String> entry : getNameComponents().entrySet()) {
                if (first) {
                    first = false;
                } else {
                    buffer.append(',');
                }
                buffer.append(entry.getKey());
                buffer.append('=');
                buffer.append(entry.getValue());
            }
            testName = buffer.toString();
            setName(testName);
        }
        return testName;
    }

    public TestResourceSet getResourceSet() {
        return resourceSet;
    }

    @Override
    protected void setUp() throws Exception {
        if (!managed) {
            LogManager.INSTANCE.setTestCase(this);
            resourceSet.setUp();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (!managed) {
            resourceSet.tearDown();
            LogManager.INSTANCE.setTestCase(null);
        }
    }

    @Override
    public String toString() {
        return getName() + "(" + testClass.getName() + ")";
    }
}