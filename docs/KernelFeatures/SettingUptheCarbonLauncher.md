# Setting Up the Carbon Launcher

> The process of setting up the Carbon Launcher in a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

The WSO2 Carbon Launcher is responsible for initializing and booting up the Carbon server. This Launcher implementation resolves the initialization of the Carbon server instance. Before starting the Carbon server, the Launcher component performs a set of steps to load the initial startup configurations given in the default `<CARBON_HOME>/bin/bootstrap/org.wso2.carbon.launcher-5.0.0.jar/launch.properties` file.
WSO2 Carbon Kernel 5 includes the `<CARBON_HOME>/conf/osgi/launch.properties` file for changing the default launch configurations. Therefore, if you want to customize the startup process by updating the configurations in the default `launch.properties` file, you can do so by updating this second file.

For detailed explanations on configuring the Launcher component, see the following topics.
* **[Configuring the Launcher](#configuring-the-launcher)**
* **[Server startup process](#server-startup-process)**
* **[Monitoring server startup logs](#monitoring-server-startup-logs)**

## Configuring the Launcher

The new `<CARBON_HOME>/conf/osgi/launch.properties` file stores all the load configurations. This file contains the set of properties that are required by the Carbon server to start up. The default `launch.properties` file is available in the Carbon server classpath. It contains all the required properties and their default values. If you want to override these default values or add new properties, you can specify the required properties and values in the `launch.properties` file.

Shown below are the default properties given in the `launch.properties` file.

    # OSGi repository location of the Carbon kernel.
    carbon.osgi.repository=file\:osgi

    carbon.osgi.framework=file\:plugins/org.eclipse.osgi_3.10.2.v20150203-1939.jar

    carbon.initial.osgi.bundles=\
      file\:plugins/org.eclipse.osgi.services_3.4.0.v20140312-2051.jar@1\:true,\
     file\:plugins/org.ops4j.pax.logging.pax-logging-api_1.8.4.jar@2\:true,\
      file\:plugins/org.ops4j.pax.logging.pax-logging-log4j2_1.8.4.jar@2\:true,\
      file\:plugins/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar@3\:true

    osgi.install.area=file\:${profile}
    osgi.configuration.area=file\:${profile}/configuration
    osgi.instance.area=file\:${profile}/workspace
    eclipse.p2.data.area=file\:p2

    # Enable OSGi console. Specify a port as value of the following property to allow
    # telnet access to OSGi console
    osgi.console=

    org.osgi.framework.bundle.parent=framework

    # The initial start level of the framework once it starts execution; the default value is 1.
    org.osgi.framework.startlevel.beginning=10

    # When osgi.clean is set to "true", any cached data used by the OSGi framework
    # will be wiped clean. This will clean the caches used to store bundle
    # dependency resolution and eclipse extension registry data. Using this
    # option will force OSGi framework to reinitialize these caches.
    # The following setting is put in place to get rid of the problems
    # faced when re-starting the system. Please note that, when this setting is
    # true, if you manually start a bundle, it would not be available when
    # you re-start the system. To avid this, copy the bundle jar to the plugins
    # folder, before you re-start the system.
    osgi.clean=true

    # Uncomment the following line to turn on Eclipse Equinox debugging.
    # You may also edit the osgi-debug.options file and fine tune the debugging
    # options to suite your needs.
    #osgi.debug=./conf/osgi/osgi-debug.options

    org.eclipse.equinox.simpleconfigurator.useReference=true
    org.eclipse.equinox.simpleconfigurator.configUrl=file\:org.eclipse.equinox.simpleconfigurator/bundles.info
 
    #carbon.server.listeners=

The properties in the `launch.properties` file are explained below:

* `carbon.osgi.repository=file\:osgi`: The location of the OSGi repository for Carbon kernel.
* `carbon.osgi.framework=file\:plugins/org.eclipse.osgi_3.10.2.v20150203-1939.jar`: This property specifies the OSGi framework  implementation bundle, which starts during the Carbon server startup.
* `carbon.initial.osgi.bundles=\file\:plugins/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar@1\:true`: Set of bundles (in a comma separated list) that need to be populated when starting the server. This allows a preferred runtime implementation of the OSGi framework to be plugged.
* `carbon.server.listeners=org.wso2.carbon.launcher.extensions.DropinsBundleDeployer`: The Carbon server listeners (in a comma separated list) that get notified when the server startup and server stop events are executed. You can add new Carbon server listeners by implementing the `org.wso2.carbon.launcher.CarbonServerListener` interface.
* `org.osgi.framework.startlevel.beginning=10`: The initial start level of the framework once it begins execution.
* `osgi.install.area`: The location where the platform is installed. This setting indicates the location of the basic Eclipse plug-ins, which are used by the OSGi runtime during installation.
* `osgi.configuration.area`: The configuration location for this platform runtime. The configuration determines the location where the OSGi runtime should store configuration information about the bundles you install during run time.
* `osgi.instance.area`: The instance data location for this session. Plugins use this location to store their data eg:workspace.

## Server startup process

1. Before loading the configurations from the launch configuration (`launch.properties`) file, as the first step the required system properties (eg: `carbon.home`, `profile`) will be initialized and verified.
2. The default launch configuration from the classpath will be loaded, followed by the configurations in the `launch.properties` file. This process completes after initializing the required properties from the property list and registering Carbon server listeners and OSGi runtime implementations.
3. Starts the carbon server that launches the OSGi framework and loads all the bundles. The Carbon server is now completely started.
4. Based on the loaded configurations, an OSGi framework instance (which is an installed bundled) is created. This framework instance is then initialized, started and all the bundles are resolved if their requirements can be satisfied.
5. Bundles (read from the `launch.properties` file with key `“carbon.initial.osgi.bundles”`) are then installed in the bundle's execution context within the framework. This enables bundles to interact with the framework.
6. Carbon server listeners will be notified during the server start and server stop events.

 > A Carbon Server Listener is an extension point that may be implemented by a Carbon developer. This can be done by implementing the `notify()` method of the `org.wso2.carbon.launcher.CarbonServerListener` interface. This is a useful feature for scenarios where you need to perform certain tasks before launching the OSGi framework as well as after the OSGi framework shuts down. These listeners will get notified before initializing the OSGi framework and after shutting down the OSGi framework. You can register Carbon listener implementations in the `launch.properties` with the key `“carbon.server.listeners”`.
 Shown below is how a Carbon Server Listener is implemented.
 
       /**
      * This is an interface which may be implemented by a Carbon developer to get notified of the Carbon server startup and
      * the Carbon server shutdown. These listener implementations will get notified before launching the OSGi framework
      * as well as after shutting down the OSGi framework. CarbonServer notifies these listeners synchronously.
      *
      * To register a CarbonServerListener, add the fully qualified class name to carbon.server.listeners property in
      * launch.properties file. This property accepts a list of comma separated fully qualified class names.
      */
      public interface CarbonServerListener {
        /**
       * Receives notification of a CarbonServerEvent.
       *
       * @param event CarbonServerEvent
       */
        public void notify(CarbonServerEvent event);
       }
            
7. After successfully starting the Carbon server, a thread is maintained until the OSGi framework completely shuts down. This thread will call the server start or server stop events, thereby monitoring the framework event status.

## Monitoring server startup logs

During the server startup process, the launcher component uses the `java-util-logging` API to publish records to the product startup console or the carbon.log file (stored in the `<CARBON_HOME>/logs` directory). 

WSO2 Carbon maintains a separate configuration file (`logging.properties`) to control the logging details of the `java.util.logging` framework. This file is stored in the `<CARBON_HOME>/bin/bootstrap` directory. There are two handlers defined in the default configuration:

* `java.util.logging.FileHandler`: For configuring `java.util.logging` to append to `carbon.log`.
* `java.util.logging.ConsoleHandler`: For configuring `java.util.logging` to append to carbon console.

The global level of the configuration is set to INFO. 

The default values for the handlers are set as shown in the below table.
 
|                     | `java.util.logging.FileHandler`       | `java.util.logging.ConsoleHandler`    |
| :-----------------: |:-------------------------------------:| :------------------------------------:|
| Default level       | `INFO`                                | `INFO`                                |
| Default Formatter   | `java.util.logging.SimpleFormatter`   | `java.util.logging.SimpleFormatter`   |
| Logging destination | `<CARBON-HOME>/log/carbon.log`        | `Console`                             |

See the following topics for instructions on changing the default configuration:

### Changing the log levels of a default handler
Consider a scenario where you need to save all the logs of level FINE in `java.util.logging` only to the `carbon.log` file (not to the Console). To achieve this, you can do the following:

* Set the global logging level to `FINE` (or a lower level than `FINE`).
* Set the logging level of the `FileHandler` to `FINE`.
* Set the logging level of the `ConsoleHandler` to `INFO` (default value).

Please find the sample logging.properties file below:

    handlers= java.util.logging.FileHandler, java.util.logging.ConsoleHandler

    .level= FINE

    java.util.logging.FileHandler.level = FINE
    java.util.logging.FileHandler.pattern = logs/carbon.log
    java.util.logging.FileHandler.limit = 50000
    java.util.logging.FileHandler.count = 1
    java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
    java.util.logging.SimpleFormatter.format = [%1$tY-%1$tm-%1$td %1$tk:%1$tM:%1$tS,%1$tL]  %4$s {%2$s} - %5$s %6$s %n

    java.util.logging.ConsoleHandler.level = INFO
    java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

### Changing the log levels of a single package/class

As explained above, changing the log level of a handler will cause the logs of all `java.util.logging` to be logged to the configured destination (`carbon.log` file or the Console). If you need to change the log level of one specific class, you can update the logging configuration at class level as explained below:

* Consider a scenario where you need to skip the FINE logs from the `org.wso2.carbon.launcher.CarbonServer` class, but you need to log all the `FINE` logs from other classes. A sample configuration for this scenario is as follows:

         .level= FINE
        java.util.logging.FileHandler.level = INFO
        …
        java.util.logging.ConsoleHandler.level = FINE  <- - -  prints FINE logs to the Console
        …
        org.wso2.carbon.launcher.CarbonServer.level=INFO    <- - - prints only info logs of this class 

* Given below is a sample configuration where only the severe logs of a class/package will be logged.

        .level= INFO
        java.util.logging.FileHandler.level = INFO
        …
        java.util.logging.ConsoleHandler.level = INFO 
        …
        com.xyz.foo.level = SEVERE    <- - - prints only SEVERE logs of this class
