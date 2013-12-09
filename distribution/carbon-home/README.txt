WSO2 Carbon Kernel ${carbon.version}
-----------------

${buildNumber}

Welcome to the WSO2 Carbon Kernel ${carbon.version} release

WSO2 Carbon is the base platform for all WSO2 products, powered by OSGi.  It is a
lightweight, high performing platform which is a collection of OSGi bundles. All
the major features which are included in WSO2 products have been developed as
pluggable Carbon components and installed into this base Carbon platform.

What's New In This Release
----------------------------
1. Equinox Kepler SR1 based OSGI runtime
2. PAX Logging based logging framework


Key Features
------------
1. Latest Equinox-SDK - WSO2 Carbon Kernel now embeds the latest Equinox SDK (3.9.1) as its OSGi runtime.
2. PAX Logging based logging framework which supports various logging API's, including OSGI LoggingService

Installation & Running
----------------------
1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory

Hardware Requirements
-------------------
1. Minimum memory - 1GB
2. Processor      - Pentium 800MHz or equivalent at minimum

Software Requirements
-------------------
1. Java SE Development Kit - 1.6 (1.6.0_24 onwards)

For more details see
http://docs.wso2.org/wiki/display/Carbon500/Installation+Prerequisites

Known Issues
------------

All known issues have been recorded at https://wso2.org/jira/browse/CARBON

Carbon Binary Distribution Directory Structure
--------------------------------------------

     CARBON_HOME
        |-- bin <directory>
        |-- lib <directory>
        |-- repository <directory>
        |   |-- components <directory>
        |   |-- conf <directory>
        |   |-- deployment <directory>
        |   |-- logs <directory>
            |-- README.txt <file>
        |-- tmp <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        |-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.

    - lib
      Contains the basic set of libraries required to startup Carbon.

    - repository
      The repository where artifacts which are deployed in WSO2 Carbon based products are stored.

    	- components
          Contains all OSGi related libraries and configurations.

        - conf
          Contains server configuration files. Ex: axis2.xml, carbon.xml

        - deployment
	      All deployment artifacts should go into this directory.

        - logs
          Contains all log files created during execution.

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
      Release information for WSO2 Carbon Kernel ${carbon.version}.

Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Carbon, visit WSO2 Carbon Home Page (http://wso2.com/products/carbon)


---------------------------------------------------------------------------
(c) Copyright 2014 WSO2 Inc.