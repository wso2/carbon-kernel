package org.wso2.carbon.base;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BaseTest {

    /**
     * Basedir for all file I/O.
     */
    public static String basedir;
    public static String carbonHome;

    static {
        basedir = System.getProperty("basedir");
        carbonHome = System.getProperty(Constants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = new File("").getAbsolutePath();
        }
        if (basedir == null) {
            basedir = new File("").getAbsolutePath();
        }
    }

    protected String testDir = "src" + File.separator + "test" + File.separator;
    protected String testResourceDir = testDir + "resources";
    protected String confDir = File.separator + "repository" + File.separator + "conf";
    protected String serverXmlPath = File.separator + "carbon.xml";

    /**
     * @param testName
     */
    public BaseTest(String testName) {
        testDir = new File(basedir, testDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
        confDir = new File(carbonHome, confDir).getAbsolutePath();
        serverXmlPath = new File(confDir, serverXmlPath).getAbsolutePath();
    }

    public File getServerXml() {
        return new File(serverXmlPath);
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    public File getCarbonHome() {
        return new File(carbonHome);
    }

    public File getConfDir() {
        return new File(confDir);
    }

    public InputStream getTestResource(String relativePath) {
        File testResource = getTestResourceFile(relativePath);
        try {
            return new FileInputStream(testResource);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The '" + testResource.getAbsolutePath() +
                    "' file does not exist. Verify that the 'basedir' System property " +
                    "is pointing to the root of the project", e);
        }
    }

}
