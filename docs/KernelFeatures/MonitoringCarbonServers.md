# Monitoring Carbon Servers
> In addition to the monitoring capabilities explained below, you can monitor server startup logs from the Carbon Launcher. Find out more from the [README.MD file of the Carbon Launcher](SettingUptheCarbonLauncher.md#monitoring-server-startup-logs). For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools).  

You can monitor the Carbon server using the following options:

* **[Using Audit Logs](#using-audit-logs)**
* **[Using MBeans for Monitoring](#using-mbeans-for-monitoring)**

## Using Audit Logs

Auditing is a primary requirement when it comes to monitoring production servers. For examples, DevOps need to have a clear mechanism for identifying who did what, and to filter possible system violations or breaches. Further, when you are developing a Carbon component, you need an API provided by Carbon Kernel to use this auditing feature.

Audit logs or audit trails contain a set of log entries that describe a sequence of actions that occurred over a period of time. Audit logs allow you to trace all the actions of a single user, or all the actions or changes introduced to a certain module in the system etc. over a period of time. For example, it captures all the actions of a single user from the first point of logging in to the server.

> Audit logs are stored in the `audit.log` file, located in the `<CARBON_HOME>/logs` directory.

See the following topics for details.

### Adding audit logs to a Carbon component

The following steps will guide you on how to add new audit logs when developing a Carbon component:

1. Add the required audit logs for your component by using the `org.wso2.carbon.kernel.Constants.AUDIT_LOG` logger that is available in Carbon Kernel by default. 

 > Note that there is a separate `log4j` daily rolling appender named `AUDIT_LOG` for adding audit logs to the `<CARBON_HOME>/logs/audit.log` file. The `org.wso2.carbon.kernel.Constants.AUDIT_LOG logger` is an instance of this logger. 
 
 Shown below is an example of an audit log that can be added to a Carbon component. 

        final Logger audit = org.wso2.carbon.kernel.Constants.AUDIT_LOG;
        audit.info("Attempting to test the audit logs.");

 For a test case on this log appender, see [`LoggingConfigurationOSGiTest.java`](https://github.com/wso2/carbon-kernel/blob/master/tests/osgi-tests/src/test/java/org/wso2/carbon/osgi/logging/LoggingConfigurationOSGiTest.java#L113).

2. If it is necessary to capture the contextual information (such as the logged in user details, remote IP address of client etc.) from a logging instance in your audit logs, you can specify the required contextual information for your component using [`SLF4J MDC`](http://www.slf4j.org/manual.html#mdc).

 Shown below is the default configuration in Carbon Kernel, where the "user-name" key is added to the MDC for the purpose of capturing the user name of the logged in user. This will ensure that the user name of the logged in user is added to the audit logs, and thereby, you do not need any additional configurations to get this information logged. 

        org.slf4j.MDC.put("user-name", userPrincipal.getName());

 > Note that the "user-name" key will be effective when the `javax.security.Principal` user gets set in the `PrivilegedCarbonContext`. Find more information about [using the `CarbonContext` API](UsingtheCarbonContext.md).

Prior to Carbon 5, it was necessary for developers to manually capture any required contextual information (such as the logged in user details, remote IP address of client etc.) at every logging instance. These contextual details then had to be manually added to the audit logs. With the new approach introduced in WSO2 Carbon 5.1.0, [SLF4J Mapped Diagnostic Context (MDC)](http://www.slf4j.org/manual.html#mdc) will capture the contextual information when you specify the relevant keys as shown in the above example. Therefore, there is no need to capture the contextual information for every logging instance because the values added to the MDC in the first instance will remain within the thread.

### Viewing audit logs

Carbon users and component developers can view local audit logs using the `<CARBON_HOME>/logs/audit.log` file. This file can be configured as a daily, rolling log file.

## Using MBeans for Monitoring
Java Management Extensions (JMX) is a standard technology in the Java platform. It provides a simple mechanism for managing resources such as applications, devices, and services. The Carbon Kernel leverages the features of the existing JMX Platform MBean Server by providing Carbon 5-based authentication for additional security and by making JMX resources available for remote access. Components inherit the platform MBean server from the Carbon Kernel. Therefore, you can simply register the resources of your Carbon component in this MBean server and expose them to remote users.

> Note that `javax.management.*` is the only required dependency.

See the following topics for instructions.

### About the JMX monitoring implementation in WSO2 Carbon

The `CarbonJMXComponent` implementation in Carbon 5 Kernel uses the existing JMX platform MBean server in Carbon and exposes the registered MBeans to remote users via the `JMXConnectorServer` implementation. In this process, user authentication is performed by the `CarbonJMXAuthenticator` implementation. A user can modify the service configuration in the `jmx.yaml` file, located in the `<CARBON_HOME>/conf` directory. The connection URL that exposes the MBeans is as follows: `service:jmx:rmi://localhost:9700/jndi/rmi://localhost:9800/jmxrmi`.

### Registering MBeans in Carbon
Once you register the MBeans for your Carbon component, they will be exposed for monitoring as explained above. The code given below illustrates how you can register an MBean in a Carbon component. In the following example, we have registered “TestMBean” in the `PlatformMBeanServer`.

    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    ObjectName mbeanName = new ObjectName("org.wso2.carbon.jmx.sample:type=Test");
    mBeanServer.registerMBean(new Test(), mbeanName);

### Monitoring MBeans in Carbon using a JMS client

Monitoring MBeans is easy with `jconsole`. Follow the steps given below.

1. Go to the **New Connection** window. 
2. Select **Remote Process** and provide the connection URL with proper hostname and ports.
3. Type in a valid username and a password (**Username:** ”username”, **Password:** “password”). 
4. Finally, click **Connect**.
