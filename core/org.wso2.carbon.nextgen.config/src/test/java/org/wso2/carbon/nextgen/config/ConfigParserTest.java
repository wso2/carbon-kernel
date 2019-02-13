package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.nextgen.config.util.FileReaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ConfigParserTest {

    @Test(dataProvider = "scenarios")
    public void getTestParseConfig(String scenario) throws IOException, ConfigParserException {

        String deploymentConfiguration = FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String inferConfiguration =
                FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String mappingConfiguration =
                FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String templateConfiguration =
                FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String validatorConfiguration =
                FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String defaultConfiguration =
                FileUtils.getFile("src", "test", "resources", scenario).getAbsolutePath();
        String expectedOutputDirPath =
                FileUtils.getFile("src", "test", "resources", scenario, "expected").getAbsolutePath();

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
            String actual = FileReaderUtils.readFile(expectedOutput);
            handleAssertion(actual, entry.getValue());
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
