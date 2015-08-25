package org.wso2.carbon.base;


import org.osgi.framework.InvalidSyntaxException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class LoggingConfigurationTest extends BaseTest {
    LoggingConfiguration loggingConfiguration;
    protected String testDir = "src" + File.separator + "test" + File.separator;
    private final String PROPERTY_FILE = "sample-log4j.properties";

    /**
     * @param testName
     */
    public LoggingConfigurationTest(String testName) {
        super(testName);
    }


    @BeforeTest
    public void doBeforeTest() {
        loggingConfiguration = LoggingConfiguration.getInstance();
    }

    @Test
    public void testRegisterConfigurations() throws IOException, InvalidSyntaxException {

        String rootLogger = "log4j.rootLogger";
        String loggerOrgWso2 = "log4j.logger.org.wso2";
        String loggerTraceMessages = "log4j.logger.trace.messages";
        String carbonConsole = "log4j.appender.CARBON_CONSOLE";
        String carbonConsoleLayout = "log4j.appender.CARBON_CONSOLE.layout";
        String carbonConsoleLayoutConversionPattern = "log4j.appender.CARBON_CONSOLE.layout.ConversionPattern";
        String carbonConsoleThreshold = "log4j.appender.CARBON_CONSOLE.threshold";

        Properties testProperties = new Properties();
        testProperties.put(rootLogger, "INFO, CARBON_CONSOLE, CARBON_LOGFILE, CARBON_SYS_LOG");
        testProperties.put(loggerOrgWso2, "INFO");
        testProperties.put(loggerTraceMessages, "TRACE,CARBON_TRACE_LOGFILE");
        testProperties.put(carbonConsole, "org.apache.log4j.ConsoleAppender");
        testProperties.put(carbonConsoleLayout, "org.apache.log4j.PatternLayout");
        testProperties.put(carbonConsoleLayoutConversionPattern, "[%d] %5p {%c} - %x %m%n");
        testProperties.put(carbonConsoleThreshold, "DEBUG");

        File configFileName = new File(getTestResourceFile(PROPERTY_FILE).getAbsolutePath());
        Properties properties = loggingConfiguration.readProperties(configFileName);

        Assert.assertEquals(testProperties.get(rootLogger), properties.get(rootLogger));
        Assert.assertEquals(testProperties.get(loggerOrgWso2), properties.get(loggerOrgWso2));
        Assert.assertEquals(testProperties.get(loggerTraceMessages), properties.get(loggerTraceMessages));
        Assert.assertEquals(testProperties.get(carbonConsole), properties.get(carbonConsole));
        Assert.assertEquals(testProperties.get(carbonConsoleLayout), properties.get(carbonConsoleLayout));
        Assert.assertEquals(testProperties.get(carbonConsoleLayoutConversionPattern), properties.get(carbonConsoleLayoutConversionPattern));
        Assert.assertEquals(testProperties.get(carbonConsoleThreshold), properties.get(carbonConsoleThreshold));
    }

}
