/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.internal.deployment.Utils;

import java.nio.file.Paths;

public class UtilsTest {

    @Test
    public void testResolveFileURLFailOnNoneFileURLs() {
        String pathURL = "http://felix.apache.org/documentation/subprojects/apache-felix-service-runtime.html";
        try {
            Utils.resolveFileURL(pathURL, null);
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "URLs other than file URLs are not supported.");
        }
    }

    @Test
    public void testResolveFileURLStartsWithFile() {
        String pathURL = "file:/home/Carbon/carbon-kernel-c5/carbon-kernel/core";
        String parentUrl = "/home/Carbon/carbon-kernel-c5/carbon-kernel/core";
        try {
            Utils.resolveFileURL(pathURL, parentUrl);
        } catch (RuntimeException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testResolveFileMalformedUrl() {
        String pathURL = Paths.get("carbon-kernel-c5", "carbon-kernel", "core").toFile().toString();
        String parentUrl = "home";
        try {
            Utils.resolveFileURL(pathURL, parentUrl);
        } catch (RuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Malformed URL : " + pathURL);
        }
    }
}
