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
package org.wso2.carbon.kernel.deployment;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Artifact test class which extends the BaseTest class.
 */
public class ArtifactTest extends BaseTest {
    private static final String DEPLOYER_REPO = "carbon-repo" + File.separator + "text-files";
    private File file = new File(getTestResourceFile(DEPLOYER_REPO).getAbsolutePath()
            + File.separator + "sample1.txt");
    private ArtifactType artifactType = new ArtifactType<String>("txt");

    private HashMap<String, Object> properties = new HashMap<>();

    private Artifact artifact;

    /**
     * @param testName
     */
    public ArtifactTest(String testName) {
        super(testName);
    }

    @BeforeSuite
    public void initialize() {
        artifact = new Artifact(file);
        properties.put("key1", "value1");
        artifact.setProperties(properties);

        artifact.setType(artifactType);

        artifact.setKey("sample1.txt");

        artifact.setVersion("1.0.0");

        artifact.setLastModifiedTime(123456789);

    }

    @Test
    public void testArtifact() {
        Assert.assertEquals(artifact.getName(), "sample1.txt");
        Assert.assertEquals(artifact.getVersion(), "1.0.0");
        Assert.assertEquals(artifact.getFile().getAbsolutePath(), file.getAbsolutePath());
        Assert.assertEquals(artifact.getKey(), "sample1.txt");
        Assert.assertEquals(artifact.getType().get(), artifactType.get());
        Assert.assertEquals(artifact.getLastModifiedTime(), 123456789);
        Assert.assertEquals(artifact.getPath(), file.getPath());
        Assert.assertEquals(artifact.getProperties().get("key1"), properties.get("key1"));
    }
}
