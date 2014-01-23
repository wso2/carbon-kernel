* What is WSS4J? *

WSS4J is part of the Apache Web Services project:

http://ws.apache.org/

Apache WSS4J is an implementation of the OASIS Web Services Security specifications
(WS-Security, WSS) from OASIS Web Services Security TC. WSS4J is primarily
a Java library that can be used to sign, verify, encrypt, and decrypt SOAP Messages
according to the WS-Security specifications. WSS4J uses Apache Axis and other Apache 
XML-Security projects and is interoperable with other JAX-RPC based server/clients 
and .Net WSE server/clients that follow the OASIS WSS specifications

* Supported WSS Specifications *

WSS4J implements

 * Web Services Security: SOAP Message Security 1.1 
 * Username Token profile 1.1
 * X.509 Certificate Token Profile 1.1

The Web Services Security part of WSS4J is fairly well tested and many
WebService projects use it already. Also interoperability with
various other implementations is well tested.

* Support of older WSS specifications *

The WSS4J release 1.1.0 is the last release that was able to emulate previous 
WSS specs

The next WSS4J releases (>= 1.5.x)
- support the OASIS V1.0 specs and the relevant namespaces only
- support one versions of provisional (draft) namespaces for the upcoming version

After the next version of the WSS specs is finished, we do one WSS4J release 
with the provisional namespaces and another release (with a new release 
number) with the then fixed namespace URIs. Doing so we could save a lot of
coding while retaining some backward compatibility using the n-1 release.


* Web Services Security Features *

WSS4J can generate and process the following SOAP Bindings:

    o XML Security
         + XML Signature
         + XML Encryption
    o Tokens
         + Username Tokens
         + Timestamps
         + SAML Tokens

WSS4J supports X.509 binary certificates and certificate paths.

The master link to WSS4J: http://ws.apache.org/wss4j/

There is also a Wiki concerning Apache WS projects and WSS4J as one
of the WS sub-projects:
    http://wiki.apache.org/ws/
    http://wiki.apache.org/ws/FrontPage/WsFx
    

WS-Trust and WS-Secure Conversation specifications

WSS4J now comes with the support for derived key token signature and encryption.
This is used by the Axis2-"rahas" module to provide the WS-Secure Conversation.

WS-Trust support is also being developed within Axis2 based on WSS4J

org.apache.ws.sandbox. package contains experimental implementations of these 
specifications.

* Installation (binary distribution) *

The WSS4J zip archive is the binary distribution and contains the wss4j
jar file, some examples, test classes (incl. sources), the interop test
classes (incl. sources and necessary certificate store), and the according
client and server deployment and property files.

The WSS4J jar file contains all classes that implement the basic functions
and the handlers. To install it make sure this jar file is in the classpath
of your Axis client and/or Axis server. 

In addition you need to set up the property files that contain information
about the certificate keystores you use. The property files and the keystore
are accessed either as resources via classpath or, if that fails, as files
using the relative path of the application

Thus no specific installation is required. The wss4j jar file could be 
included into ear or war files of enterprise or web application servers.

Please refer to the JAVADOC files of the distribution for further 
information how to use WSS4J, the handlers, and how to setup the
deployment files.


* Crypto Notice *

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
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
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Bouncycastle     : http://www.bouncycastle.org/



* Required software *

To work with WSS4J you need additional software. Most of the software is also
needed by your SOAP base system, e.g. Apache Axis. 

To simplify installation and operation of WSS4J an additional ZIP file 
is provided that holds all other JARs that are required by WSS4J. Please 
note that we probably not use the very latest versions of these JARs, but 
we used them during the tests.

To implement the Web Service Security (WSS) part specific software is 
required:

axis-1.4.jar
axis-ant-1.4.jar
axis-jaxrpc-1.4.jar
axis-saaj-1.4.jar
    These jars contain the Apache Axis base software. They implement
    the basic SOAP processing, deployment, WSDL to Java, Java to WSDL
    tools and a lot more. Please refer to a Axis documentation how to
    setup Axis. You should be familiar with Axis, its setup, and 
    deployment methods before you start with any WSS4J functions.
    
    See: http://ws.apache.org/axis/

bcprov-jdk14-1.45.jar
    This is the BouncyCastle library that implements all necessary
    encryption, hashing, certificate, and keystore functions. Without
    this fantastic library WSS4J wouldn't work at all.
    
    See: http://www.bouncycastle.org/
    
commons-codec-1.3.jar
commons-discovery-0.2.jar
commons-logging-1.1.jar
    These jars are from the Commons project and provide may useful 
    functions, such as Base64 encoding/decoding, resource lookup,
    and much more. Please refer to the commons project to get more
    information.
    
    The master link for the commons project:
    http://jakarta.apache.org/commons/index.html

junit-3.8.1.jar
    The famous unit test library. Required if you like to build WSS4J
    from source and run the unit tests.
    
    See: http://www.junit.org/
    
log4j-1.2.9.jar
    The logging library. Required to control the logging, error 
    reporting and so on.
    
    See: http://logging.apache.org/

opensaml-1.1.jar
    The SAML implementation used by WSS4J to implement the SAML profile.
    
    See: http://www.opensaml.org/

serializer-2.7.1.jar
    The Apache Xalan XML serializer library.
    
    See: http://xml.apache.org/xalan-j/

wsdl4j-1.5.1.jar
    The WSDL parsing functions, required by Axis tools to read and
    parse WSDL.
    
    See: http://ws.apache.org/axis/  under related projects
    
xalan-2.7.1.jar
    Library that implements XML Path Language (XPath) and XSLT. The XML 
    Security implementation needs several functions of Xalan XPath.
   
    See: http://xml.apache.org/xalan-j/
   
xmlsec-1.4.3.jar
    This library implements the XML-Signature Syntax and Processing and
    the XML Encryption Syntax and Processing specifications of the W3C. Thus
    they form one of the base foundations of WSS4J.  
    
    See: http://xml.apache.org/security/
    
xercesImpl.jar
xml-apis.jar
    The XML parser implementation. Required by anybody :-) .

    See: http://xml.apache.org/xerces2-j/
