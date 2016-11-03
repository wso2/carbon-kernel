# Welcome to WSO2 Carbon Kernel
WSO2 Carbon Kernel 5.2.0 is the core of the next-generation WSO2 Carbon platform. 

It is completely rearchitected Carbon Kernel from the ground up with the latest technologies and patterns. Additionally, the Carbon Kernel is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

# Architecture and Key Features
Carbon Kernel is a modular, light-weight, OSGi-based server development framework, which provides the base for developing servers. Eclipse Equinox is used as the OSGi runtime from Kernel 5.0.0 onwards. However, you can plug in any OSGi implementation to your Carbon server. The diagram below depicts the architecture of WSO2 Carbon Kernel and its key components.
.......
### Carbon Launcher
The Carbon launcher boots up the Carbon server. This Launcher component implementation resolves the initialization of the Carbon server instance: Before starting the Carbon server, the launcher component performs a set of steps that loads the initial startup configurations. Read about how the Carbon Launcher is used for development.

### Logging Framework with Log4j 2.0 as the backend
The Carbon logging framework is implemented using the PaxLogging framework, which is a well known open-source project that implements strong backend functionalities for logging inside the Carbon server. High performing Log4j 2.0 is used as the logging backend with this framework. For more information on the supported logging API's and configurations, see Configuring the Logging Framework.

### Component Startup Order Resolver
The Carbon startup order resolver implementation resolves the startup order among multiple components. It also notifies a component when all of its dependencies (OSGi services, OSGi bundle, etc) are available. This implementation leverages some of the OSGi constructs, such as Require-Capability and Provider-Capability manifest header entries, to resolve the ordering among components. Read more about the Startup Order Resolver Framework.

### Transport Management
The Carbon transport management feature provides a pluggable extension point, which allows users to add new transports to the existing server. It also manages the lifecycle of all the transports. Read more about transport management.

### Pluggable Runtime Management
The pluggable runtime framework in Carbon can be used to manage third-party runtimes in the Carbon server. This framework provides a good deal of flexibility when managing the lifecycle of a runtime. This is useful in situations where it is necessary to use the server in maintenance mode etc. In this case, each runtime needs to be set to maintenance mode. WSO2 Carbon will use this extension point to trigger a lifecycle change to enable the maintenance mode of the runtime. For example, in case of the Tomcat runtime, you need to stop the Tomcat connectors when the server goes into maintenance mode. Read more about pluggable runtime management.

# How To Contribute
* Please report issues at [WSO2 JIRA](https://wso2.org/jira/browse/Carbon).
* Send your pull requests to [master branch](https://github.com/wso2/carbon-kernel/tree/master).
* You can find more instructions on how to contribute on community site (http://wso2.com/community).

# Contact Us
WSO2 developers can be contacted via the mailing lists:
* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
