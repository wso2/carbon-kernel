WSO2 Carbon Kernel ${carbon.version} M1
-----------------------------

Welcome to the WSO2 Carbon Kernel ${carbon.version} M1 release

Carbon kernel 5 is the next generation of WSO2 Carbon kernel, re-architected from the ground up with the latest technologies and patterns to overcome the existing architectural limitations as well as to get rid of the dependencies to the legacy technologies like Apache Axis2.

What's New In This Release
----------------------------
1. Equinox Kepler based light-weight OSGi runtime.
2. New Carbon launcher implementation 
3. Centralized logging back-end (based on Log4j) which supports multiple logging APIs.

Installation & Running
----------------------
1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory

Hardware Requirements
-------------------
1. Minimum memory - 512MB
2. Processor      - Pentium 800MHz or equivalent at minimum

Software Requirements
-------------------
1. Java SE Development Kit - 1.7

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
