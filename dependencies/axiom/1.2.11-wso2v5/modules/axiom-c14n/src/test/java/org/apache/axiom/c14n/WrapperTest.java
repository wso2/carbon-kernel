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

package org.apache.axiom.c14n;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.axiom.c14n.omwrapper.*;

/**
 * @author Saliya Ekanayake (esaliya@gmail.com)
 */
public class WrapperTest extends TestCase {
    public WrapperTest(String name){
        super(name);
    }

    public static Test suite() {
        TestSuite suite =  new TestSuite("All org.apache.axiom.c14n.omwrapper JUnit Tests");
        suite.addTest(AttrImplTest.suite());
        suite.addTest(CommentImplTest.suite());
        suite.addTest(DocumentImplTest.suite());
        suite.addTest(ElementImplTest.suite());
        suite.addTest(NamedNodeMapImplTest.suite());
        suite.addTest(NodeListImplTest.suite());
        suite.addTest(TextImplTest.suite());
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
