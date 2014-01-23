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

import java.io.InputStream;
import java.net.URL;

import javax.activation.DataSource;
import javax.activation.URLDataSource;

public class TestUtils {
    public static final String MTOM_TEST_MESSAGE_FILE = "message.bin";
    public static final String MTOM_TEST_MESSAGE_CONTENT_TYPE =
            "multipart/related; " +
            "boundary=\"MIMEBoundaryurn:uuid:F02ECC18873CFB73E211412748909307\"; " +
            "type=\"application/xop+xml\"; " +
            "start=\"<0.urn:uuid:F02ECC18873CFB73E211412748909308@apache.org>\"; " +
            "start-info=\"text/xml\"; " +
            "charset=UTF-8;" +
            "action=\"mtomSample\"";
    
    private TestUtils() {}
    
    public static InputStream getTestFile(String name) {
        return TestUtils.class.getClassLoader().getResourceAsStream(name);
    }
    
    public static DataSource getTestFileAsDataSource(String name) {
        return new URLDataSource(TestUtils.class.getClassLoader().getResource(name));
    }
    
    public static URL getTestFileURL(String name) {
        return TestUtils.class.getClassLoader().getResource(name);
    }
    
    public static String getTestFileURI(String name) {
        return TestUtils.class.getClassLoader().getResource(name).toExternalForm();
    }
}
