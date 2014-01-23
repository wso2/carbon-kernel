Sample: ServiceLifeCycle
=========================

Introduction:
============

This sample demonstrate usage of the service lifecycle and a bit of session managment. 
The main idea is to show where and how to use service lifecycle interface and 
session related methods. 


Prerequisites
=============
Apache Ant 1.6.2 or later

If you want to access the service in a REST manner you have to deploy the service in an
application server such as Apache Tomcat. Note that it will not work with axis2server.



Deploying the Service
===================== 

Deploy into Sample repository:
    
 * Type ant generate.service or simply ant from AXIS2_HOME/samples/servicelifecycle

Deploy into Tomcat :
     
 * To build and copy the service archive file into Tomcat, type ant copy.to.tomcat from 
AXIS2_HOME/samples/servicelifecycle which will copy the aar file into
<tomcat_home>/web-app/axis2/WEB-INF/services directory.

Running the Client
==================
Type ant run.client from AXIS2_HOME/samples/servicelifecycle.

And then follow the instructions as mentioned in the console.

When asked for service epr address, enter
http://<host>:<port>/axis2/services/Library
Where <host> & <port> would be the host and port that tomcat is running on, respectively.

Advanced Guide
==============
For more details kindly see doc/servicelifecycleguide.html

Note
==============
Sometimes, if you're having trouble running the client successfully, 
It may be necessary to clean the services repository before you generate the service, deploy it
and run the client. (i.e. delete services created from previous samples.)

Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.

