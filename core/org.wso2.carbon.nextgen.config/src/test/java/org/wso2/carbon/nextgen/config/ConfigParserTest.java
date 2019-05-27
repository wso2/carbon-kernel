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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.nextgen.config.model.Context;
import org.wso2.carbon.nextgen.config.util.FileUtils;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class ConfigParserTest {

    Path tempDir;
    Context context = new Context();

    @BeforeMethod
    public void createTemporaryDirectory() throws IOException {

        tempDir = Files.createTempDirectory("output");
        context.getTemplateData().clear();
        context.getSecrets().clear();
        context.getResolvedSystemProperties().clear();
        context.getResolvedEnvironmentVariables().clear();
    }

    @AfterMethod
    public void removeTemporaryDirectory() throws IOException {

        org.apache.commons.io.FileUtils.deleteDirectory(tempDir.toFile());
    }

    @Test(dataProvider = "scenarios")
    public void getTestParseConfig(String scenario) throws IOException, ConfigParserException {

        String deploymentConfiguration = org.apache.commons.io.FileUtils.getFile("src", "test", "resources",
                scenario).getAbsolutePath();
        String inferConfiguration =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String mappingConfiguration =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String templateConfiguration =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String validatorConfiguration =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String defaultConfiguration =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String unitConfiguration =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String expectedOutputDirPath =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario, "expected")
                        .getAbsolutePath();
        String newConfigDirectoryPath = Paths.get(tempDir.toString(), "repository", "resources", "conf").toString();

        ConfigParser configParser = new ConfigParser.ConfigParserBuilder()
                .withDeploymentConfigurationPath(deploymentConfiguration)
                .withInferConfigurationFilePath(inferConfiguration)
                .withMappingFilePath(mappingConfiguration)
                .withValidatorFilePath(validatorConfiguration)
                .withTemplateFilePath(templateConfiguration)
                .withDefaultValueFilePath(defaultConfiguration)
                .withUnitResolverFilePath(unitConfiguration)
                .withMetaDataFilePath(newConfigDirectoryPath)
                .build();
        Map<String, String> outputFileContentMap = configParser.parse(context);
        File resultDir = new File(expectedOutputDirPath);
        for (Map.Entry<String, String> entry : outputFileContentMap.entrySet()) {
            File expectedOutput = new File(resultDir, entry.getKey());
            if (!expectedOutput.exists() || !expectedOutput.isFile()) {
                Assert.fail("Expected result file doesn't exist for " + entry.getKey());
            }
            String expected = FileUtils.readFile(expectedOutput);
            if (expectedOutput.getName().contains(".xml")) {
                handleXmlAssersions(entry.getValue(), expected);
            } else if (expectedOutput.getName().contains(".properties")) {
                Properties expectedProperties = new Properties();
                Properties actualProperties = new Properties();
                expectedProperties.load(new FileInputStream(expectedOutput));
                expectedProperties.load(new StringReader(entry.getValue()));
                handlePropertiesFileAssersion(actualProperties, expectedProperties);
            } else {
                handleAssertion(entry.getValue(), expected);
            }
        }

    }

    @Test
    public void testMetadataParsing() throws ConfigParserException, IOException {

        String deploymentConfiguration = org.apache.commons.io.FileUtils.getFile("src", "test", "resources",
                "scenario-8").getAbsolutePath();
        org.apache.commons.io.FileUtils.copyDirectory(new File(deploymentConfiguration), tempDir.toFile());
        String newConfigDirectoryPath = Paths.get(tempDir.toString(), "repository", "resources", "conf").toString();
        String configDirectoryPath = Paths.get(tempDir.toString(), "repository", "conf").toString();

        // Check With no metadata Directory
        ConfigParser configParser = new ConfigParser.ConfigParserBuilder()
                .withBasePath(tempDir.toString())
                .withDeploymentConfigurationPath(tempDir.toString())
                .withInferConfigurationFilePath(newConfigDirectoryPath)
                .withMappingFilePath(newConfigDirectoryPath)
                .withValidatorFilePath(newConfigDirectoryPath)
                .withTemplateFilePath(newConfigDirectoryPath)
                .withDefaultValueFilePath(newConfigDirectoryPath)
                .withUnitResolverFilePath(newConfigDirectoryPath)
                .withMetaDataFilePath(newConfigDirectoryPath)
                .build();
        configParser.parse(tempDir.toString());

        // Check with metadata while no changes
        configParser.parse(tempDir.toString());

        // Change the Resulted File
        org.apache.commons.io.FileUtils.writeStringToFile(Paths.get(configDirectoryPath, "user-mgt.xml").toFile(),
                "\n<abcde>ddd</abcde>\n", true);
        configParser.parse(tempDir.toString());
        // Check if added content is there
        Assert.assertFalse(org.apache.commons.io.FileUtils.readFileToString(Paths.get(configDirectoryPath,
                "user-mgt.xml").toFile()).contains("abcde>ddd</abcde>"));
        // Change the Template File
        org.apache.commons.io.FileUtils.writeStringToFile(Paths.get(newConfigDirectoryPath, "templates",
                "repository", "conf", "user-mgt.xml.j2").toFile(), "\n<abcde>ddd</abcde>\n", true);
        configParser.parse(tempDir.toString());
        // Check change applied to result file
        Assert.assertTrue(org.apache.commons.io.FileUtils.readFileToString(Paths.get(configDirectoryPath,
                "user-mgt.xml").toFile()).contains("abcde>ddd</abcde>"));
        String deploymentContent = org.apache.commons.io.FileUtils.readFileToString(Paths.get(tempDir.toString(),
                "deployment.toml").toFile());
        // Change in deployment.toml
        deploymentContent = deploymentContent.replaceAll("username = \"admin1\"", "username = \"admin2\"");
        org.apache.commons.io.FileUtils.writeStringToFile(Paths.get(tempDir.toString(), "deployment.toml")
                .toFile(), deploymentContent);
        configParser.parse(tempDir.toString());
        Assert.assertTrue(org.apache.commons.io.FileUtils.readFileToString(Paths.get(configDirectoryPath,
                "user-mgt.xml").toFile()).contains("<UserName>admin2</UserName>"));
        // Create un tracked File and Do changes
        new File(Paths.get(configDirectoryPath, "temp.xml").toString()).createNewFile();
        configParser.parse(tempDir.toString());
        // Create new Template and check if its get as change
        Paths.get(newConfigDirectoryPath, "templates", "repository", "conf", "abc.xml.j2").toFile().createNewFile();
        configParser.parse(tempDir.toString());
        Assert.assertTrue(Paths.get(configDirectoryPath, "abc.xml").toFile().exists());

// Check System property
        deploymentContent = org.apache.commons.io.FileUtils.readFileToString(Paths.get(tempDir.toString(),
                "deployment.toml").toFile());
        // Change in deployment.toml
        deploymentContent = deploymentContent.replaceAll("username = \"admin2\"", "username = \"\\" +
                "$sys{admin_username}\"");
        org.apache.commons.io.FileUtils.writeStringToFile(Paths.get(tempDir.toString(), "deployment.toml")
                .toFile(), deploymentContent);
        System.setProperty("admin_username", "admin3");
        configParser.parse(tempDir.toString());
        Assert.assertTrue(org.apache.commons.io.FileUtils.readFileToString(Paths.get(configDirectoryPath,
                "user-mgt.xml").toFile()).contains("<UserName>admin3</UserName>"));
        System.setProperty("admin_username", "admin4");
        configParser.parse(tempDir.toString());
        Assert.assertTrue(org.apache.commons.io.FileUtils.readFileToString(Paths.get(configDirectoryPath,
                "user-mgt.xml").toFile()).contains("<UserName>admin4</UserName>"));

        // Remove deployment.toml and check
        Paths.get(tempDir.toString(), "deployment.toml").toFile().delete();
        configParser.parse(tempDir.toString());
    }

    @DataProvider(name = "scenarios")
    public Object[] scenarios() {

        return new Object[]{
                "scenario-1",
                "scenario-2",
                "scenario-3",
                "scenario-4",
                "scenario-5",
                "scenario-6",
                "scenario-7"
        };
    }

    private void handleAssertion(String actual, String expected) {

        StringBuilder actualFileContent = new StringBuilder();
        StringBuilder expectedFileContent = new StringBuilder();
        for (String s : actual.split("\n")) {
            actualFileContent.append(s.trim());
        }
        for (String s : expected.split("\n")) {
            expectedFileContent.append(s.trim());
        }
        if (actualFileContent.toString().equals(expectedFileContent.toString())) {
            Assert.assertTrue(true);
        } else {
            Assert.assertEquals(actual, expected);
        }
    }

    private void handleXmlAssersions(String actual, String expected) {

        Diff difference = DiffBuilder.compare(actual).withTest(expected).ignoreComments().ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes)).checkForSimilar()
                .build();
        if (!difference.hasDifferences()) {
            Assert.assertTrue(true);
        } else {
            Assert.assertEquals(actual, expected);
        }
    }

    private void handlePropertiesFileAssersion(Properties actualProperties, Properties expectedProperties) {

        Assert.assertEquals(actualProperties, expectedProperties);
    }

}
