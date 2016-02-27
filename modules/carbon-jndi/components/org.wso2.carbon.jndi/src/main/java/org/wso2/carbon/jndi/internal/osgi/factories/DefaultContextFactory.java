/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.jndi.internal.osgi.factories;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jndi.JNDIContextManager;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.NamingManager;
import java.util.Hashtable;
import java.util.Optional;

public class DefaultContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {

        //Find the BundleContext of the caller of this method.
        BundleContext callersBundleContext = getCallersBundleContext(environment);
        if (callersBundleContext == null) {
            //TODO Proper error handling.
            throw new NoInitialContextException("");
        }

        //Retrieve the JNDIContextManager service from the BundleContext and invoke getInitialContext method.
        Optional<ServiceReference<JNDIContextManager>> contextManagerSR = Optional.ofNullable(
                callersBundleContext.getServiceReference(JNDIContextManager.class));

        return contextManagerSR
                .map(callersBundleContext::getService)
                .map(jndiContextManager -> {
                    try {
                        return jndiContextManager.newInitialContext(environment);
                    } catch (NamingException e) {
                        //TODO Proper error handling  and logging
                        return null;
//                        new RuntimeException(e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new NoInitialContextException(""));


        //IF no BundleContext is found then, throw NoInitialContext Exception.

//        return null;
    }

    private BundleContext getCallersBundleContext(Hashtable<?, ?> environment) {
        BundleContext callersBC = null;

        // 1. Look in the JNDI environment properties for a property called osgi.service.jndi.bundleContext.
        //      If a value for this property exists then use it as the Bundle Context.
        //      If the Bundle Context has been found stop.

        Object obj = environment.get("osgi.service.jndi.bundleContext");
        if (obj instanceof BundleContext) {
            return (BundleContext) obj;
        }

        // 2. Obtain the Thread Context Class Loader; if it, or an ancestor class loader, implements the
        //      BundleReference interface, call its getBundle method to get the client's Bundle; then call
        //      getBundleContext on the Bundle object to get the client's Bundle Context.
        //      If the Bundle Context has been found stop.

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        callersBC = getCallersBundleContext(Optional.of(tccl));

        // 3. Walk the call stack until the invoker is found. The invoker can be the caller of the InitialContext class
        //    constructor or the NamingManager or DirectoryManager getObjectInstance methods.
        //      • Get the class loader of the caller and see if it, or an ancestor, implements the
        //        BundleReference interface.
        //      • If a Class Loader implementing the BundleReference interface is found call the getBundle method to
        //        get the clients Bundle; then call the getBundleContext method on the Bundle to get the
        //        clients Bundle Context.
        //      • If the Bundle Context has been found stop, else continue with the next stack frame.

        //Creating a local class which extends SecurityManager to get the current execution stack as an array of classes
        class DummySecurityManager extends SecurityManager {
            public Class<?>[] getClassContext() {
                return super.getClassContext();
            }
        }

        Class[] currentClassStack = new DummySecurityManager().getClassContext();

        int index;
        boolean found = false;
        for (index = 0; index < currentClassStack.length; index++) {
            Class clazz = currentClassStack[index];

            if (found) {
                callersBC = getCallersBundleContext(Optional.ofNullable(clazz.getClassLoader()));
                if (callersBC != null) {
                    break;
                }
            }

            // Check whether NamingManager or InitialContext is a superclass or super interface of the current class.
            if (!found &&
                    (NamingManager.class.isAssignableFrom(clazz) || InitialContext.class.isAssignableFrom(clazz))) {
                found = true;
            }

        }

        return callersBC;
    }

    /**
     * Returns a BundleContext obtained from the given ClassLoader or from an ancestor ClassLoader.
     * <p>
     * First check whether this ClassLoader implements the BundleReference interface, if so try to get BundleContext.
     * If not get from the parent ClassLoader. Repeat these steps until a not-null BundleContext is found or the parent
     * ClassLoader becomes null.
     *
     * @param classLoaderOptional an {@code Optional} describing a ClassLoader
     * @return BundleContext extracted from the give ClassLoader or from its ancestors ClassLoaders.
     */
    private BundleContext getCallersBundleContext(Optional<ClassLoader> classLoaderOptional) {

        if (!classLoaderOptional.isPresent()) {
            return null;
        }

        return classLoaderOptional
                .filter(classLoader -> classLoader instanceof BundleReference)
                .map(classLoader -> (BundleReference) classLoader)
                .map(bundleReference -> bundleReference.getBundle().getBundleContext())
                .orElseGet(() -> getCallersBundleContext(Optional.ofNullable(classLoaderOptional.get().getParent())));
    }
}
