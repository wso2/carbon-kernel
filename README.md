# Welcome to WSO2 Carbon Kernel 5.2.0 Milestone 3
WSO2 Carbon Kernel 5.2.0 is the core of the next-generation WSO2 Carbon platform. 

It is completely rearchitected Carbon Kernel from the ground up with the latest technologies and patterns. Additionally, the Carbon Kernel is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

* **[Architecture](#architecture)**
* **[Key Features and Tools](#key-features-and-tools)**
* **[Getting Started](#getting-started)**
* **[How To Contribute](#how-to-contribute)**
* **[Contact Us](#contact-us)**

## Architecture
Carbon Kernel is a modular, light-weight, OSGi-based server development framework, which provides the base for developing servers. Eclipse Equinox is used as the OSGi runtime from Kernel 5.0.0 onwards. However, you can plug in any OSGi implementation to your Carbon server. The diagram below depicts the architecture of WSO2 Carbon Kernel and its key components.

![carbon-kernel-architecture-01](https://cloud.githubusercontent.com/assets/21237558/20616347/939893b6-b307-11e6-882f-4c3f302ada0c.png)

## Key Features and Tools
Follow the links given below for details of the core capabilities of Carbon Kernel.

* [Resolving the component startup order](docs/KernelFeatures/ResolvingtheComponentStartupOrder.md)
* [Adding new transports](docs/KernelFeatures/AddingNewTransports.md)
* [Plugging a new runtime](docs/KernelFeatures/PluggingaNewRuntime.md)
* [Using the CarbonContext API](docs/KernelFeatures/UsingtheCarbonContext.md)
* [Developing a Carbon tool](docs/KernelFeatures/DevelopingaCarbonTool.md)
* [Configuring Logging for a Carbon Server](docs/KernelFeatures/ConfiguringLogging.md)
* [Monitoring Carbon Servers](docs/KernelFeatures/MonitoringCarbonServers.md)
* [Setting up the Carbon Launcher](docs/KernelFeatures/SettingUptheCarbonLauncher.md)
* [Using in-container OSGi testing for development](docs/DeveloperTools/InContainerOSGiTesting.md)

Follow the links given below for details of tools and archetypes that can be used for developing Carbon products.

* [Creating a Carbon component in one step using Maven archetypes](docs/DeveloperTools/UsingMavenArchetypes.md#creating-a-carbon-component-in-one-step)
* [Creating a generic OSGi bundle in one step using Maven archetypes](docs/DeveloperTools/UsingMavenArchetypes.md#creating-a-generic-osgi-bundle-in-one-step)
* [Converting JARs to OSGi bundles](docs/DeveloperTools/ConvertingJARsToOSGiBundles.md)
* [Dropping OSGi Bundles into a Carbon Server](docs/KernelFeatures/DroppingOSGiBundlesintoaCarbonServer.md)

Follow the links given below for details of plugins:

* [Using the Carbon Feature Plugin](docs/DeveloperTools/UsingtheCarbonFeaturePlugin.md)
* [Using the Maven Bundle Plugin](docs/DeveloperTools/UsingtheMavenBundlePlugin.md)

Follow the links given below for reference information.

* [Using Annotations with OSGi Declarative Services](docs/DeveloperTools/UsingAnnotationswithOSGiDeclarativeServices.md)

## Getting Started
See the steps for [setting up and starting a WSO2 Carbon server](docs/GettingStarted.md).

## How To Contribute
* Please report issues at [WSO2 JIRA](https://wso2.org/jira/browse/Carbon).
* Send your pull requests to [master branch](https://github.com/wso2/carbon-kernel/tree/master).
* You can find more instructions on howto contribute on community site (http://wso2.com/community).

## Contact Us
WSO2 developers can be contacted via the mailing lists:
* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
