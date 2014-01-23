Sample: mex (Metadata Exchange)
====================================

Introduction
============

The examples provided demostrates data retrieval for WS-MetadataExchange GetMetadata request
handled by different level of data locators below: 
  1. Default Axis2 data locator
  2. Plug-in service level data locator
  3. Plug-in service sevel WSDL-specific data locator

Note: Example 2 and 3 are only for the sole purpose of demostrating plug-in data locator support. The
      data retrieval logic for the plug-in data locators are not implemented.

Details for the examples are documented in the Apache Metadata Exchange User's Guide.

Prerequisites  
=============

To build the sample service you must have ant-1.6.x installed in your system. 

To set AXIS2_HOME in Unix/Linux type:
$export AXIS2_HOME=<path to axis2 distribution>

metadataExchange module must be deployed and engaged. 
Please refer to Apache Metadata Exchange User's Guide for how to deploy and engage 
the metadataExchange module.


Building the Service
====================

To build the sample service, type: $ant generate.service or just ant

This will build the DefaultAxis2DataLocatorDemoService.aar, ServiceLevelDataLocatorDemoService.aar,
and WSDLDataLocatorDemoService.aar service archive files in the build directory and copy them to the
<AXIS2_HOME>/repository/services directory.

You can start the Axis2 server by running either axis2server.bat (on Windows) or axis2server.sh
(on Linux)that are located in <AXIS2_HOME>/bin directory.

The WSDL for this service should be viewable at:

http://<yourhost>:<yourport>/axis2/services/DefaultAxis2DataLocatorDemoService?wsdl 
(e.g. http://localhost:8080/axis2/services/DefaultAxis2DataLocatorDemoService?wsdl)

The clients to send GetMetadata requests are available in samples/mex/src/userguide/mex/clients directory.


Running the Client
==================

To compile and run Default Axis2 data locator scenario, type
$ant run.client.default

To compile and run Plug-in service level Axis2 data locator scenario, type
$ant run.client.service

To compile and run PLug-in WSDL-specific level Axis2 data locator scenario, type
$ant run.client.wsdl


Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.




