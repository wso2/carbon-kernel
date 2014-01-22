package org.wso2.carbon.launcher.test;


import java.io.*;
import java.util.ArrayList;

public class BaseTest {

    protected String testDir = "src" + File.separator + "test" + File.separator;
    protected String testResourceDir = testDir + "resources";
//    private final static String LOGS = "logs" + File.separator + "test.logs";

    /**
     * Basedir for all file I/O. Important when running tests from the reactor.
     * Note that anyone can use this statically.
     */
    public static String basedir;

    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    /**
     * @param testName
     */
    public BaseTest(String testName) {
        testDir = new File(basedir, testDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
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

    public ArrayList<String> getLogsFromTestResource(FileInputStream testResource) {
        ArrayList<String> logRecords = new ArrayList<String>();
//        InputStream testResource = null;
        BufferedReader bufferedReader = null;
        try {
//            testResource = new FileInputStream(new File("/home/manoj/Desktop/carbon-kernel/modules/launcher/src/test/resources/logs/test.logs"));
            bufferedReader = new BufferedReader(new InputStreamReader(testResource));
            String strLine;
            while ((strLine = bufferedReader.readLine()) != null)   {
                logRecords.add(strLine);
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                bufferedReader.close();
                testResource.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return logRecords;
    }
}
