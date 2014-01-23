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
package org.apache.axis2.testutils;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

class RuntimeIgnoreRunNotifier extends RunNotifier {
    private final RunNotifier target;

    public RuntimeIgnoreRunNotifier(RunNotifier target) {
        this.target = target;
    }

    @Override
    public void addFirstListener(RunListener listener) {
        target.addFirstListener(listener);
    }

    @Override
    public void addListener(RunListener listener) {
        target.addListener(listener);
    }

    @Override
    public void fireTestFailure(Failure failure) {
        if (failure.getException() instanceof RuntimeIgnoreException) {
            target.fireTestIgnored(failure.getDescription());
        } else {
            target.fireTestFailure(failure);
        }
    }

    @Override
    public void fireTestFinished(Description description) {
        target.fireTestFinished(description);
    }

    @Override
    public void fireTestIgnored(Description description) {
        target.fireTestIgnored(description);
    }

    @Override
    public void fireTestRunFinished(Result result) {
        target.fireTestRunFinished(result);
    }

    @Override
    public void fireTestRunStarted(Description description) {
        target.fireTestRunStarted(description);
    }

    @Override
    public void fireTestStarted(Description description) throws StoppedByUserException {
        target.fireTestStarted(description);
    }

    @Override
    public void pleaseStop() {
        target.pleaseStop();
    }

    @Override
    public void removeListener(RunListener listener) {
        target.removeListener(listener);
    }

    @Override
    public void testAborted(Description description, Throwable cause) {
        target.testAborted(description, cause);
    }
}