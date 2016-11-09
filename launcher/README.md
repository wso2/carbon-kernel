
# Resolving the Component Startup Order
WSO2 introduces a Carbon component startup order resolver implementation that does not need to leverage the existing startup ordering mechanism in OSGi. This new implementation resolves the startup order among multiple components. Further, it notifies a component when all of its dependencies (OSGi services, OSGi bundle etc.) are available. The following sections explain how this solution works, and how it can be implemented for your components.

### Why we need a startup order resolver
WSO2 Carbon Kernel provides an OSGi-based framework for developing enterprise-grade, server-side applications. Transport management, runtime management, centralized logging and deployment engine are some of its core features. When you start a Carbon server, there will be requirements where some components need to wait until other components are initialized (inter-component dependencies). Also, there will be requirements where a component needs to wait until all of its internal services and extensions are available (intra-component dependencies).
The following diagram depicts a scenario with both types of dependencies among Carbon components:
.....
The Microservice Manager and the Transport Manager are Carbon components. Both these components contain one or more related OSGi bundles. In this scenario, the Transport Manager component should be initialized after the Microservice Manager. This is because, the Transport Manager should not open any ports until all the microservices are properly deployed. You can easily handle this via OSGi services:
  1. The Microservice Manager component registers an OSGi service when it is fully initialized. 
  2. The Transport Manager component gets activated soon after that service is available.
Things get complicated when you think about other dependencies of the Microservice Manager and the Transport Manager. Therefore, the initialization of components in the above scenario should happen in the following order:
  1. The Microservice Manager component should only be initialized after all the required microservices are registered.
  2. The Transport Manager component should only be initialized:
        a. after the Microservice Manager component is fully initialized.
        b. after all the transport services are registered from individual bundles during the server startup. For example, the HTTPS transport bundle registers a transport as an OSGi service. There are five such transport bundles shown in the above figure. Therefore, during server startup, you can expect five different OSGi services of type 'transport'. Now, the Transport Manager has to wait until all five OSGi services are available.
       
Transport Manager does not need to wait for OSGi services of type 'transport' that come from transport bundles that get installed after the server starts. Transport Manager only waits for OSGi services of type 'transport' that become available during server startup. Therefore, if someone adds a new transport, or removes an existing transport bundle, the number of transport OSGi services will change. Hence, it is not static, and thereby, you have to calculate the expected number of transport services at runtime. OSGi declarative services do not solve this problem.

### The solution
You need to know the number of OSGi services that are expected by the component. You need to calculate the expected number of services using static resources such as MANIFEST.MF files of OSGi bundles, or any other files inside OSGi bundles. When you know the expected number of OSGi services, you can hold the initialization of the component until all those services are available at runtime.
This may break the dynamism of OSGi because OSGi services can come and go at any time, and thereby depending on a specific number of OSGi services could be tricky. However, WSO2 develops middleware products and those are server-side components. You need to resolve the startup order of WSO2 components. These specific requirements cannot be satisfied using the default mechanisms in OSGi. 
WSO2 recently introduced a framework called WSO2 Microservices Framework for Java (MSF4J), to develop microservices. You can develop and run microservices in a standalone mode and in WSO2 Carbon-based products as OSGi bundles. If you are using Carbon-based products to host your microservices, then you need to register your microservices as OSGi services. Now, when the microservices engine receives a request, engine dispatches it to the correct microservice. Neverthless, microservices engine cannot accept requests until it knows that all the microservices are available as OSGi services.
Otherwise, during server startup, certain microservices will be available while other microservices are yet to be registered as OSGi services. You shouldn’t open ports until all the microservices are available. Microservices engine should notify when all the microservices are registered as OSGi services during the startup. Then only Transport Manager should open ports. All this should happen during the server startup. The Carbon startup order resolver component solves this problem in OSGi.

### About the Carbon startup order resolver
Startup order resolver component is available from WSO2 Carbon kernel 5.0.0 onwards, and its design handles startup ordering complexities in WSO2 Carbon-based products. This is a generic utility, which can resolve startup order of any other component such as microservice engine, Transport Manager etc. 
The startup order resolver component in Carbon ensures that a particular component is not started until all the other components required by that component are fully initialized. To achieve this, the components with dependencies are separated into two categories as shown below.
  1. Startup listener components: Components that need to hold its initialization, until all the required OSGi services or capabilities are available during server startup. For example, the Microservice engine needs to hold it’s initialization until all the microservices from OSGi bundles that are available during server startup are registered as OSGi services. 
  2. OSGi service components: Components that register OSGi services. For example, user management components expose it’s capabilities via a microservice. JMS transport module registers the JMS transport as OSGi services. 
  
