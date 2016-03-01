
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

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.jndi.osgi.utils.OSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JNDITest {
    private static final Logger logger = LoggerFactory.getLogger(JNDITest.class);

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private JNDIContextManager jndiContextManager;

    //TODO Clean up these tests

    @Test
    public void testJNDITest1() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:comp");
        initialContext.bind("java:comp/name", "sameera");

        InitialContext context = new InitialContext();
        String name = (String) context.lookup("java:comp/name");
        assertEquals(name, "sameera", "Value not found in JNDI");


        NamingEnumeration namingEnumeration = context.list("java:comp");
        namingEnumeration.hasMore();
        namingEnumeration.next();


        namingEnumeration = context.listBindings("java:comp");
        namingEnumeration.hasMore();
        namingEnumeration.next();

        context.rebind("java:comp/name", "jayasoma");
        name = (String) context.lookup("java:comp/name");
        assertEquals(name, "jayasoma", "Value not found in JNDI");

        context.rename("java:comp", "java:comp1");
        name = (String) context.lookup("java:comp1/name");
        assertEquals(name, "jayasoma", "Value not found in JNDI");

        context.rename("java:comp1", "java:comp");
    }

    @Test
    public void testJNDITest2() throws Exception {

        Hashtable<String, String> environment = new Hashtable<>(1);
        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                "org.wso2.carbon.jndi.java.javaURLContextFactory");


        InitialContext initialContext = new InitialContext(environment);

        initialContext.createSubcontext("java:comp/env1");
        initialContext.bind("java:comp/env1/name", "sameera");

        InitialContext context = new InitialContext(environment);
        String name = (String) context.lookup("java:comp/env1/name");

        assertEquals(name, "sameera", "Value not found in JNDI");
    }

    @Test
    public void testJNDITest3() throws Exception {
        String name = null;

        Context initialContext = jndiContextManager.newInitialContext();

        initialContext.createSubcontext("java:comp/env2");
        initialContext.bind("java:comp/env2/name", "jayasoma");

        Context context = jndiContextManager.newInitialContext();

        name = (String) context.lookup("java:comp/env2/name");

        assertEquals(name, "jayasoma", "Value not found in JNDI");
    }


    @Test
    public void testJNDITest4() throws Exception {
        Map<String, String> environment = new HashMap<String, String>(1);
        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                "org.wso2.carbon.jndi.java.javaURLContextFactory");

        Context initialContext = jndiContextManager.newInitialContext(environment);

        initialContext.createSubcontext("java:comp/env3");
        initialContext.bind("java:comp/env3/jdbc", "wso2");


        Context context = jndiContextManager.newInitialContext(environment);
        String name = (String) context.lookup("java:comp/env3/jdbc");

        assertEquals(name, "wso2", "Value not found in JNDI");
    }

}