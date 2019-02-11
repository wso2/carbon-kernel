WSO2 Carbon ${carbon.version}
-----------------

${buildNumber}

Welcome to the WSO2 Carbon ${carbon.version} release

WSO2 Carbon is the base platform for all WSO2 products, powered by OSGi.  It is a
lightweight, high performing platform which is a collection of OSGi bundles. All
the major features which are included in WSO2 products have been developed as
pluggable Carbon components and installed into this base Carbon platform.

What's New In This Release
----------------------------
01. Java7/Java8 support - Starting from kernel 4.4.0 release, the kernel can now be compiled and run on both Java7 and Java8.
02. Tomcat upgrade - Tomcat is upgraded to version 7.0.59.
03. Tenant Eager Loading - WSO2 Carbon Kernel can now be configured to load specified set of tenants at server start-up.

Key Features
------------
01. Composable Server Architecture - Provides a modular, light-weight, OSGi-based server development framework.
02. Carbon Application(CApp) deployment support.
03. Multi-Profile Support for Carbon Platform - This enable a single product to run on multiple modes/profiles.
04. Carbon + Tomcat JNDI Context - Provide ability to access both carbon level and tomcat level JNDI resources to applications using a single JNDI context.
05. Distributed Caching and Clustering functionality - Carbon kernel provides a distributed cache and clustering implementation which is based on Hazelcast- a group communication framework
06. Pluggable Transports Framework - This is based on Axis2 transports module
07. Registry/Repository API- Provide core registry/repository API for component developers
08. User Management API - Provides a basic user management API for component developers
09. Logging - Carbon kernel supports both Java logging as well as Log4j. Logs from both these sources will be aggregated to a single output
10. Pluggable artifact deployer framework - Kernel can be extended to deploy any kind of artifacts such as Web services, Web apps, Business processes, Proxy services, User stores etc.
11. Deployment Synchronization - Provides synchronization of deployed artifacts across a product cluster.
12. Ghost Deployment - Provides a lazy loading mechanism for deployed artifacts
13. Multi-tenancy support - The roots of the multi-tenancy in Carbon platform lies in the Carbon kernel. This feature includes tenant level isolation as well as lazy loading of tenants.


Carbon Binary Distribution Directory Structure
--------------------------------------------

     CARBON_HOME
        |-- bin <directory>
        |-- dbscripts <directory>
        |-- lib <directory>
        |-- repository <directory>
        |   |-- components <directory>
        |   |-- conf <directory>
        |   |-- data <directory>
        |   |-- database <directory>
        |   |-- deployment <directory>
        |   |-- logs <directory>
        |   |-- resources <directory>
        |   |   |-- security <directory>
        |   |-- tenants <directory>
            |-- README.txt <file>
        |-- samples <directory>
        |-- tmp <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        |-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.

    - dbscripts
      Contains the database creation & seed data population SQL scripts for
      various supported databases.

    - lib
      Contains the basic set of libraries required to startup Carbon.

    - repository
      The repository where Carbon artifacts & Axis2 services and 
      modules deployed in WSO2 Carbon are stored. 
      In addition to this other custom deployers such as
      dataservices and axis1services are also stored.

    	- components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. Ex: axis2.xml, carbon.xml

        - data
          Contains internal LDAP related data.

        - database
          Contains the WSO2 Registry & User Manager database.

        - deployment
          Contains server side and client side Axis2 repositories. 
	      All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

        - resources
          Contains additional resources that may be required.

        - tenants
          Directory will contain relevant tenant artifacts
          in the case of a multitenant deployment.

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property.

    - LICENSE.txt
      Apache License 2.0 under which WSO2 Carbon is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document contains information on installing WSO2 Carbon.

    - release-notes.html
      Release information for WSO2 Carbon ${carbon.version}.

Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Carbon, visit WSO2 Carbon Home Page (http://wso2.com/products/carbon)

Crypto Notice
-------------

This distribution includes cryptographic software.  The country in
which you currently reside may have restrictions on the import,
possession, use, and/or re-export to another country, of
encryption software.  Before using any encryption software, please
check your country's laws, regulations and policies concerning the
import, possession, or use, and re-export of encryption software, to
see if this is permitted.  See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms.  The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included cryptographic
software:

Apache Santuario : http://santuario.apache.org/
Bouncycastle     : http://www.bouncycastle.org/

---------------------------------------------------------------------------
(c) Copyright 2018 WSO2 Inc.
