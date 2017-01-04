# Adding New Transports
> The process of adding new transports to a WSO2 product is explained below. For the full list of capabilities available in this kernel version, see the **features** section in the [root README.md file](../../README.md#key-features-and-tools). 

From Carbon 5 Kernel onwards, we are providing a pluggable interface to add new transports to the existing server. Following are the steps that need to be carried out when adding a new transport to the server.

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

 Refer the `carbon-transport` project's [`NettyListener`](https://github.com/wso2/carbon-transports/blob/v2.1.0/http/netty/components/org.wso2.carbon.transport.http.netty/src/main/java/org/wso2/carbon/transport/http/netty/internal/NettyTransportActivator.java#L47) implementation for more details. You can also find examples on how to extend the `CarbonTransport` and on how to write your own transport implementation.
 
2. Register the implemented server as an OSGi service. For example, If you have extended the `CarbonTransport` class and implemented `JettyCarbonTransport`, you need to register the implemented Carbon Transport as follows:

        NettyListener nettyCarbonTransport = new NettyListener("netty");
        bundleContext.registerService(CarbonTransport.class.getName(), nettyCarbonTransport, null);

 Refer registration of [`NettyListener`](https://github.com/wso2/carbon-transports/blob/v2.1.0/http/netty/components/org.wso2.carbon.transport.http.netty/src/main/java/org/wso2/carbon/transport/http/netty/internal/NettyTransportActivator.java#L47). You have now registered your transport to the server.

## Registering the transport in the Kernel startup order framework
The Startup Order Resolver component in Kernel allows you to add transports and resolve them statically as well as dynamically. The Transport Manager component in Carbon will only be started once the relevant transports are already initialized. Therefore, the transport implementation should be defined as OSGi service components. Note that your transport can be registered as a single OSGi service or as multiple services. See the instructions on [resolving the component startup order](ResolvingtheComponentStartupOrder.md).

## Managing transports using OSGi console commands
After registering the new transport, the transports can be managed by the osgi command line. Use ‘help’ to list all the commands available. The following commands are available for the purpose of transport management.

    --Transport Management---
     startTransport <transportName> - Start the specified transport with <transportName>.
     stopTransport <transportName> - Stop the specified transport with <transportName>
     startTransports - Start all transports
     stopTransports - Stop all transports
     beginMaintenance - Activate maintenance mode of all transports
     endMaintenance - Deactivate maintenance mode of all transports
     listTransports - List all the available transports
     Example: startTransport jetty
