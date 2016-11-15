# Welcome to WSO2 Carbon Kernel
WSO2 Carbon Kernel 5.2.0 is the core of the next-generation WSO2 Carbon platform. 

It is completely rearchitected Carbon Kernel from the ground up with the latest technologies and patterns. Additionally, the Carbon Kernel is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

# Architecture
Carbon Kernel is a modular, light-weight, OSGi-based server development framework, which provides the base for developing servers. Eclipse Equinox is used as the OSGi runtime from Kernel 5.0.0 onwards. However, you can plug in any OSGi implementation to your Carbon server. The diagram below depicts the architecture of WSO2 Carbon Kernel and its key components.
.......

# Key Features

Given below are details of the core capabilities of Carbon Kernel.

* Resolving the component startup order
* Adding new transports
* Plugging a new runtime
* Using the CarbonContext API
* Developing a Carbon tool
* Configuring Logging for a Carbon Server
* Monitoring Carbon Servers

* Setting up the Carbon Launcher
* Using in-container OSGi testing for development

Given below are details of tools and archetypes that can be used for developing Carbon products.

* Creating a Carbon component in one step using Maven archetypes
* Creating a generic OSGi bundle in one step using Maven archetypes
* Converting JARs to OSGi bundles
* Using the dropins support for OSGi bundles

# Getting Started
### Downloading the Kernel
You can download the product distribution from here.

### Installing the Kernel
#### Installation prerequisites
Prior to installing any WSO2 Carbon-based product, it is necessary to have the appropriate prerequisite software installed on your system. Verify that the computer has the supported operating system and development platforms before starting the installation.

*System requirements*
 Memory 
~ 256 MB minimum
 Disk 
 ~ 50 MB, excluding space allocated for log files. 
 
*Environment compatibility*
 Operating Systems / Databases 
 
WSO2 Carbon Kernel can be run on Windows / Linux and MacOS platforms that are Oracle/Open JDK 1.8.* compliant.      

*Supporting applications*

The following applications are required for running the product and its samples or for building from the source code. Mandatory installs are marked with an asterisk (*).
Application
Purpose
Version
Download Links
Oracle Java SE Development Kit (JDK)*
To launch the product, as each product is a Java application.
To build the product from the source distribution (both JDK and Apache Maven are required).
1.8.*
If you want to build the product from the source distribution, you must use JDK 1.8.*.
http://www.oracle.com/technetwork/java/javase/downloads/index.html
Apache Maven
To build the product from the source distribution (both JDK and Apache Maven are required). If you are installing the product by downloading and extracting the binary distribution instead of building from the source code, you do not need to install Maven.
To build samples.
3.3.x
http://maven.apache.org/

#### Installing the server

### Launching the Kernel
...

# How To Contribute
* Please report issues at [WSO2 JIRA](https://wso2.org/jira/browse/Carbon).
* Send your pull requests to [master branch](https://github.com/wso2/carbon-kernel/tree/master).
* You can find more instructions on how to contribute on community site (http://wso2.com/community).

# Contact Us
WSO2 developers can be contacted via the mailing lists:
* WSO2 Developers List : dev@wso2.org
* WSO2 Architecture List : architecture@wso2.org
