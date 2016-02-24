
/**
 * KeyStoreAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.security.mgt.stub.keystore;

    /**
     *  KeyStoreAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class KeyStoreAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public KeyStoreAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public KeyStoreAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getStoreEntries method
            * override this method for handling normal response from getStoreEntries operation
            */
           public void receiveResultgetStoreEntries(
                    org.wso2.carbon.security.mgt.stub.keystore.GetStoreEntriesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getStoreEntries operation
           */
            public void receiveErrorgetStoreEntries(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPaginatedKeystoreInfo method
            * override this method for handling normal response from getPaginatedKeystoreInfo operation
            */
           public void receiveResultgetPaginatedKeystoreInfo(
                    org.wso2.carbon.security.mgt.stub.keystore.GetPaginatedKeystoreInfoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPaginatedKeystoreInfo operation
           */
            public void receiveErrorgetPaginatedKeystoreInfo(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getKeystoreInfo method
            * override this method for handling normal response from getKeystoreInfo operation
            */
           public void receiveResultgetKeystoreInfo(
                    org.wso2.carbon.security.mgt.stub.keystore.GetKeystoreInfoResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getKeystoreInfo operation
           */
            public void receiveErrorgetKeystoreInfo(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getKeyStores method
            * override this method for handling normal response from getKeyStores operation
            */
           public void receiveResultgetKeyStores(
                    org.wso2.carbon.security.mgt.stub.keystore.GetKeyStoresResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getKeyStores operation
           */
            public void receiveErrorgetKeyStores(java.lang.Exception e) {
            }
                


    }
    