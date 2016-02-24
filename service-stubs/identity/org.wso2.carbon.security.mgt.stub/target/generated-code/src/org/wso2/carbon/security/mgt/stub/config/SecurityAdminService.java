

/**
 * SecurityAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.security.mgt.stub.config;

    /*
     *  SecurityAdminService java interface
     */

    public interface SecurityAdminService {
          

        /**
          * Auto generated method signature
          * 
                    * @param getScenarios0
                
             * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.config.GetScenariosResponse getScenarios(

                        org.wso2.carbon.security.mgt.stub.config.GetScenarios getScenarios0)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getScenarios0
            
          */
        public void startgetScenarios(

            org.wso2.carbon.security.mgt.stub.config.GetScenarios getScenarios0,

            final org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */
        public void  applySecurity(
         org.wso2.carbon.security.mgt.stub.config.ApplySecurity applySecurity2

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */
        public void  activateUsernameTokenAuthentication(
         org.wso2.carbon.security.mgt.stub.config.ActivateUsernameTokenAuthentication activateUsernameTokenAuthentication3

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */
        public void  applyKerberosSecurityPolicy(
         org.wso2.carbon.security.mgt.stub.config.ApplyKerberosSecurityPolicy applyKerberosSecurityPolicy4

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */
        public void  disableSecurityOnService(
         org.wso2.carbon.security.mgt.stub.config.DisableSecurityOnService disableSecurityOnService5

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getSecurityConfigData6
                
             * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.config.GetSecurityConfigDataResponse getSecurityConfigData(

                        org.wso2.carbon.security.mgt.stub.config.GetSecurityConfigData getSecurityConfigData6)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSecurityConfigData6
            
          */
        public void startgetSecurityConfigData(

            org.wso2.carbon.security.mgt.stub.config.GetSecurityConfigData getSecurityConfigData6,

            final org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param getSecurityScenario8
                
             * @throws org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.config.GetSecurityScenarioResponse getSecurityScenario(

                        org.wso2.carbon.security.mgt.stub.config.GetSecurityScenario getSecurityScenario8)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getSecurityScenario8
            
          */
        public void startgetSecurityScenario(

            org.wso2.carbon.security.mgt.stub.config.GetSecurityScenario getSecurityScenario8,

            final org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    