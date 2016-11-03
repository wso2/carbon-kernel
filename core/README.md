
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
  
#### Test
