

/**
 * KeyStoreAdminService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.security.mgt.stub.keystore;

    /*
     *  KeyStoreAdminService java interface
     */

    public interface KeyStoreAdminService {
          
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */
        public void  removeCertFromStore(
         org.wso2.carbon.security.mgt.stub.keystore.RemoveCertFromStore removeCertFromStore1

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getStoreEntries2
                
             * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.keystore.GetStoreEntriesResponse getStoreEntries(

                        org.wso2.carbon.security.mgt.stub.keystore.GetStoreEntries getStoreEntries2)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getStoreEntries2
            
          */
        public void startgetStoreEntries(

            org.wso2.carbon.security.mgt.stub.keystore.GetStoreEntries getStoreEntries2,

            final org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */
        public void  addKeyStore(
         org.wso2.carbon.security.mgt.stub.keystore.AddKeyStore addKeyStore4

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getPaginatedKeystoreInfo5
                
             * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.keystore.GetPaginatedKeystoreInfoResponse getPaginatedKeystoreInfo(

                        org.wso2.carbon.security.mgt.stub.keystore.GetPaginatedKeystoreInfo getPaginatedKeystoreInfo5)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getPaginatedKeystoreInfo5
            
          */
        public void startgetPaginatedKeystoreInfo(

            org.wso2.carbon.security.mgt.stub.keystore.GetPaginatedKeystoreInfo getPaginatedKeystoreInfo5,

            final org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */
        public void  importCertToStore(
         org.wso2.carbon.security.mgt.stub.keystore.ImportCertToStore importCertToStore7

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        

        /**
          * Auto generated method signature
          * 
                    * @param getKeystoreInfo8
                
             * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.keystore.GetKeystoreInfoResponse getKeystoreInfo(

                        org.wso2.carbon.security.mgt.stub.keystore.GetKeystoreInfo getKeystoreInfo8)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getKeystoreInfo8
            
          */
        public void startgetKeystoreInfo(

            org.wso2.carbon.security.mgt.stub.keystore.GetKeystoreInfo getKeystoreInfo8,

            final org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     
       /**
         * Auto generated method signature for Asynchronous Invocations
         * 
                 * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */
        public void  deleteStore(
         org.wso2.carbon.security.mgt.stub.keystore.DeleteStore deleteStore10

        ) throws java.rmi.RemoteException
        
        
               ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        

        /**
          * Auto generated method signature
          * 
             * @throws org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException : 
         */

         
                     public org.wso2.carbon.security.mgt.stub.keystore.GetKeyStoresResponse getKeyStores(

                        )
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceSecurityConfigExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
          */
        public void startgetKeyStores(

            

            final org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    