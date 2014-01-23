Sample: Google Spell Checker
=============================

Introduction
============

This example demonstrates the use of asynchronous Web method invocations. The user can continue to type 
text on the text editor where the text editor does the spell check from a hosted Web service. The Web 
method invocation is carried out in an asynchronous manner allowing the user to continuously type text 
without having to stall for the response.


Pre-Requisites
==============

Apache Ant 1.6.2 or later


Running The Sample
==================

To generate stubs and run the text editor, type ant.

Note that when running the build script, a stub is generated for the hosted spell check service at:
http://tools.wso2.net:12001/axis2/services/SimplifiedSpellCheck?wsdl

N.B.: This public Web service uses Google Web APIs to provide spell check service. In case of a
failure SimplifiedSpellCheck echo sends back the phrase (sent for spell check) instead of throwing
an error. Therefore, if the spell check editor just outputs the input words/phrase, that is because
of this behavior of the SimplifiedSpellCheck service.

Help
====
Please contact axis-user list (axis-user@ws.apache.org) if you have any trouble running the sample.











