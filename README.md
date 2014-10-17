#WSO2 Carbon Kernel

WSO2 Carbon redefines middleware by providing an integrated and componentized middleware platform that adapts to the specific needs of any enterprise IT project - on premise or in the cloud. 100% open source and standards-based, WSO2 Carbon enables developers to rapidly orchestrate business processes, compose applications and develop services using WSO2 Developer Studio and a broad range of business and technical services that integrate with legacy, packaged and SaaS applications.

WSO2 Carbon kernel, the lean, modular, OSGi-based platform, is the base of the WSO2 Carbon platform. It is a composable server architecture which inherits modularity and dynamism from OSGi framework. WSO2 Carbon kernel can be considered as a framework for server development. All the WSO2 products are composed as a collection reusable components running on this kernel. These products/components inherits all the core services provided by Carbon kernel such as Registry/repository, User management, Transports, Caching, Clustering, Logging, Deployment related features.

##Key Features
* Composable Server Architecture - Provides a modular, light-weight, OSGi-based server development framework.
* Carbon Application(CApp) deployment support.
* Multi-Profile Support for Carbon Platform - Enables a single product to run on multiple modes/profiles.
* Carbon + Tomcat JNDI Context - Provides ability to access both carbon level and tomcat level JNDI resources to applications using a single JNDI context.
* Distributed Caching and Clustering functionality - Provides a distributed cache and clustering implementation which is based on Hazelcast- a group communication framework
* Pluggable Transports Framework - Based on Axis2 transports module.
* Registry/Repository API- Provides core registry/repository API for component developers.
* User Management API - Provides a basic user management API for component developers.
* Logging - Supports both Java logging as well as Log4j. Logs from both these sources will be aggregated to a single output
* Pluggable artifact deployer framework - Can be extended to deploy any kind of artifacts such as Web services, Web apps, Business processes, Proxy services, User stores etc.
* Deployment Synchronization - Provides synchronization of deployed artifacts across a product cluster.
* Ghost Deployment - Provides a lazy loading mechanism for deployed artifacts</li>
* Multi-tenancy support - The roots of the multi-tenancy in Carbon platform lies in the Carbon kernel. This feature includes tenant level isolation as well as lazy loading of tenants.

##New Features
* Simplified logging story with pluggable log provider support.
* Upgraded versions of Hazelcast, Log4j, BouncyCastle.
* Improved Composite application support.


##How to Contribute

* WSO2 Carbon kernel code is hosted in [GitHub](https://github.com/wso2/carbon4-kernel/).
* Carbon 4.3.0 release work is carried out in "release-4.3.0" branch of this GitHub project.
* Please report issues at [Carbon JIRA](https://wso2.org/jira/browse/CARBON) and Send your pull requests to [development branch](https://github.com/wso2/carbon4-kernel/tree/development).

##Contact us

WSO2 Carbon developers can be contacted via the mailing lists:

* Carbon Developers List : dev@wso2.org
* Carbon Architecture List : architecture@wso2.org
