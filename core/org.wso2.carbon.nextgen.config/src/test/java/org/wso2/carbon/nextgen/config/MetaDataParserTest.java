/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public class MetaDataParserTest {

    @Test
    public void testReadLastModifiedValues() throws ConfigParserException {

        String deploymentConfiguration =
                FileUtils.getFile("src", "test", "resources", "MetadataTest").getAbsolutePath();
        String basePath =
                FileUtils.getFile("src", "test", "resources").getAbsolutePath();
        Map<String, String> modifiedMap = MetaDataParser.readLastModifiedValues(basePath, deploymentConfiguration);
        Assert.assertEquals(modifiedMap.size(), 4);
        Assert.assertEquals(modifiedMap.get("MetadataTest/file1"), "d41d8cd98f00b204e9800998ecf8427e");
    }

    @Test
    public void testGetChangedFiles() throws ConfigParserException {

        String deploymentConfiguration =
                FileUtils.getFile("src", "test", "resources", "MetadataTest").getAbsolutePath();

        String metadataConfiguration =
                FileUtils.getFile("src", "test", "resources", "metadata.properties").getAbsolutePath();
        String basePath =
                FileUtils.getFile("src", "test", "resources").getAbsolutePath();
        ChangedFileSet changedFiles = MetaDataParser.getChangedFiles(basePath, Arrays.asList(deploymentConfiguration),
                metadataConfiguration);
        Assert.assertEquals(changedFiles.getChangedFiles().size(), 0);

    }

    @Test
    public void testGetChangedFilesWithNewFile() throws ConfigParserException {

        String deploymentConfiguration =
                FileUtils.getFile("src", "test", "resources", "NewFileTest").getAbsolutePath();

        String metadataConfiguration = FileUtils.getFile("src", "test", "resources",
                "metadata-new.properties").getAbsolutePath();
        String basePath = FileUtils.getFile("src", "test", "resources").getAbsolutePath();
        ChangedFileSet changedFiles = MetaDataParser.getChangedFiles(basePath, Arrays.asList(deploymentConfiguration),
                metadataConfiguration);
        Assert.assertEquals(changedFiles.getChangedFiles().size(), 1);
        Assert.assertTrue(changedFiles.getChangedFiles().contains(Paths.get("NewFileTest", "a", "file1").toString()));

    }

}
