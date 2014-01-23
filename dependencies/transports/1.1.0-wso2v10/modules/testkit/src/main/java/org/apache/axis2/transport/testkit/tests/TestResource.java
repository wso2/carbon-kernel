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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.axis2.transport.testkit.Adapter;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TestResource {
    private enum Status { UNRESOLVED, RESOLVED, SETUP, RECYCLED };
    
    private interface Invocable {
        void execute(Object object) throws Exception;
    }
    
    private static class MethodInvocation implements Invocable {
        private final Method method;
        private final Object[] args;
        
        public MethodInvocation(Method method, Object[] args) {
            this.method = method;
            this.args = args;
        }
        
        public void execute(Object object) throws Exception {
            try {
                method.invoke(object, args);
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Error) {
                    throw (Error)cause;
                } else if (cause instanceof Exception) {
                    throw (Exception)cause;
                } else {
                    throw ex;
                }
            }
        }
    }
    
    private static class FieldResetter implements Invocable {
        private final Field field;
        
        public FieldResetter(Field field) {
            this.field = field;
        }

        public void execute(Object object) throws Exception {
            field.set(object, null);
        }
    }
    
    private final Object instance;
    private final Object target;
    private final Set<TestResource> directDependencies = new HashSet<TestResource>();
    private final LinkedList<Invocable> initializers = new LinkedList<Invocable>();
    private final List<Invocable> finalizers = new LinkedList<Invocable>();
    private Status status = Status.UNRESOLVED;
    private boolean hasHashCode;
    private int hashCode;
    
    public TestResource(Object instance) {
        this.instance = instance;
        Object target = instance;
        while (target instanceof Adapter) {
            target = ((Adapter)target).getTarget();
        }
        this.target = target;
    }
    
    public void resolve(TestResourceSet resourceSet) {
        if (status != Status.UNRESOLVED) {
            return;
        }
        for (Class<?> clazz = target.getClass(); !clazz.equals(Object.class);
                clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getAnnotation(Setup.class) != null) {
                    Type[] parameterTypes = method.getGenericParameterTypes();
                    Object[] args = new Object[parameterTypes.length];
                    for (int i=0; i<parameterTypes.length; i++) {
                        Type parameterType = parameterTypes[i];
                        if (!(parameterType instanceof Class)) {
                            throw new Error("Generic parameters not supported in " + method);
                        }
                        Class<?> parameterClass = (Class<?>)parameterType;
                        if (parameterClass.isArray()) {
                            Class<?> componentType = parameterClass.getComponentType();
                            TestResource[] resources = resourceSet.findResources(componentType, true);
                            Object[] arg = (Object[])Array.newInstance(componentType, resources.length);
                            for (int j=0; j<resources.length; j++) {
                                TestResource resource = resources[j];
                                directDependencies.add(resource);
                                arg[j] = resource.getInstance();
                            }
                            args[i] = arg;
                        } else {
                            TestResource[] resources = resourceSet.findResources(parameterClass, true);
                            if (resources.length == 0) {
                                throw new Error(target.getClass().getName() + " depends on " +
                                        parameterClass.getName() + ", but none found");
                            } else if (resources.length > 1) {
                                throw new Error(target.getClass().getName() + " depends on " +
                                        parameterClass.getName() + ", but multiple candidates found");
                                
                            }
                            TestResource resource = resources[0];
                            directDependencies.add(resource);
                            args[i] = resource.getInstance();
                        }
                    }
                    method.setAccessible(true);
                    initializers.addFirst(new MethodInvocation(method, args));
                } else if (method.getAnnotation(TearDown.class) != null && method.getParameterTypes().length == 0) {
                    method.setAccessible(true);
                    finalizers.add(new MethodInvocation(method, null));
                }
            }
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(Transient.class) != null) {
                    field.setAccessible(true);
                    finalizers.add(new FieldResetter(field));
                }
            }
        }
        status = Status.RESOLVED;
    }

    public Object getInstance() {
        return instance;
    }
    
    public Object getTarget() {
        return target;
    }

    public boolean hasLifecycle() {
        return !(initializers.isEmpty() && finalizers.isEmpty());
    }

    public void setUp() throws Exception {
        if (status != Status.RESOLVED) {
            throw new IllegalStateException();
        }
        for (Invocable initializer : initializers) {
            initializer.execute(target);
        }
        status = Status.SETUP;
    }
    
    public void recycle(TestResource resource) {
        if (status != Status.RESOLVED || resource.status != Status.SETUP || !equals(resource)) {
            throw new IllegalStateException();
        }
        status = Status.SETUP;
        resource.status = Status.RECYCLED;
    }
    
    public void tearDown() throws Exception {
        if (status != Status.SETUP) {
            throw new IllegalStateException();
        }
        for (Invocable finalizer : finalizers) {
            finalizer.execute(target);
        }
        status = Status.RESOLVED;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestResource) {
            TestResource other = (TestResource)obj;
            return target == other.target && directDependencies.equals(other.directDependencies);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hashCode;
        if (hasHashCode) {
            hashCode = this.hashCode;
        } else {
            hashCode = new HashCodeBuilder().append(target).append(directDependencies).toHashCode();
            if (status != Status.UNRESOLVED) {
                this.hashCode = hashCode;
            }
        }
        return hashCode;
    }

    @Override
    public String toString() {
        Class<?> clazz = target.getClass();
        String simpleName = clazz.getSimpleName();
        if (simpleName.length() > 0) {
            return simpleName;
        } else {
            return "<anonymous " + clazz.getSuperclass().getSimpleName() + ">";
        }
    }
}
