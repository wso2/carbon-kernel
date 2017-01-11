# Welcome to WSO2 Carbon Kernel
WSO2 Carbon Kernel 5 is the core of the next-generation WSO2 Carbon platform. 

Carbon Kernel 5 is completely rearchitected from the ground up with the latest technologies and patterns. Additionally, the Carbon Kernel is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

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

* [Resolving the Component Startup Order](docs/KernelFeatures/ResolvingtheComponentStartupOrder.md)
* [Adding New Transports](docs/KernelFeatures/AddingNewTransports.md)
* [Plugging a New Runtime](docs/KernelFeatures/PluggingaNewRuntime.md)
* [Using the CarbonContext API](docs/KernelFeatures/UsingtheCarbonContext.md)
* [Developing a Carbon tool](docs/KernelFeatures/DevelopingaCarbonTool.md)
* [Configuring Logging for a Carbon Server](docs/KernelFeatures/ConfiguringLogging.md)
* [Monitoring Carbon Servers](docs/KernelFeatures/MonitoringCarbonServers.md)
* [Setting Up the Carbon Launcher](docs/KernelFeatures/SettingUptheCarbonLauncher.md)
* [Dropping OSGi Bundles into a Carbon Server](docs/KernelFeatures/DroppingOSGiBundlesintoaCarbonServer.md)
* [Using Secure Vault](docs/KernelFeatures/UsingSecureVault.md)

Follow the links given below for details of tools, archetypes and other capabilities that can be used for developing Carbon products.

* [Creating a Carbon Component in One Step using Maven Archetypes](docs/DeveloperTools/UsingMavenArchetypes.md#creating-a-carbon-component-in-one-step)
* [Creating a Generic OSGi Bundle in One Step using Maven Archetypes](docs/DeveloperTools/UsingMavenArchetypes.md#creating-a-generic-osgi-bundle-in-one-step)
* [Converting JARs to OSGi bundles](docs/DeveloperTools/ConvertingJARsToOSGiBundles.md)
* [Using In-Container OSGi Testing for Development](docs/DeveloperTools/UsingIn-ContainerOSGiTesting.md)
* [Using Annotations with OSGi Declarative Services](docs/DeveloperTools/UsingAnnotationswithOSGiDeclarativeServices.md)
* [Setting up a Git Repository](docs/DeveloperTools/SettingUpaGitRepository.md)
* [Accessing the Carbon Configurations](docs/DeveloperTools/AccessingCarbonConfigs.md)
* [Using the Global Configuration Model](docs/DeveloperTools/UpdatingConfigurations.md) 
* [Designing the Product Directory Structure](docs/DeveloperTools/DesigningProductDirectoryStructure.md) 

Follow the links given below for details of plugins:

* [Using the Maven Bundle Plugin](docs/DeveloperTools/UsingtheMavenBundlePlugin.md)
* [Using Carbon Touchpoint](docs/DeveloperTools/UsingCarbonTouchpoint.md)

## Getting Started
See the steps for [setting up and starting a WSO2 Carbon server](docs/GettingStarted.md).

## How To Contribute
* Please report issues at [WSO2 Carbon Issues](https://github.com/wso2/carbon-kernel/issues).
* Send your pull requests to [master branch](https://github.com/wso2/carbon-kernel/tree/master).
* You can find more instructions on howto contribute on community site (http://wso2.com/community).

## Contact Us
WSO2 developers can be contacted via the mailing lists:
* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
