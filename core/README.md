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
