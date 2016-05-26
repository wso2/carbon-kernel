WSO2 Carbon Kernel ${carbon.version}
-----------------------------

Welcome to the WSO2 Carbon Kernel ${carbon.version} release

Carbon Kernel 5 is the next generation of WSO2 Carbon kernel, re-architected from the ground up with the latest
technologies and patterns to overcome the existing architectural limitations. Additionally, the Carbon Kernel
is now a lightweight, general-purpose OSGi runtime specializing in hosting servers, providing key functionality
for server developers. The result is a streamlined and even more powerful middleware platform than ever before.

What's New In This Release
----------------------------
1. Upgrade to Eclipse Luna SR2 OSGi Framework.
2. Pax Exam OSGi Test Framework Support.
3. Carbon Feature Plugin 2.0 integration.
4. Logging framework backend upgraded to log4j 2.0 support.

Installation & Running
----------------------
1. Extract the downloaded zip file.
2. Run the carbon.sh or carbon.bat file in the bin directory.
3. You can enable/disable OSGi Console by un-commenting the osgi.console property in
   CARBON_HOME/conf/osgi/launch.properties file.
4. You can enable OSGi debug logs by un-commenting the osgi.debug property in
   CARBON_HOME/conf/osgi/launch.properties file.

Hardware Requirements
-------------------
1. Minimum memory - 512MB
2. Processor      - Pentium 800MHz or equivalent at minimum

Software Requirements
-------------------
1. Java SE Development Kit - 1.8

All known issues have been recorded at https://wso2.org/jira/browse/CARBON

Carbon Binary Distribution Directory Structure
--------------------------------------------

     CARBON_HOME
        |-- bin <directory>
        |   |-- bootstrap <directory>
        |-- osgi <directory>
        |-- conf <directory>
        |-- logs <directory>
        |-- tmp <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        |-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.
        - bootstrap
            Contains the basic set of libraries required to bootstrap Carbon.
    - osgi
      Contains all OSGi related libraries and configurations.

    - conf
      Contains server configuration files. Ex: carbon.yml

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
(c) Copyright 2015 WSO2 Inc.
