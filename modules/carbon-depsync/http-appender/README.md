A custom log4j2 appender to send log events as a HTTP POST request to  a given hostname and port. Add a <HTTP> element to log4j2.xml to send logs to this appender. Please find the sample configuration below.

<HTTP name="HTTP_APPENDER" host="localhost" port="8080" connectTimeoutMillis="10000" immediateFlush="true" ignoreExceptions="true">
      		<PatternLayout pattern="[%d] %5p {%c} [%hostname] - [%ip]- %m%ex%n"/>
</HTTP>

Please find a sample configuration of a log4j2.xml file below:

<Configuration status="INFO" packages="org.wso2.carbon.http.socket.appender,org.wso2.carbon.host.converter">
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
        <HTTP name="HTTP_APPENDER" host="localhost" port="8080" connectTimeoutMillis="10000" immediateFlush="true" ignoreExceptions="true">
      		<PatternLayout pattern="[%d] %5p {%c} [%hostname] - [%ip]- %m%ex%n"/>
    	</HTTP>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="CARBON_CONSOLE"/>
            <AppenderRef ref="CARBON_LOGFILE"/>
        </Root>
        <Logger name="org.wso2.carbon.custom.deployer" additivity="true">
            <AppenderRef ref="HTTP_APPENDER"/>
        </Logger>
    </Loggers>
</Configuration>
