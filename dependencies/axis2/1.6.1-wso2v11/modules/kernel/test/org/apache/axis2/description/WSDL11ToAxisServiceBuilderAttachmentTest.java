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

package org.apache.axis2.description;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;

/**
 * 
 */
public class WSDL11ToAxisServiceBuilderAttachmentTest extends TestCase {
    
    public void testAttachment() {
        File testResourceFile = new File("test-resources/wsdl/SwaTest.wsdl");
        System.out.println("testResouceFile: " + testResourceFile);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            System.out.println("WSDL file: " + testResourceFile.getName());
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
            fail("Caught exception " + e.toString());
        }
        return;
    }

}
