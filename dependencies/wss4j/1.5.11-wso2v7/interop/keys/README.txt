- Certificates and keys are from the Gartner WSS interop show. 
- Passwords for every private key is 'password' (no quotes). 
- Bob identity for service and Alice identity for client.
- The ca.pfx files contains cert and private key for intermediary CA 
  used to issue Alice and Bob certificates and root.pfx contains cert 
  and private key for root CA used to issue the intermediary CA 
  certificate.
- Conversion tips are from http://mark.foster.cc/kb/openssl-keytool.html
- Please use JDK1.5 (importing WssIP has problems with JDK1.4)

set CLASSPATH=org.mortbay.jetty-5.1.4rc0.jar;

java org.mortbay.util.PKCS12Import alice.pfx interop2.jks
keytool -keyclone -keystore interop2.jks -alias 1 -dest alice
keytool -delete -keystore interop2.jks -alias 1

java org.mortbay.util.PKCS12Import bob.pfx interop2.jks
keytool -keyclone -keystore interop2.jks -alias 1 -dest bob
keytool -delete -keystore interop2.jks -alias 1

java org.mortbay.util.PKCS12Import WssIP.pfx interop2.jks
keytool -keyclone -keystore interop2.jks -alias "wssip's oasis interop test ca id" -dest wssip
keytool -delete -keystore interop2.jks -alias "wssip's oasis interop test ca id"

java org.mortbay.util.PKCS12Import ca.pfx ca.jks
java org.mortbay.util.PKCS12Import root.pfx root.jks

keytool -export -alias 1 -keystore root.jks -file root.crt
keytool -export -alias 1 -keystore ca.jks -file ca.crt

keytool -import -keystore interop2.jks -import -trustcacerts -alias root -file root.crt
keytool -import -keystore interop2.jks -import -trustcacerts -alias ca -file ca.crt

keytool -list -v -keystore interop2.jks