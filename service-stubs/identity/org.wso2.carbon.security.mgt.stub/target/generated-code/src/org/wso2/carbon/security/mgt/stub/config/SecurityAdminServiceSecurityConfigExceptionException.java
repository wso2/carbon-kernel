
/**
 * SecurityAdminServiceSecurityConfigExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

package org.wso2.carbon.security.mgt.stub.config;

public class SecurityAdminServiceSecurityConfigExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1456290811684L;
    
    private org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigException faultMessage;

    
        public SecurityAdminServiceSecurityConfigExceptionException() {
            super("SecurityAdminServiceSecurityConfigExceptionException");
        }

        public SecurityAdminServiceSecurityConfigExceptionException(java.lang.String s) {
           super(s);
        }

        public SecurityAdminServiceSecurityConfigExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public SecurityAdminServiceSecurityConfigExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigException getFaultMessage(){
       return faultMessage;
    }
}
    