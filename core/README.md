> The core capabilities that are available at Carbon Kernel-level are listed below. For the full list of capabilities available in Carbon Kernel 5.1.0, see the [root README.md file](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/README.md). 

Follow the relevant link for information on each capability:

* **[Resolving the Component Startup Order](#resolving-the-component-startup-order)**
* **[Adding New Transports](#adding-new-transports)**
* **[Plugging a New Runtime](#plugging-a-new-runtime)**
* **[Using the CarbonContext API](#using-the-carboncontext-api)**
* **[Developing a Carbon Tool](#developing-a-carbon-tool)**
* **[Configuring Logging for a Carbon Server](#configuring-logging-for-a-carbon-server)**
* **[Monitoring Carbon Servers](#monitoring-carbon-servers)**

# Resolving the Component Startup Order
WSO2 introduces a Carbon component startup order resolver implementation that does not need to leverage the existing startup ordering mechanism in OSGi. This new implementation resolves the startup order among multiple components. Further, it notifies a component when all of its dependencies (OSGi services, OSGi bundle etc.) are available. The following sections explain how this solution works, and how it can be implemented for your components.

See the following sub topics:

* **[Why we need a startup order resolver](#why-we-need-a-startup-order-resolver)**
* **[The solution](#the-solution)**
* **[About the Carbon startup order resolver](#about-the-carbon-startup-order-resolver)**
 * **[Defining a startup listener component](#defining-a-startup-listener-component)**
 * **[Defining an OSGi service component](#defining-an-osgi-service-component)**

## Why we need a startup order resolver
WSO2 Carbon Kernel provides an OSGi-based framework for developing enterprise-grade, server-side applications. Transport management, runtime management, centralized logging and deployment engine are some of its core features. When you start a Carbon server, there will be requirements where some components need to wait until other components are initialized (inter-component dependencies). Also, there will be requirements where a component needs to wait until all of its internal services and extensions are available (intra-component dependencies).

The following diagram depicts a scenario with both types of dependencies among Carbon components:

![startup-order-resolver - untitled page-3](https://cloud.githubusercontent.com/assets/21237558/20616362/bdb3d7dc-b307-11e6-9f4c-04331da88b9f.jpeg)

The **Microservice Manager** and the **Transport Manager** are Carbon components. Both these components contain one or more related OSGi bundles. In this scenario, the Transport Manager component should be initialized after the Microservice Manager. This is because, the Transport Manager should not open any ports until all the microservices are properly deployed. You can easily handle this via OSGi services:

  1. The Microservice Manager component registers an OSGi service when it is fully initialized. 
  2. The Transport Manager component gets activated soon after that service is available.
  
Things get complicated when you think about other dependencies of the Microservice Manager and the Transport Manager. Therefore, the initialization of components in the above scenario should happen in the following order:

  1. The Microservice Manager component should only be initialized after all the required microservices are registered.
  2. The Transport Manager component should only be initialized:
  
   a. after the Microservice Manager component is fully initialized. 

   b. after all the transport services are registered from individual bundles during the server startup. For example, the HTTPS transport bundle registers a transport as an OSGi service. There are five such transport bundles shown in the above figure. Therefore, during server startup, you can expect five different OSGi services of type 'transport'. Now, the Transport Manager has to wait until all five OSGi services are available.
       
> Transport Manager does not need to wait for OSGi services of type 'transport' that come from transport bundles that get installed after the server starts. Transport Manager only waits for OSGi services of type 'transport' that become available during server startup. Therefore, if someone adds a new transport, or removes an existing transport bundle, the number of transport OSGi services will change. Hence, it is not static, and thereby, you have to calculate the expected number of transport services at runtime. OSGi declarative services do not solve this problem.

## The solution
You need to know the number of OSGi services that are expected by the component. You need to calculate the expected number of services using static resources such as `MANIFEST.MF` files of OSGi bundles, or any other files inside OSGi bundles. When you know the expected number of OSGi services, you can hold the initialization of the component until all those services are available at runtime.

This may break the dynamism of OSGi because OSGi services can come and go at any time, and thereby depending on a specific number of OSGi services could be tricky. However, WSO2 develops middleware products and those are server-side components. You need to resolve the startup order of WSO2 components. These specific requirements cannot be satisfied using the default mechanisms in OSGi. 

WSO2 recently introduced a framework called WSO2 Microservices Framework for Java (MSF4J), to develop microservices. You can develop and run microservices in a standalone mode and in WSO2 Carbon-based products as OSGi bundles. If you are using Carbon-based products to host your microservices, then you need to register your microservices as OSGi services. Now, when the microservices engine receives a request, engine dispatches it to the correct microservice. Neverthless, microservices engine cannot accept requests until it knows that all the microservices are available as OSGi services.

Otherwise, during server startup, certain microservices will be available while other microservices are yet to be registered as OSGi services. You shouldn’t open ports until all the microservices are available. Microservices engine should notify when all the microservices are registered as OSGi services during the startup. Then only Transport Manager should open ports. All this should happen during the server startup. The Carbon startup order resolver component solves this problem in OSGi.

## About the Carbon startup order resolver

Startup order resolver component is available from WSO2 Carbon kernel 5.0.0 onwards, and its design handles startup ordering complexities in WSO2 Carbon-based products. This is a generic utility, which can resolve startup order of any other component such as microservice engine, Transport Manager etc. 

The startup order resolver component in Carbon ensures that a particular component is not started until all the other components required by that component are fully initialized. To achieve this, the components with dependencies are separated into two categories as shown below.

* **Startup listener components:** Components that need to hold its initialization, until all the required OSGi services or capabilities are available during server startup. For example, the Microservice engine needs to hold it’s initialization until all the microservices from OSGi bundles that are available during server startup are registered as OSGi services. 
* **OSGi service components:** Components that register OSGi services. For example, user management components expose it’s capabilities via a microservice. JMS transport module registers the JMS transport as OSGi services. 
  
### Defining a startup listener component

A component has to be defined as a listener component, if there are other components that should be initialized before the listener component is initialized. For example, in the above scenario the Transport Manager component should be defined as a listener component, because it requires other transport components to be initialized before it.

An OSGi listener component is defined as shown below.

1. The startup order resolver identifies a startup listener component from the following manifest header:

        Carbon-Component: startup.listener;
        componentName=”transport-mgt”;
        requiredService=”org.wso2.carbon.kernel.transports.CarbonTransport”;

 * `startup.listener`: Marks this component as a startup listener.
 
 * `componentName`: This is a unique name to identify the component. Each and every startup listener component should have a unique name.
 
 * `requiredService`: A comma separated list of OSGi service keys. These are the OSGi services that the listener component should wait for. That is, the startup listener component should hold it’s initialization until all the services of specified keys are available.

2. The startup order resolver notifies a startup listener component when all the required services are available. In order to get this notification, the startup listener component should register an OSGi service with the following interface: `org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener`.

 An implementation of the `RequiredCapabilityListener` interface is shown below.

      public class TransportStartupListener implements RequiredCapabilityListener {
    
      @Override
        public void onAllRequiredCapabilitiesAvailable() {
           // This method is invoked by the startup 
              order resolver when all the required 
               services are available
           }
           }

  Shown below is how you register the `RequiredCapabilityListener` implementation given above as an OSGi service in your `BundleActivator`.

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
        
 The value given for the componentName property should be equal to the `componentName` property value of the startup listener component that was defined earlier. This is how the startup order resolver maps the `RequiredCapabilityListener` with corresponding startup listener components (`startup.listener`).

### Defining an OSGi service component
A component is required to be defined as an OSGi service component when there are other components depending on the initialization of this component. For example, the Transport Manager component will only be started once the relevant transports are already initialized. Therefore, the transport implementation should be defined as OSGi service components.

#### **Registering a single OSGi service from a component**

The startup order resolver identifies an OSGi service component from the following manifest header:

    Carbon-Component: osgi.service; 
    objectClass=”org.wso2.carbon.kernel.transports.CarbonTransport”
    
 * `osgi.service`: This property marks this component as an OSGi service component.
 
 * `objectClass`: This property indicates the type of OSGi service that will be registered by the component. 

Please note that all components that register OSGi services do not need to include this manifest header. You need to include this header only if there are other components waiting for your OSGi service. For example, the Transport Manager component waits for all the transport OSGi services. If you are developing a transport, you need to put this header into your bundle’s `MANIFEST.MF`.

#### **Registering multiple OSGI services from a component**

When you define an OSGi service component, the component bundle may need to register more than one OSGi service of the same type. This can be done in two ways as explained below.

 * **Registering a known number of services:** You can use the serviceCount manifest attribute to specify the number of services that you register from your bundle as shown below. Here you know the exact number of services that your register at development time.

        Carbon-Component: osgi.service; 
        objectClass=”org.wso2.carbon.kernel.transports.CarbonTransport”; 
        serviceCount="4"

 * **Registering an indefinite number of services:** In certain scenarios, you may not know the number of OSGi services you register from your bundle at the time of development. However, during server startup, you can calculate the number of OSGi services that need to be registered. You may obtain this value from a configuration file, from another OSGi service or from any source. In this scenario, you cannot use the serviceCount manifest attribute, since you don’t know the count at development time.
 
In order to solve this problem, we have introduced an interface called `CapabilityProvider` from the `org.wso2.carbon.kernel.startupresolver` package. 

 1. You can register the OSGi service as shown below.

        public class HTTPTransportProvider implements CapabilityProvider {   
        @Override
        public int getCount() {
            return 4;
        }
        }

 2. Now you need to specify the `"Carbon-Component"` manifest header as shown below.

        Carbon-Component: osgi.service; 
        objectClass=”org.wso2.carbon.kernel.startupresolver.CapabilityProvider”;
        capabilityName="org.wso2.carbon.kernel.transports.CarbonTransport";

 3. Shown below is how you can register the `CapabilityProvider` implementation shown above as an OSGi service in your `BundleActivator`.

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

As explained above, the startup order resolver processes the `Carbon-Component` manifest headers, and figures out the components that need to be notified when all requirements are satisfied. Similarly, the startup order resolver figures out the expected number of OSGi services for each startup listener component. The startup order resolver listens to OSGi service events, and notifies startup listener components, as and when their requirements are satisfied.

# Adding New Transports
From Carbon 5.0.0 Kernel onwards, we are providing a pluggable interface to add new transports to the existing server. Following are the steps that need to be carried out when adding a new transport to the server.

* **[Adding a new transport to the Carbon server](#adding-a-new-transport-to-the-carbon-server)**
* **[Registering the transport in the Kernel startup order framework](#registering-the-transport-in-the-kernel-startup-order-framework)**
* **[Managing transports using OSGi console commands](#managing-transports-using-osgi-console-commands)**

## Adding a new transport to the Carbon server
Follow the steps given below to implement a new transport.

1. Implement the [`CarbonTransport`](https://github.com/wso2/carbon-kernel/blob/5.0.x/core/src/main/java/org/wso2/carbon/kernel/transports/CarbonTransport.java) abstract class with the following methods:

        protected abstract void start();
        protected abstract void stop();
        protected abstract void beginMaintenance();
        protected abstract void endMaintenance();

 Refer the `carbon-transport` project [`NettyListener`](https://github.com/wso2/carbon-transports/blob/v2.1.0/http/netty/components/org.wso2.carbon.transport.http.netty/src/main/java/org/wso2/carbon/transport/http/netty/internal/NettyTransportActivator.java#L47) implementation for more details and examples on how to extend the `CarbonTransport` and write your own transport implementation.
 
2. Register the implemented server as an OSGi service. For example, If you have extended the `CarbonTransport` class and implemented `JettyCarbonTransport`, you need to register the implemented Carbon Transport as follows:

        NettyListener nettyCarbonTransport = new NettyListener("netty");
        bundleContext.registerService(CarbonTransport.class.getName(), nettyCarbonTransport, null);

 Refer registration of [`NettyListener`](https://github.com/wso2/carbon-transports/blob/v2.1.0/http/netty/components/org.wso2.carbon.transport.http.netty/src/main/java/org/wso2/carbon/transport/http/netty/internal/NettyTransportActivator.java#L47). You have now registered your transport to the server.

## Registering the transport in the Kernel startup order framework
The Startup Order Resolver component in Kernel allows you to add transports and resolve them statically as well as dynamically. The Transport Manager component in Carbon will only be started once the relevant transports are already initialized. Therefore, the transport implementation should be defined as OSGi service components. Note that your transport can be registered as a single OSGi service or as multiple services. See the instructions on [resolving the component startup order](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#resolving-the-component-startup-order).

## Managing transports using OSGi console commands
After registering the new transport, the transports can be managed by the osgi command line. Use ‘help’ to list all the commands available. Following commands are available for the purpose of transport management.

    --Transport Management---
     startTransport <transportName> - Start the specified transport with <transportName>.
     stopTransport <transportName> - Stop the specified transport with <transportName>
     startTransports - Start all transports
     stopTransports - Stop all transports
     beginMaintenance - Activate maintenance mode of all transports
     endMaintenance - Deactivate maintenance mode of all transports
     listTransports - List all the available transports
     Example: startTransport jetty

# Plugging a New Runtime
From Carbon 5.0.0 Kernel onwards, Carbon provides a pluggable interface to add runtimes to the existing server. Following are the instructions that you need to follow when adding a new runtime.

* **[Adding a New Runtime](#adding-a-new-runtime)**
* **[Testing your New Runtime](#testing-your-new-runtime)**

## Adding a New Runtime
The following example illustrates how you can plug your own runtime and register it with the Carbon runtime framework. In this example, we will run through the steps for plugging the Tomcat runtime (which is currently available with the Carbon 4.2.0 release) to Carbon 5.0.0.

1. Create a simple maven project with the following dependencies. 

        <dependency>
        <groupId>org.wso2.carbon</groupId>
        <artifactId>org.wso2.carbon.core</artifactId>
        <version>5.0.0</version>
        </dependency>

 You may also need to add other dependencies according to the requirement.

2. Implement the runtime interface. See the example given below.

 This code sample contains the implementation that was done for Carbon 4.2.0 components. In this code segment, the current Tomcat integration is re-factored to Carbon at [`http://svn.wso2.org/repos/wso2/carbon/kernel/branches/4.2.0/core/org.wso2.carbon.tomcat/4.2.0/`](http://svn.wso2.org/repos/wso2/carbon/kernel/branches/4.2.0/core/org.wso2.carbon.tomcat/4.2.0/).

        public class TomcatRuntime implements Runtime {
        private static Log log = LogFactory.getLog(TomcatRuntime.class);
        private static CarbonTomcatService carbonTomcatService;
        private InputStream inputStream;
        static ClassLoader bundleCtxtClassLoader;
        private RuntimeState state = RuntimeState.PENDING;

        /**
         * initialization code goes here.i.e : configuring TomcatService instance using catalina-server.xml
        */
        @Override
        public void init() {
        bundleCtxtClassLoader = Thread.currentThread().getContextClassLoader();
        String carbonHome = System.getProperty("carbon.home");
        String catalinaHome = new File(carbonHome).getAbsolutePath() + File.separator + "lib" +
                File.separator + "tomcat";
        String catalinaXML = new File(carbonHome).getAbsolutePath() + File.separator +
                "repository" + File.separator + "conf" + File.separator +
                "tomcat" + File.separator + "catalina-server.xml";
        try {
            inputStream = new FileInputStream(new File(catalinaXML));
        } catch (FileNotFoundException e) {
            log.error("could not locate the file catalina-server.xml", e);
        }
        //setting catalina.base system property. carbonTomcatService configurator refers this property while carbonTomcatService instance creation.
        //you can override the property in wso2server.sh
        if (System.getProperty("catalina.base") == null) {
            System.setProperty("catalina.base", System.getProperty("carbon.home") + File.separator +
                    "lib" + File.separator + "tomcat");
        }
        carbonTomcatService = new CarbonTomcatService();
        carbonTomcatService.configure(catalinaHome, inputStream);
        state = RuntimeState.INACTIVE;
         }

         /**
        * starting the a carbonTomcatService instance in a new thread. Otherwise activator gets blocked.
        */
         @Override
        public synchronized void start() {
        new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setContextClassLoader(bundleCtxtClassLoader);
                try {
                    carbonTomcatService.start();
                    state = RuntimeState.ACTIVE;
                } catch (LifecycleException e) {
                    log.error("carbonTomcatService life-cycle exception", e);
                }
            }
        }).start();
        }

         /**
         * stopping the carbonTomcatService instance
        */
                @Override
                public void stop() {
                try {
                 carbonTomcatService.stop();
                 state = RuntimeState.INACTIVE;
                 } catch (LifecycleException e) {
                log.error("Error while stopping carbonTomcatService", e);
                }
                }

                 @Override
                public void startMaintenance() {
                // work specific to startMaintenance
                state = RuntimeState.MAINTENANCE;
                 }
                @Override
                public void stopMaintenance() {
                // work specific to stopMaintenance
                state = RuntimeState.INACTIVE;
                }

                @Override
                public Enum<RuntimeState> getState() {
                return state;
                }

                @Override
                public void setState(RuntimeState runtimeState) {
                this.state = runtimeState;
                }

                /**
                * we are not expecting others to access this service. The only use case would be activator.
                * hence package private access modifier
                *
                * @return
                */
                CarbonTomcatService getTomcatInstance() {
                return carbonTomcatService;
                }
                }

3. Write a bundle activator for the above runtime as shown below.

        public class TomcatRuntimeActivator implements BundleActivator {
        private static Log log = LogFactory.getLog(TomcatRuntimeActivator.class);
        private TomcatRuntime tomcatRuntime;
        private ServiceRegistration serviceRegistration;
        private ServiceListener serviceListener;

        public void start(BundleContext bundleContext) throws Exception {
        try {
            this.tomcatRuntime = new TomcatRuntime();
            tomcatRuntime.init();
            //  we'll start the server once our transports get up
            //  this is done by startup finalizer it will start all the runtimes
            //  runtime will only serve request once the runtime is start()
            serviceRegistration = bundleContext.registerService(Runtime.class.getName(), tomcatRuntime, null);
            serviceRegistration = bundleContext.registerService(TomcatService.class.getName(), tomcatRuntime.getTomcatInstance(), null);
            if (log.isDebugEnabled()) {
                log.debug("Registering the JNDI stream handler...");
            }
            //registering JNDI stream handler
            Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
            properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[]{"jndi"});
            bundleContext.registerService(URLStreamHandlerService.class.getName(), new JNDIURLStreamHandlerService(), properties);
        } catch (Throwable t) {
            log.fatal("Error while starting Tomcat " + t.getMessage(), t);
            //do not throw because framework will keep trying. catching throwable is a bad thing, but
            //looks like we have no other option.
        }
        }

        public void stop(BundleContext bundleContext) throws Exception {
        this.tomcatRuntime.stop();
        serviceRegistration.unregister();
        tomcatRuntime = null;
        }
        }

4. Once the above points are addressed in your project, we need to add the Maven bundle plugin properties to generate the `component-level` metadata for `scr` annotations and bundle info. An example of the POM file is given below. The packaging should be 'bundle' in the `pom.xml`.

        <properties>
        <bundle.activator>org.wso2.carbon.tomcat.internal.TomcatRuntimeActivator</bundle.activator>
        <private.package>
            org.wso2.carbon.tomcat.internal.*, 
            org.wso2.carbon.tomcat.jndi.* 
        </private.package>
        <export.package>
                !org.wso2.carbon.tomcat.internal.*,
                org.wso2.carbon.tomcat.*,
                org.wso2.carbon.tomcat.api.*,
                org.wso2.carbon.tomcat.server.*; version="${project.version}"
                </export.package>
                <import.package>
                org.apache.tomcat.*;version="[1.7.0,2.0.0)",
                org.apache.catalina.*;version="[1.7.0,2.0.0)",
                org.apache.naming.*;version="[1.7.0,2.0.0)",
                *;resolution:=optional
                </import.package>
                </properties>

## Testing your New Runtime

You can test the new runtime by following the steps given below.

1. Build your component, which will generate an OSGi bundle.
2. Now this bundle can be installed in a running Carbon server instance by adding the bundle to the dropins directory in the OSGi repository (which is `<CARBON_HOME>/osgi`). Once you have added your bundle to the dropins directory, it will be installed to the OSGi runtime by the Kernel launcher. 
 
 > Find out more about how the [dropins directory is used for deploying bundles](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/tools#dropins-support-for-osgi-bundles).

# Using the CarbonContext API

The `CarbonContext` API is used for the purpose of storing and retrieving data that is thread local. This API implements the two classes named `CarbonContext` and `PrivilegedCarbonContext`.

* **[CarbonContext](#carboncontext)**
* **[PrivilegedCarbonContext](#privilegedcarboncontext)**

## CarbonContext

This is the `ReadOnly` API, which is basically the user-level API. Shown below is a sample use case of the `CarbonContext` API.

    CarbonContext carbonContext = CarbonContext.getCurrentContext();
    String tenant = carbonContext.getTenant();
    Principal principal = carbonContext.getUserPrincipal();
    Object propertyValue = carbonContext.getProperty("PROPERTY_KEY");

As shown above, the `CarbonContext` class is used to get the following information:

* The name of the tenant dedicated for the server.
  
  > **Retrieving tenant information:**
  Note that from Carbon 5.0.0 onwards, a server is dedicated to one tenant. Therefore, we do not have a separate API for setting the tenant name. The tenant name will be taken from the `carbon.yml` file or it can be set as a system/environment variable. 
  
* The `User Principal` value, which is the JAAS principal for authorization that is applicable to the currently logged in user. 
* The properties that help you set values that can be used later in the thread flow.

## PrivilegedCarbonContext

This is the `ReadWrite` API, which is secured using java security permission. This is the final class extending from the `CarbonContext` API. Shown below is a sample use case of the `PrivilegedCarbonContext` API.

    PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
    privilegedCarbonContext.setUserPrincipal(userPrincipal);
    privilegedCarbonContext.setProperty("PROPERTY_KEY", propertyValue);

As shown above, the `PrivilegedCarbonContext` class is used to set the following information:
* The User Principal value.
* Property values.

# Developing a Carbon Tool

See the topics given below for information on Carbon tools.

* **[About Carbon Tools](#about-carbon-tools)**
* **[Creating a Carbon tool](#creating-a-carbon-tool)**

## About Carbon Tools

Carbon tools provide you the option of using various features as standalone functionalities that are detached from the server startup process. That is, you will be able to use these functions easily by executing a simple tool. These tools can be executed at any point, irrespective of whether the server is started.  

Carbon tools provide the following benefits:

* Improve the server startup speed by detaching the function from the server startup process. 
* Allows you to execute the tools as standalone functions without running the Carbon server. 
* Allows you to extend Carbon tools easily by introducing custom tool(s) in addition to the tools available by default.

Given below are the optional tools that are available by default with the Carbon Kernel distribution. You can find more details on how to use these tools by following the given links.

* [Java Archive (JAR) file to OSGi bundle converter](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/tools#converting-jars-to-osgi-bundles)
* [Dropins deployer tool](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/tools#dropins-support-for-osgi-bundles)
 
## Creating a Carbon Tool

> The Java Archive (JAR) file that contains these tool implementations is `org.wso2.carbon.tools*.jar`. This JAR is stored in the `<CARBON_HOME>/bin/bootstrap/tools` directory. 

Given below are the steps for developing a sample custom tool.

1. You must implement the [`org.wso2.carbon.tools.CarbonToo`](https://github.com/wso2/carbon-kernel/blob/master/tools/tools-core/src/main/java/org/wso2/carbon/tools/CarbonTool.java) Java interface in order to develop your custom tool. Given below is an example that implements a tool named `CustomTool`.

		public class CustomTool implements CarbonTool {
  
   		/**
   		 * Executes the WSO2 Custom Tool based on the specified arguments.
    		*
    		* @param toolArgs the arguments required for the tool execution
    		*/
   		@Override
   		public void execute(String... toolArgs) {
		// the tool execution implementation goes here
    		}
		}

2. To make sure that the custom tool is executable, you need to initialize it within the executeTool(String toolIdentifier, String... toolArgs) method of the [`org.wso2.carbon.tools.CarbonToolExecutor`](https://github.com/wso2/carbon-kernel/blob/master/tools/tools-core/src/main/java/org/wso2/carbon/tools/CarbonToolExecutor.java) Java class. The tool identifier in this example is `custom-tool`, which is indicated using the case property.

		case “custom-tool”:
		carbonTool = new CustomTool();
		break;
		
3. Create a shell script or a batch file that executes the `org.wso2.carbon.tools*.jar` file that is stored in the `<CARBON_HOME>/bin/bootstrap/tools` directory. Make sure that the `wso2.carbon.tool` system property is set to the `custom-tool` identifier (which corresponds to the case value you specified in step 2).

		-Dwso2.carbon.tool="custom-tool"

# Configuring Logging for a Carbon Server

See the following topics for details on logging related configurations in Carbon 5.x.x.

* **[Configuring the Logging Framework](#configuring-the-logging-framework)**
 * **[Configuring a logging API for your bundle](#configuring-a-logging-api-for-your-bundle)**
 * **[Configuring the Carbon logging level](#configuring-the-carbon-logging-level)**
* **[Enabling Asynchronous Logging](#enabling-asynchronous-logging)**

## Configuring the Logging Framework

The Logging framework in Carbon Kernel 5.1.0 ( WSO2 Carbon 5.x.x platform) is implemented using [PaxLogging](https://ops4j1.jira.com/wiki/display/paxlogging/Pax+Logging), which is used as the underlying logging library. High performing log4j 2.0 is used as the logging backend with this framework. The Carbon Logging framework supports a number of logging APIs and this allows users to use any logging API in the components that they develop.

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
Most of the configuration related `log4j2` are available at the official documentation: https://logging.apache.org/log4j/2.x/manual/configuration.html

## Enabling Asynchronous Logging
WSO2 Carbon 5.x.x Kernel uses logging framework with Pax Logging. You can find out more about this logging framework and the default configurations from here. You can follow the steps given below if you want to enable [asynchronous logging](https://logging.apache.org/log4j/2.x/manual/async.html) for your Carbon 5.x.x-based server.

1. Download the disrupter OSGi bundle from [here](http://mvnrepository.com/artifact/com.lmax/disruptor/3.2.0) and copy it to the `<CARBON_HOME>/osgi/plugins` directory.
2. Open the `launch.properties` file from the `<CARBON_HOME>/conf/osgi` folder and add the disrupter JAR to the initial bundles list as shown below.

        carbon.initial.osgi.bundles=\file\:plugins/disruptor-3.2.0.jar@2\:true,
	
 > The [Carbon Launcher](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/launcher/README.md) component will initialize the bundles listed below when the server is started.

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

# Monitoring Carbon Servers

You can monitor the Carbon server using the following options:

* **[Using Audit Logs](#using-audit-logs)**
* **[Using MBeans for Monitoring](#using-mbeans-for-monitoring)**

> In addition to these monitoring capabilities, you can monitor server startup logs from the Carbon Launcher. Find out more from the [README.MD file of the Carbon Launcher](https://github.com/nilminiwso2/carbon-kernel-1/blob/master/launcher/README.md#monitoring-server-startup-logs).

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

 > Note that the "user-name" key will be effective when the `javax.security.Principal` user gets set in the `PrivilegedCarbonContext`. Find more information about [using the `CarbonContext` API](https://github.com/nilminiwso2/carbon-kernel-1/tree/master/core#using-the-carboncontext-api).

Prior to Carbon 5.x.x, it was necessary for developers to manually capture any required contextual information (such as the logged in user details, remote IP address of client etc.) at every logging instance. These contextual details then had to be manually added to the audit logs. With the new approach introduced in WSO2 Carbon 5.1.0, [SLF4J Mapped Diagnostic Context (MDC)](http://www.slf4j.org/manual.html#mdc) will capture the contextual information when you specify the relevant keys as shown in the above example. Therefore, there is no need to capture the contextual information for every logging instance because the values added to the MDC in the first instance will remain within the thread.

### Viewing audit logs

Carbon users and component developers can view local audit logs using the `<CARBON_HOME>/logs/audit.log` file. This file can be configured as a daily, rolling log file.

## Using MBeans for Monitoring
Java Management Extensions (JMX) is a standard technology in the Java platform. It provides a simple mechanism for managing resources such as applications, devices, and services. The Carbon 5.1.0 Kernel leverages the features of the existing JMX Platform MBean Server by providing Carbon 5.1.0-based authentication for additional security and by making JMX resources available for remote access. Components inherit the platform MBean server from the Carbon 5.1.0 Kernel. Therefore, you can simply register the resources of your Carbon component in this MBean server and expose them to remote users.

> Note that `javax.management.*` is the only required dependency.

See the following topics for instructions.

### About the JMX monitoring implementation in WSO2 Carbon

The `CarbonJMXComponent` implementation in Carbon 5.1.0 Kernel uses the existing JMX platform MBean server in Carbon and exposes the registered MBeans to remote users via the `JMXConnectorServer` implementation. In this process, user authentication is performed by the `CarbonJMXAuthenticator` implementation. A user can modify the service configuration in the `jmx.yaml` file, located in the `<CARBON_HOME>/conf` directory. The connection URL that exposes the MBeans is as follows: `service:jmx:rmi://localhost:9700/jndi/rmi://localhost:9800/jmxrmi`.

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
