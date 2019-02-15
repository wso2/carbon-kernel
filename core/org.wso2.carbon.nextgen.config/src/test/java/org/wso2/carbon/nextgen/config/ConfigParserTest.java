package org.wso2.carbon.nextgen.config;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.nextgen.config.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ConfigParserTest {

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
        String expectedOutputDirPath =
                org.apache.commons.io.FileUtils.getFile("src", "test", "resources", scenario, "expected")
                        .getAbsolutePath();

        ConfigParser configParser = new ConfigParser.ConfigParserBuilder()
                .withDeploymentConfigurationPath(deploymentConfiguration)
                .withInferConfigurationFilePath(inferConfiguration)
                .withMappingFilePath(mappingConfiguration)
                .withValidatorFilePath(validatorConfiguration)
                .withTemplateFilePath(templateConfiguration)
                .withDefaultValueFilePath(defaultConfiguration)
                .build();
        Map<String, String> outputFileContentMap = configParser.parse();
        File resultDir = new File(expectedOutputDirPath);
        for (Map.Entry<String, String> entry : outputFileContentMap.entrySet()) {
            File expectedOutput = new File(resultDir, entry.getKey());
            if (!expectedOutput.exists() || !expectedOutput.isFile()) {
                Assert.fail("Expected result file doesn't exist for " + entry.getKey());
            }
            String expected = FileUtils.readFile(expectedOutput);
            handleAssertion(entry.getValue(), expected);
        }

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

}
