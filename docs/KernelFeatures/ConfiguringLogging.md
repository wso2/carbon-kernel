# Configuring Logging for a Carbon Server
> The configurations required for setting up logging in a Carbon server is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

See the following topics for details on logging related configurations in Carbon 5.

* **[Configuring the Logging Framework](#configuring-the-logging-framework)**
 * **[Configuring a logging API for your bundle](#configuring-a-logging-api-for-your-bundle)**
 * **[Configuring the Carbon logging level](#configuring-the-carbon-logging-level)**
* **[Enabling Asynchronous Logging](#enabling-asynchronous-logging)**

## Configuring the Logging Framework

The Logging framework in Carbon Kernel 5 is implemented using [PaxLogging](https://ops4j1.jira.com/wiki/display/paxlogging/Pax+Logging), which is used as the underlying logging library. High performing log4j 2.0 is used as the logging backend with this framework. The Carbon Logging framework supports a number of logging APIs and this allows users to use any logging API in the components that they develop.

See the following topics for details:

### Configuring a logging API for your bundle
Given below are the logging APIs that are currently supported with the framework.

* Jakarta Commons Logging API
* OSGi Log Service API
* JDK Logging API
* Avalon Logger API
* SLF4J API support
* Tomcat Juli API

You can follow the steps given below to get the relevant logging API supported for your bundle or project. 

1. The following is the maven dependency from `pax-logging-api`, which provides support for all the logging APIs mentioned
above. This dependency packs all the logging APIs mentioned above within itself and exports those packages in an OSGi environment.

        <dependency>
            <groupId>org.ops4j.pax.logging</groupId>
            <artifactId>pax-logging-api</artifactId>
            <version>1.8.4</version>
        </dependency>

2. After using the above dependency, you can add the relevant import statement to the bundle plugin section under `<Import-Package>` as shown below. This example imports the `slf4j` logging API package, with a specific version range. Likewise, you can import packages for the other logging APIs mentioned above.

        <Import-Package>
        org.slf4j.*;version="[1.7.1, 2.0.0)"
        </Import-Package> 

### Configuring the Carbon logging level
Given below is the basic `log4j-based` logging configuration used in kernel. This configuration file (`log4j2.xml`) is located in the `<CARBON_HOME>/conf` directory. 

    <Configuration>
    <Appenders>
        <Console name="CARBON_CONSOLE" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d] %5p {%c} - %m%ex%n"/>
        </Console>
        <RollingFile name="CARBON_LOGFILE" fileName="${sys:carbon.home}/logs/carbon.log"
                     filePattern="${sys:carbon.home}/logs/carbon-%d{MM-dd-yyyy}.log">
            <PatternLayout pattern="[%d] %5p {%c} - %m%ex%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
            </Policies>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="CARBON_CONSOLE"/>
            <AppenderRef ref="CARBON_LOGFILE"/>
        </Root>
    </Loggers>
    </Configuration>

For example, to enable debug logs on the `org.wso2.carbon.kernel.runtime` package, you can add the following entry to the `log4j2.xml` file.

        <Logger name="org.wso2.carbon.kernel.runtime" level="debug"/>

The logs are published to both the console and the `carbon.log` file (located in the `<CARBON_HOME>/logs` directory). The `RollingFile` appender is used as the file logger, which is configured to use the default `TimeBasedTriggeringPolicy` policy as the rolling policy. This will rollout the `carbon.log` file at the start of each day with the file pattern : `carbon-%d{MM-dd-yyyy}.log`
Most of the configurations related to `log4j2` are available in the official documentation: https://logging.apache.org/log4j/2.x/manual/configuration.html

## Enabling Asynchronous Logging
WSO2 Carbon 5 Kernel uses the logging framework with Pax Logging. You can find out more about this logging framework and the default configurations from [here](#configuring-the-logging-framework). You can follow the steps given below if you want to enable [asynchronous logging](https://logging.apache.org/log4j/2.x/manual/async.html) for your Carbon 5-based server.

1. Download the disrupter OSGi bundle from [here](http://mvnrepository.com/artifact/com.lmax/disruptor/3.2.0) and copy it to the `<CARBON_HOME>/wso2/lib/plugins` directory.
2. Open the `launch.properties` file from the `<CARBON_HOME>/conf/osgi` folder and add the disrupter JAR to the initial bundles list as shown below.

        carbon.initial.osgi.bundles=file\:plugins/disruptor-3.2.0.jar@2\:true,
	
 > The [Carbon Launcher](SettingUptheCarbonLauncher.md) component will initialize the bundles listed below when the server is started.

3. Open the `pax-logging.properties` file from the `<CARBON_HOME>/conf/etc` folder and set the 
`org.ops4j.pax.logging.log4j2.async` parameter to true as shown below.

        org.ops4j.pax.logging.log4j2.config.file=${carbon.home}/conf/log4j2.xml
        org.ops4j.pax.logging.log4j2.async=true

4. Open the `log4j2.xml` file from the `<CARBON_HOME>/conf` folder and the async loggers as shown below.

        <Configuration>
            <Appenders>
    	        <RandomAccessFile name="RandomAccessFile" fileName="${sys:carbon.home}/logs/carbon.log" immediateFlush="false" append="false">
       		        <PatternLayout>
          	        <Pattern>[%d] %5p {%c} - %m%ex%n</Pattern>
        		        </PatternLayout>
                </RandomAccessFile>
                <Console name="CARBON_CONSOLE" target="SYSTEM_OUT">
                    <PatternLayout pattern="[%d] %5p {%c} - %m%ex%n"/>
                </Console>
            </Appenders>

            <Loggers>
		        <AsyncLogger name="com.foo.Bar" level="trace" includeLocation="true">
      		        <AppenderRef ref="RandomAccessFile"/>
    	        </AsyncLogger>
                <Root level="debug">
     		        <AppenderRef ref="RandomAccessFile"/>
                    <AppenderRef ref="CARBON_CONSOLE"/>
                </Root>
            </Loggers>
        </Configuration>
