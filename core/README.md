* **[Resolving the Component Startup Order](#resolving-the-component-startup-order)**
* **[Adding New Transports](#adding-new-transports)**
* **[Plugging a New Runtime](#plugging-a-new-runtime)**
* **[Using the CarbonContext API](#using-the-carboncontext-api)**

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

  *Startup listener components:* Components that need to hold its initialization, until all the required OSGi services or capabilities are available during server startup. For example, the Microservice engine needs to hold it’s initialization until all the microservices from OSGi bundles that are available during server startup are registered as OSGi services. 
  
  *OSGi service components:* Components that register OSGi services. For example, user management components expose it’s capabilities via a microservice. JMS transport module registers the JMS transport as OSGi services. 
  
#### *Defining a startup listener component*

A component has to be defined as a listener component, if there are other components that should be initialized before the listener component is initialized. For example, in the above scenario the Transport Manager component should be defined as a listener component, because it requires other transport components to be initialized before it.

An OSGi listener component is defined as shown below.

1. The startup order resolver identifies a startup listener component from the following manifest header:

        Carbon-Component: startup.listener;
        componentName=”transport-mgt”;
        requiredService=”org.wso2.carbon.kernel.transports.CarbonTransport”;

 *startup.listener:* Marks this component as a startup listener.
 
 *componentName:* This is a unique name to identify the component. Each and every startup listener component should have a unique name.
 
 *requiredService:* A comma separated list of OSGi service keys. These are the OSGi services that the listener component should wait for. That is, the startup listener component should hold it’s initialization until all the services of specified keys are available.

2. The startup order resolver notifies a startup listener component when all the required services are available. In order to get this notification, the startup listener component should register an OSGi service with the following interface: org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener.

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

#### *Defining an OSGi service component*
A component is required to be defined as an OSGi service component when there are other components depending on the initialization of this component. For example, the Transport Manager component will only be started once the relevant transports are already initialized. Therefore, the transport implementation should be defined as OSGi service components.

*Registering a single OSGi service from a component*

The startup order resolver identifies an OSGi service component from the following manifest header:

    Carbon-Component: osgi.service; 
    objectClass=”org.wso2.carbon.kernel.transports.CarbonTransport”
    
 *osgi.service:* This property marks this component as an OSGi service component.
 
 *objectClass:* This property indicates the type of OSGi service that will be registered by the component. 

Please note that all components that register OSGi services do not need to include this manifest header. You need to include this header only if there are other components waiting for your OSGi service. For example, the Transport Manager component waits for all the transport OSGi services. If you are developing a transport, you need to put this header into your bundle’s MANIFEST.MF.

*Registering multiple OSGI services from a component*

When you define an OSGi service component, the component bundle may need to register more than one OSGi service of the same type. This can be done in two ways as explained below.

 *Registering a known number of services:* You can use the serviceCount manifest attribute to specify the number of services that you register from your bundle as shown below. Here you know the exact number of services that your register at development time.

      Carbon-Component: osgi.service; 
      objectClass=”org.wso2.carbon.kernel.transports.CarbonTransport”; 
      serviceCount="4"

 *Registering an indefinite number of services:* In certain scenarios, you may not know the number of OSGi services you register from your bundle at the time of development. However, during server startup, you can calculate the number of OSGi services that need to be registered. You may obtain this value from a configuration file, from another OSGi service or from any source. In this scenario, you cannot use the serviceCount manifest attribute, since you don’t know the count at development time.
 
In order to solve this problem, we have introduced an interface called CapabilityProvider from the org.wso2.carbon.kernel.startupresolver package. 

 1. You can register the OSGi service as shown below.

        public class HTTPTransportProvider implements CapabilityProvider {   
        @Override
        public int getCount() {
            return 4;
        }
        }

 2. Now you need to specify the "Carbon-Component" manifest header as shown below.

        Carbon-Component: osgi.service; 
        objectClass=”org.wso2.carbon.kernel.startupresolver.CapabilityProvider”;
        capabilityName="org.wso2.carbon.kernel.transports.CarbonTransport";

 3. Shown below is how you can register the CapabilityProvider implementation shown above as an OSGi service in your BundleActivator.

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

# Adding New Transports
From Carbon 5.0.0 Kernel onwards, we are providing a pluggable interface to add new transports to the existing server. Following are the steps that need to be carried out when adding a new transport to the server.

### Adding a new transport to the Carbon server
Follow the steps given below to implement a new transport.

1. Implement the CarbonTransport abstract class with the following methods:

        protected abstract void start();
        protected abstract void stop();
        protected abstract void beginMaintenance();
        protected abstract void endMaintenance();

 Refer the carbon-transport project NettyListener implementation for more details and examples on how to extend the CarbonTransport and 

2. write your own transport implementation.
Register the implemented server as an OSGi service. For example, If you have extended the CarbonTransport class and implemented JettyCarbonTransport, you need to register the implemented Carbon Transport as follows:

        NettyListener nettyCarbonTransport = new NettyListener("netty");
        bundleContext.registerService(CarbonTransport.class.getName(), nettyCarbonTransport, null);

 Refer registration of NettyListener. You have now registered your transport to the server.

### Registering the transport in the Kernel startup order framework
The Startup Order Resolver component in Kernel allows you to add transports and resolve them statically as well as dynamically. The Transport Manager component in Carbon will only be started once the relevant transports are already initialized. Therefore, the transport implementation should be defined as OSGi service components. Note that your transport can be registered as a single OSGi service or as multiple services. See the instructions on resolving the component startup order.

### Managing transports using OSGi console commands
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

### Adding a New Runtime
The following example illustrates how you can plug your own runtime and register it with the Carbon runtime framework. In this example, we will run through the steps for plugging the Tomcat runtime (which is currently available with the Carbon 4.2.0 release) to Carbon 5.0.0.

1. Create a simple maven project with the following dependencies. 

        <dependency>
        <groupId>org.wso2.carbon</groupId>
        <artifactId>org.wso2.carbon.core</artifactId>
        <version>5.0.0</version>
        </dependency>

 You may also need to add other dependencies according to the requirement.

2. Implement the runtime interface. See the example given below.

 This code sample contains the implementation that was done for Carbon 4.2.0 components. In this code segment, the current Tomcat integration is re-factored to Carbon at http://svn.wso2.org/repos/wso2/carbon/kernel/branches/4.2.0/core/org.wso2.carbon.tomcat/4.2.0/.

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

4. Once the above points are addressed in your project, we need to add the Maven bundle plugin properties to generate the component-level metadata for scr annotations and bundle info. An example of the POM file is given below. The packaging should be 'bundle' in the pom.xml.

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

### Testing your New Runtime

You can test the new runtime by following the steps given below.

1. Build your component, which will generate an OSGi bundle.
2. Now this bundle can be installed in a running Carbon server instance by adding the bundle to the dropins directory in the OSGi repository (which is <CARBON_HOME>/osgi). Once you have added your bundle to the dropins directory, it will be installed to the OSGi runtime by the Kernel launcher. Find out more about how the dropins directory is used for deploying bundles.

# Using the CarbonContext API

The CarbonContext API is used for the purpose of storing and retrieving data that is thread local. This API implements the two classes named CarbonContext and PrivilegedCarbonContext.

## CarbonContext

This is the ReadOnly API, which is basically the user-level API. Shown below is a sample use case of the CarbonContext API.

    CarbonContext carbonContext = CarbonContext.getCurrentContext();
    String tenant = carbonContext.getTenant();
    Principal principal = carbonContext.getUserPrincipal();
    Object propertyValue = carbonContext.getProperty("PROPERTY_KEY");

As shown above, the CarbonContext class is used to get the following information:
The name of the tenant dedicated for the server.
Retrieving tenant information:
Note that from Carbon 5.0.0 onwards, a server is dedicated to one tenant. Therefore, we do not have a separate API for setting the tenant name. The tenant name will be taken from the carbon.yml file or it can be set as a system/environment variable. 

The User Principal value, which is the JAAS principal for authorization that is applicable to the currently logged in user. 
The properties that help you set values that can be used later in the thread flow.

## PrivilegedCarbonContext

This is the ReadWrite API, which is secured using java security permission. This is the final class extending from the CarbonContext API. Shown below is a sample use case of the PrivilegedCarbonContext API.

    PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getCurrentContext();
    privilegedCarbonContext.setUserPrincipal(userPrincipal);
    privilegedCarbonContext.setProperty("PROPERTY_KEY", propertyValue);

As shown above, the PrivilegedCarbonContext class is used to set the following information:

 The User Principal value.

 Property values.
