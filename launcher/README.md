
## Setting up the Carbon Launcher

The WSO2 Carbon Launcher is responsible for initializing and booting up the Carbon server. This Launcher implementation resolves the initialization of the Carbon server instance. Before starting the Carbon server, the Launcher component performs a set of steps to load the initial startup configurations given in the default <CARBON_HOME>/bin/bootstrap/org.wso2.carbon.launcher-5.0.0.jar/launch.properties file.
WSO2 Carbon Kernel 5.1.0 includes the <CARBON_HOME>/conf/osgi/launch.properties file, to change the default launch configurations. Therefore, if you want to customize the startup process by updating the configurations in the default launch.properties file, you can do so by updating this second file.
For detailed explanations on configuring the Launcher component, see the following topics.

### Configuring the Launcher

The new <CARBON_HOME>/conf/osgi/launch.properties  file stores all the load configurations. This file contains the set of properties that are required by the Carbon server to start up. The default launch.properties file is available in the Carbon server classpath. It contains all the required properties and their default values. If you want to override these default values or add new properties, you can specify the required properties and values in the launch.properties file.

Shown below are the default properties given in the launch.properties file.

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
