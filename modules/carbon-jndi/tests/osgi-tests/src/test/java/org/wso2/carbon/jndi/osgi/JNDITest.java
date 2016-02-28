
/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.jndi.osgi;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.CarbonRuntime;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JNDITest {
    private static final Logger logger = LoggerFactory.getLogger(JNDITest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonRuntime carbonRuntime;

//    @ProbeBuilder
//    public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
//        probe.setHeader(
//                Constants.DYNAMICIMPORT_PACKAGE,
//                "*");
//        return probe;
//    }

//    @Inject
//    private JNDIContextManager jndiContextManager;

    @Test
    public void testJNDITest1() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:comp");
        initialContext.bind("java:comp/name", "sameera");

        InitialContext context = new InitialContext();
        String name = (String) context.lookup("java:comp/name");

        assertEquals(name, "sameera", "Value not found in JNDI");
    }

    @Test
    public void testJNDITest2() throws Exception {

        Hashtable<String, String> environment = new Hashtable<>(1);
        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                "org.wso2.carbon.jndi.java.javaURLContextFactory");

        InitialContext initialContext = new InitialContext(environment);
        initialContext.createSubcontext("java:comp/env");
        initialContext.bind("java:comp/env/name", "sameera");

        InitialContext context = new InitialContext();
        String name = (String) context.lookup("java:comp/env/name");

        assertEquals(name, "sameera", "Value not found in JNDI");
    }

//    @Test
//    public void testJNDITest2() throws Exception {
//        String name = null;
//
//        ServiceReference<JNDIContextManager> jndiContextManagerSRef = bundleContext.getServiceReference(JNDIContextManager.class);
//        if (jndiContextManagerSRef != null) {
//
//            JNDIContextManager jndiContextManager = bundleContext.getService(jndiContextManagerSRef);
//
//            if (jndiContextManager != null) {
//                Context initialContext = jndiContextManager.newInitialContext();
//
//                initialContext.createSubcontext("java:comp/env");
//                initialContext.bind("java:comp/env/name", "jayasoma");
//
//                InitialContext context = new InitialContext();
//                name = (String) context.lookup("java:comp/env/name");
//            }
//        }
//
//        assertEquals(name, "jayasoma", "Value not found in JNDI");
//    }

//
//    @Test
//    public void testJNDITest3() throws Exception {
//        Map<String, String> environment = new HashMap<String, String>(1);
//        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
//                "org.wso2.carbon.jndi.java.javaURLContextFactory");
//
//        Context initialContext = jndiContextManager.newInitialContext(environment);
//
//        initialContext.createSubcontext("java:comp/env/jdbc");
//        initialContext.bind("java:comp/env/jdbc", "wso2");
//
//        InitialContext context = new InitialContext();
//        String name = (String) context.lookup("java:comp/env/jdbc");
//
//        assertEquals(name, "wso2", "Value not found in JNDI");
//    }

}