/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.tools.spi;

import org.testng.annotations.Test;
import org.wso2.carbon.tools.TestConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

public class SPICreatorTest {
    private static final Path converterTestResources =
            Paths.get(TestConstants.TARGET_FOLDER, TestConstants.TEST_RESOURCES,
                      TestConstants.CONVERTER_TEST_RESOURCES);
    private static final Path sampleJARFile = Paths.get(converterTestResources.toString(), TestConstants.ARTIFACT_FIVE);

    @Test
    public void testAddingSPI() throws IOException {
        SPICreator spiCreator = new SPICreator();
        spiCreator.execute("org.wso2.spi.TestSPI", "org.wso2.carbon.impl.TestSPIImpl", sampleJARFile.toString(),
                           converterTestResources.toString());

        String jarFileName = sampleJARFile.getFileName().toString();
        Path tmpDir = converterTestResources.resolve(jarFileName.substring(0, jarFileName.lastIndexOf(".")));
        Path finalJarPath = tmpDir.resolve(jarFileName);
        assertTrue(Files.exists(finalJarPath));

        JarFile jarFile = new JarFile(finalJarPath.toFile());
        assertNotNull(jarFile.getJarEntry("META-INF/services/org.wso2.spi.TestSPI"));
    }
}
