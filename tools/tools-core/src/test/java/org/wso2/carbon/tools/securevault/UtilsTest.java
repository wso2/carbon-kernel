package org.wso2.carbon.tools.securevault;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.securevault.utils.Utils;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.jar.Manifest;

/**
 * This class defines the unit test cases for Cipher Tool Utils.
 *
 * @since 5.2.0
 */
public class UtilsTest {
    private static final Path targetPath = Paths.get("target");

    @Test
    public void testGetCustomClassLoader() throws ClassNotFoundException {
        URLClassLoader urlClassLoader =
                Utils.getCustomClassLoader(Optional.of(targetPath.toAbsolutePath().toString()));
        Class clazz = urlClassLoader.loadClass("org.wso2.carbon.tools.CarbonTool");
        Assert.assertNotNull(clazz);
    }

    @Test
    public void testGetCustomClassLoaderWithCarbonHome() throws ClassNotFoundException {
        System.setProperty(org.wso2.carbon.kernel.Constants.CARBON_HOME, Paths.get(targetPath.toString(),
                "carbon-home").toString());
        URLClassLoader urlClassLoader =
                Utils.getCustomClassLoader(Optional.of(targetPath.toAbsolutePath().toString()));
        Class clazz = urlClassLoader.loadClass("org.wso2.carbon.tools.CarbonTool");
        Assert.assertNotNull(clazz);
    }

    @Test
    public void testGetCustomClassLoaderWithJarOnJar() throws ClassNotFoundException, IOException, CarbonToolException {
        Path testResourses = Paths.get(targetPath.toString(), "test-resources", "lib");
        Path filePath = Files.list(testResourses)
                .filter(path -> path.toString().toLowerCase().endsWith(".jar"))
                .findFirst()
                .orElseThrow(() -> new IOException("Unable to find a .jar file for the test"));

        BundleGeneratorUtils.convertFromJarToBundle(filePath, filePath.getParent(), new Manifest(), "");

        URLClassLoader urlClassLoader =
                Utils.getCustomClassLoader(Optional.of(targetPath.toAbsolutePath().toString()));
        Class clazz = urlClassLoader.loadClass("org.wso2.carbon.tools.CarbonTool");
        Assert.assertNotNull(clazz);
    }
}
