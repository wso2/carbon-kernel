#!/bin/sh

# Export the key as a request (use security as the password)
"$JAVA_HOME/bin/keytool" -keystore wss4j.keystore -alias wss4jCert -certreq -file cert.req
"$JAVA_HOME/bin/keytool" -keystore wss4j.keystore -alias wss4jCertDSA -certreq -file certDSA.req
