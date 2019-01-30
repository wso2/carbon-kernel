package org.wso2.ei.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class ConfigParserTest {

    @Test
    public void getTestParseConfig() throws IOException, ValidationException {

        String deploymentConfiguration = FileUtils.getFile("src", "test", "resources", "scenario-1", "deployment" +
                ".toml").getAbsolutePath();
        String inferConfiguration =
                FileUtils.getFile("src", "test", "resources", "scenario-1", "infer.json").getAbsolutePath();
        String mappingConfiguration =
                FileUtils.getFile("src", "test", "resources", "scenario-1", "key-mappings.toml").getAbsolutePath();
        String templateConfiguration =
                FileUtils.getFile("src", "test", "resources", "scenario-1", "user-mgt.xml").getAbsolutePath();
        String result =
                FileUtils.getFile("src", "test", "resources", "scenario-1", "result").getAbsolutePath();

        ConfigParser configParser = new ConfigParser.ConfigParserBuilder()
                .withDeploymentConfigurationPath(deploymentConfiguration)
                .withInferConfigurationFilePath(inferConfiguration)
                .withMappingFilePath(mappingConfiguration)
                .withTemplateFilePath(templateConfiguration)
                .build();
        String output = configParser.parse();
        String actual = Files.toString(Paths.get(result).toFile(), Charsets.UTF_8);
        Assert.assertEquals(output, actual);
    }

}
