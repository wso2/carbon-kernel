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
package org.wso2.carbon.tools;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.tools.utils.BundleGeneratorUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Java class which tests the delete method of BundleGeneratorUtils.java class.
 *
 * @since 5.0.0
 */
public class DeletePathTest {
    private static final Path DIRECTORY = Paths.get(TestConstants.TEST_DIRECTORY_TWO);
    private static final Path DIRECTORY_WITH_CHILDREN = Paths.get(TestConstants.TEST_DIRECTORY_ONE);

    static {
        TestUtils.createDirectoryWithChildren(DIRECTORY_WITH_CHILDREN);
        TestUtils.createDirectory(DIRECTORY);
    }

    @Test public void deleteChildlessDirectoryTest() {
        boolean deleted;
        if (Files.exists(DIRECTORY)) {
            try {
                deleted = BundleGeneratorUtils.delete(DIRECTORY);
            } catch (IOException e) {
                deleted = false;
            }
        } else {
            deleted = false;
        }
        Assert.assertTrue(deleted);
    }

    @Test public void deleteDirectoryWithChildrenTest() {
        boolean deleted;
        if (Files.exists(DIRECTORY_WITH_CHILDREN)) {
            try {
                deleted = BundleGeneratorUtils.delete(DIRECTORY_WITH_CHILDREN);
            } catch (IOException e) {
                deleted = false;
            }
        } else {
            deleted = false;
        }
        Assert.assertTrue(deleted);
    }
}
