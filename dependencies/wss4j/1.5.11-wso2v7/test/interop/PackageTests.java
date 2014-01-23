/**
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

package interop;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test package for WS-Security tests
 */
public class PackageTests extends TestCase {

    public PackageTests(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestInteropKeys.class);
        suite.addTestSuite(TestScenario1.class);
        suite.addTestSuite(TestScenario2.class);
        suite.addTestSuite(TestScenario2a.class);
        suite.addTestSuite(TestScenario3.class);
        suite.addTestSuite(TestScenario4.class);
        suite.addTestSuite(TestScenario5.class);
        suite.addTestSuite(TestScenario6.class); 
        suite.addTestSuite(TestScenario7.class); 
        suite.addTestSuite(TestSTScenario1.class);
        suite.addTestSuite(TestSTScenario3.class);
        suite.addTestSuite(TestSTScenario4.class);
        return suite;
    }

    /**
     * Main method
     * <p/>
     * 
     * @param args command line args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