#### Defining a startup listener component
A component has to be defined as a listener component, if there are other components that should be initialized before the listener component is initialized. For example, in the above scenario the Transport Manager component should be defined as a listener component, because it requires other transport components to be initialized before it. 
An OSGi listener component is defined as shown below.

The startup order resolver identifies a startup listener component from the following manifest header:
Carbon-Component: startup.listener;
componentName=”transport-mgt”;
requiredService=”org.wso2.carbon.kernel.transports.CarbonTransport”;
startup.listener : Marks this component as a startup listener.
componentName : This is a unique name to identify the component. Each and every startup listener component should have a unique name.
requiredService : A comma separated list of OSGi service keys. These are the OSGi services that the listener component should wait for. That is, the startup listener component should hold it’s initialization until all the services of specified keys are available.
The startup order resolver notifies a startup listener component when all the required services are available. In order to get this notification, the startup listener component should register an OSGi service with the following interface: org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener.
An implementation of the RequiredCapabilityListener interface is shown below.
public class TransportStartupListener implements RequiredCapabilityListener {
    
    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        // This method is invoked by the startup 
           order resolver when all the required 
           services are available
    }
}

Shown below is how you register the RequiredCapabilityListener implementation given above as an OSGi service in your BundleActivator.
public class TransportBundleActivator implements BundleActivator {
public void start(BundleContext bundleContext)
            throws Exception {

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("componentName", "transport-mgt");
        bundleContext.registerService(
                TransportStartupListener.class,
                new TransportStartupListener(), properties);
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }
}
The value given for the componentName property should be equal to the componentName property value of the startup listener component that was defined earlier. This is how the startup order resolver maps the RequiredCapabilityListener with corresponding startup listener components (startup.listener).

#### Defining an OSGi service component
A component is required to be defined as an OSGi service component when there are other components depending on the initialization of this component. For example, the Transport Manager component will only be started once the relevant transports are already initialized. Therefore, the transport implementation should be defined as OSGi service components.
Registering a single OSGi service from a component
The startup order resolver identifies an OSGi service component from the following manifest header:
Carbon-Component: osgi.service; 
objectClass=”org.wso2.carbon.kernel.transports.CarbonTransport”
osgi.service: This property marks this component as an OSGi service component.
objectClass: This property indicates the type of OSGi service that will be registered by the component. 
Please note that all components that register OSGi services do not need to include this manifest header. You need to include this header only if there are other components waiting for your OSGi service. For example, the Transport Manager component waits for all the transport OSGi services. If you are developing a transport, you need to put this header into your bundle’s MANIFEST.MF.
Registering multiple OSGI services from a component
When you define an OSGi service component, the component bundle may need to register more than one OSGi service of the same type. This can be done in two ways as explained below.
Registering a known number of services
You can use the serviceCount manifest attribute to specify the number of services that you register from your bundle as shown below. Here you know the exact number of services that your register at development time.
Carbon-Component: osgi.service; 
objectClass=”org.wso2.carbon.kernel.transports.CarbonTransport”; 
serviceCount="4"

Registering an indefinite number of services 
In certain scenarios, you may not know the number of OSGi services you register from your bundle at the time of development. However, during server startup, you can calculate the number of OSGi services that need to be registered. You may obtain this value from a configuration file, from another OSGi service or from any source. In this scenario, you cannot use the serviceCount manifest attribute, since you don’t know the count at development time.
In order to solve this problem, we have introduced an interface called CapabilityProvider from the org.wso2.carbon.kernel.startupresolver package. 
You can register the OSGi service as shown below.
public class HTTPTransportProvider implements CapabilityProvider {
    
    @Override
    public int getCount() {
        return 4;
    }
}Now you need specify the Carbon-Component manifest header as follows.

