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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A Java class with utility methods for test classes.
 *
 * @since 5.0.0
 */
public class TestUtils {
    private TestUtils() {
    }

    protected static boolean createDirectory(Path directory) {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected static boolean createFile(Path file) {
        try {
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected static boolean createDirectoryWithChildren(Path parentDirectory) {
        List<Path> children = getChildPaths(parentDirectory);
        return ((TestUtils.createDirectory(parentDirectory)) && (TestUtils.createFile(children.get(0))) && TestUtils
                .createFile(children.get(1)) && (TestUtils.createDirectory(children.get(2))));
    }

    protected static List<Path> getChildPaths(Path parentDirectory) {
        Path sampleFileOne = Paths.get(parentDirectory.toString(), TestConstants.CHILD_TEST_FILE_ONE);
        Path sampleFileTwo = Paths.get(parentDirectory.toString(), TestConstants.CHILD_TEST_FILE_TWO);
        Path sampleDirectoryOne = Paths.get(parentDirectory.toString(), TestConstants.CHILD_TEST_DIRECTORY_ONE);

        List<Path> paths = new ArrayList<>();
        paths.add(sampleFileOne);
        paths.add(sampleDirectoryOne);
        paths.add(sampleFileTwo);

        return paths;
    }

    protected static boolean isMatching(List<Object> expected, List<Object> actual) {
        if ((expected != null) && (actual != null)) {
            if (expected.size() == actual.size()) {
                for (Object actualObject : actual) {
                    boolean exists = false;
                    for (Object expectedObject : expected) {
                        if (expectedObject.equals(actualObject)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
