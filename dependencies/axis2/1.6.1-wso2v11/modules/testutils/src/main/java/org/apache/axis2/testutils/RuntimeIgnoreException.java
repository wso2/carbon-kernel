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

/**
 * Indicates that a test case should be skipped. Throwing this exception allows to skip a test case
 * dynamically at runtime. This is useful if a test case relies on some assumption that may not be
 * true in all environments. Note that this only works if the appropriate test runner is configured.
 * <p>
 * It should also be noted that there are alternative techniques that may be more appropriate in
 * some use cases:
 * <ul>
 * <li>Simply return from the test method if the expectation about the runtime environment is not
 * met. This is simpler, but has the disadvantage that the test case is counted as successful
 * instead of skipped.
 * <li>Using a Maven profile to exclude tests. This is more appropriate e.g. to exclude test cases
 * that apply only to some Java versions.
 * </ul>
 * 
 * @see AllTestsWithRuntimeIgnore
 * @see JUnit38ClassRunnerWithRuntimeIgnore
 */
public class RuntimeIgnoreException extends Error {
    private static final long serialVersionUID = -2378820905593825587L;

    public RuntimeIgnoreException(String message) {
        super(message);
    }
}