Now you need to specify the "Carbon-Component" manifest header as shown below.
Carbon-Component: osgi.service; 
objectClass=”org.wso2.carbon.kernel.startupresolver.CapabilityProvider”;
capabilityName="org.wso2.carbon.kernel.transports.CarbonTransport";

Shown below is how you can register the CapabilityProvider implementation shown above as an OSGi service in your BundleActivator.
public class HTTPBundleActivator implements BundleActivator {
    public void start(BundleContext bundleContext)
            throws Exception {

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("capabilityName",
              "org.wso2.carbon.kernel.transports.CarbonTransport");
        bundleContext.registerService(
                HTTPTransportProvider.class,
                new HTTPTransportProvider(), properties);
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }
}

As explained above, the startup order resolver processes the Carbon-Component manifest headers, and figures out the components that need to be notified when all requirements are satisfied. Similarly, the startup order resolver figures out the expected number of OSGi services for each startup listener component. The startup order resolver listens to OSGi service events, and notifies startup listener components, as and when their requirements are satisfied.

# Setting up the Carbon Launcher

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

The properties in the launch.properties file are explained below.

Property	Description
carbon.osgi.repository=file\:osgi	The location of the OSGi repository for Carbon kernel.
carbon.osgi.framework=file\:plugins/org.eclipse.osgi_3.10.2.v20150203-1939.jar	This property specifies the OSGi framework implementation bundle, which starts during the Carbon server startup.
carbon.initial.osgi.bundles=\file\:plugins/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar@1\:true
Set of bundles (in a comma separated list) that need to be populated when starting the server. This allows a preferred runtime implementation of the OSGi framework to be plugged.
carbon.server.listeners=org.wso2.carbon.launcher.extensions.DropinsBundleDeployer	
The Carbon server listeners (in a comma separated list) that get notified when the server startup and server stop events are executed. You can add new Carbon server listeners by implementing the org.wso2.carbon.launcher.CarbonServerListener interface.
org.osgi.framework.startlevel.beginning=10	The initial start level of the framework once it begins execution.
osgi.install.area	The location where the platform is installed. This setting indicates the location of the basic Eclipse plug-ins, which are used by the OSGi runtime during installation.
osgi.configuration.area	The configuration location for this platform runtime. The configuration determines the location where the OSGi runtime should store configuration information about the bundles you install during run time.
osgi.instance.area	The instance data location for this session. Plug-ins use this location to store their data eg:workspace

### Server startup process

1. Before loading the configurations from the launch configuration (launch.properties) file, as the first step the required system properties (eg: carbon.home, profile) will be initialized and verified.
2. The default launch configuration from the classpath will be loaded, followed by the configurations in the launch.properties file. This process completes after initializing the required properties from the property list and registering Carbon server listeners and OSGi runtime implementations.
3. Starts the carbon server that launches the OSGi framework and loads all the bundles. The Carbon server is now completely started.
4. Based on the loaded configurations, an OSGi framework instance (which is an installed bundled) is created. This framework instance is then initialized, started and all the bundles are resolved if their requirements can be satisfied.
5. Bundles (read from the launch.properties file with key “carbon.initial.osgi.bundles”) are then installed in the bundle's execution context within the framework. This enables bundles to interact with the framework.
6. Carbon server listeners will be notified during the server start and server stop events.

 A Carbon Server Listener is an extension point that may be implemented by a Carbon developer. This can be done by implementing the notify() method of the org.wso2.carbon.launcher.CarbonServerListener interface. This is a useful feature for scenarios where you need to perform certain tasks before launching the OSGi framework as well as after the OSGi framework shuts down. These listeners will get notified before initializing the OSGi framework and after shutting down the OSGi framework. You can register Carbon listener implementations in the launch.properties with the key “carbon.server.listeners”.

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

### Server startup logs

During the server startup process, the launcher component uses the java-util-logging API to publish records to the product startup console or the <CARBON_HOME>/logs/carbon.log file. Bootstrap logger maintains two separate handlers: 

 ConsoleLogHandler for configuring java.util.logging, which appends to the Carbon console. This can be used for bootstrap logging via java.util.logging prior to the startup of pax logging.

 FileLogHandler for configuring java.util.logging, which appends to the wso2carbon.log file. This could be used for bootstrap logging prior to framework startup.

For instructions on how to configure this logging facility, see the documentation on monitoring Carbon startup logs.
