
/**
 * KeyStoreAdminServiceSecurityConfigExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.security.mgt.stub.keystore;

public class KeyStoreAdminServiceSecurityConfigExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290810003L;
    
    private org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigException faultMessage;

    
        public KeyStoreAdminServiceSecurityConfigExceptionException() {
            super("KeyStoreAdminServiceSecurityConfigExceptionException");
        }

        public KeyStoreAdminServiceSecurityConfigExceptionException(java.lang.String s) {
           super(s);
        }

        public KeyStoreAdminServiceSecurityConfigExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public KeyStoreAdminServiceSecurityConfigExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigException getFaultMessage(){
       return faultMessage;
    }
}
    