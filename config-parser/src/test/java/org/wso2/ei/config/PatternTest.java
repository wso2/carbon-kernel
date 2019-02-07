package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PatternTest {

    @Test(dataProvider = "scenarios")
    public void getTestParseConfig(String scenario) throws IOException, ConfigParserException {

        String deploymentConfiguration = FileUtils.getFile("src", "test", "resources", "patterns",scenario, "deployment" +
                ".toml").getAbsolutePath();
        String inferConfiguration =
                FileUtils.getFile("src", "test", "resources", "patterns", "infer.json").getAbsolutePath();
        String mappingConfiguration =
                FileUtils.getFile("src", "test", "resources", "patterns", "key-mappings.toml").getAbsolutePath();
        String templateConfiguration =
                FileUtils.getFile("src", "test", "resources", "patterns", "templates").getAbsolutePath();
        String validatorConfiguration =
                FileUtils.getFile("src", "test", "resources", "patterns", "validator.json").getAbsolutePath();
        String defaultConfiguration =
                FileUtils.getFile("src", "test", "resources", "patterns", "default.json").getAbsolutePath();
        String expectedOutputDirPath =
                FileUtils.getFile("src", "test", "resources", "patterns", scenario, "expected").getAbsolutePath();

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
            String actual = Files.asCharSource(expectedOutput, Charsets.UTF_8).read();
            handleAssertion(actual, entry.getValue());
        }

    }

    @DataProvider(name = "scenarios")
    public Object[] scenarios() {

        return new Object[]{
                "pattern-3/gw",
                "pattern-3/km"
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
