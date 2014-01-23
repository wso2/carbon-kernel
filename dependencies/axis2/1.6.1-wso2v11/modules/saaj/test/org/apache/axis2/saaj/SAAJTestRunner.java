/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.saaj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.xml.soap.SAAJMetaFactory;

import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/**
 * Special JUnit test runner that allows test cases to be validated against
 * Sun's SAAJ implementation.
 * If a test method is annotated with {@link Validated} (in addition to {@link org.junit.Test},
 * then this runner will execute the test twice: once with Sun's SAAJ implementation and
 * once with Axis2's. This is a convenient way to validate test cases, i.e. to make sure that
 * the test cases make correct assertions about SAAJ behavior. Of course this makes the implicit
 * assumption that Sun's implementation is bug free, which is not necessarily the case...
 */
public class SAAJTestRunner extends JUnit4ClassRunner {
    private static class MultiRunListener extends RunListener {
        private final RunNotifier notifier;
        private boolean firstRun = true;
        private int runs;
        private String failureMessage;
        
        public MultiRunListener(RunNotifier notifier, int runs) {
            this.notifier = notifier;
            this.runs = runs;
        }

        @Override
        public void testStarted(Description description) throws Exception {
            runs--;
            if (firstRun) {
                notifier.fireTestStarted(description);
                firstRun = false;
            }
        }
        
        @Override
        public void testFailure(Failure failure) throws Exception {
            if (failureMessage != null) {
                failure = new Failure(failure.getDescription(), new Error(failureMessage,
                        failure.getException()));
            }
            notifier.fireTestFailure(failure);
            runs = 0;
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            notifier.fireTestIgnored(description);
            runs = 0;
        }

        @Override
        public void testFinished(Description description) throws Exception {
            if (runs == 0) {
                notifier.fireTestFinished(description);
            }
        }
        
        public void setFailureMessage(String failureMessage) {
            this.failureMessage = failureMessage;
        }

        public boolean isShouldContinue() {
            return runs > 0;
        }
    }
    
    public SAAJTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void invokeTestMethod(Method method, RunNotifier notifier) {
        boolean validate = method.getAnnotation(Validated.class) != null;
        RunNotifier multiRunNotifier = new RunNotifier();
        MultiRunListener multiRunListener = new MultiRunListener(notifier, validate ? 2 : 1);
        multiRunNotifier.addListener(multiRunListener);
        if (validate) {
            multiRunListener.setFailureMessage(
                    "Invalid test case; execution failed with SAAJ reference implementation");

            System.setProperty("javax.xml.soap.MessageFactory",
            		"com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl");
            System.setProperty("javax.xml.soap.SOAPFactory",
            		"com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl");
            System.setProperty("javax.xml.soap.SOAPConnectionFactory",
            		"com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory");
            System.setProperty("javax.xml.soap.MetaFactory",
            		"com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");

            resetSAAJFactories();

            super.invokeTestMethod(method, multiRunNotifier);
        }
        if (multiRunListener.isShouldContinue()) {
            multiRunListener.setFailureMessage(null);
            System.setProperty("javax.xml.soap.MessageFactory",
                    "org.apache.axis2.saaj.MessageFactoryImpl");
            System.setProperty("javax.xml.soap.SOAPFactory",
                    "org.apache.axis2.saaj.SOAPFactoryImpl");
            System.setProperty("javax.xml.soap.SOAPConnectionFactory",
                    "org.apache.axis2.saaj.SOAPConnectionFactoryImpl");
            System.setProperty("javax.xml.soap.MetaFactory",
                    "org.apache.axis2.saaj.SAAJMetaFactoryImpl");
            resetSAAJFactories();
            super.invokeTestMethod(method, multiRunNotifier);
        }
    }
    
    private void resetSAAJFactories() {
        // SAAJMetaFactory caches the instance; use reflection to reset it between test runs.
        // Note that the other factories are OK.
        try {
            Field field = SAAJMetaFactory.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Throwable ex) {
            throw new Error(ex);
        }
    }
}
