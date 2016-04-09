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

import org.testng.annotations.Test;
import org.wso2.carbon.tools.exceptions.CarbonToolException;
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A Java class which tests the listZipFileContent method of BundleGeneratorUtils.java class.
 *
 * @since 5.0.0
 */
public class ListZipFileContentTest {

    @Test(expectedExceptions = { IOException.class, CarbonToolException.class })
    public void listZipFileContentOfTextFileTest() throws IOException, CarbonToolException {
        Path textFilePath = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "sample", ".txt");
        textFilePath.toFile().deleteOnExit();
        BundleGeneratorUtils.listZipFileContent(textFilePath);
    }

    @Test(expectedExceptions = { IOException.class, CarbonToolException.class })
    public void listZipFileContentOfNonExistingFileTest() throws IOException, CarbonToolException {
        BundleGeneratorUtils.listZipFileContent(Paths.get(System.getProperty("java.io.tmpdir"), "temp.zip"));
    }

    @Test(expectedExceptions = { IOException.class, CarbonToolException.class })
    public void listZipFileContentOfDirectoryTest() throws IOException, CarbonToolException {
        BundleGeneratorUtils.listZipFileContent(Paths.get(System.getProperty("java.io.tmpdir")));
    }

}
