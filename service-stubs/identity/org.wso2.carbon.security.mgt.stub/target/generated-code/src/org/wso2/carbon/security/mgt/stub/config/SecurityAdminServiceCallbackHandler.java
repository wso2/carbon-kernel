
/**
 * SecurityAdminServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:27 UTC)
 */

    package org.wso2.carbon.security.mgt.stub.config;

    /**
     *  SecurityAdminServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SecurityAdminServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SecurityAdminServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SecurityAdminServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getScenarios method
            * override this method for handling normal response from getScenarios operation
            */
           public void receiveResultgetScenarios(
                    org.wso2.carbon.security.mgt.stub.config.GetScenariosResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getScenarios operation
           */
            public void receiveErrorgetScenarios(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getSecurityConfigData method
            * override this method for handling normal response from getSecurityConfigData operation
            */
           public void receiveResultgetSecurityConfigData(
                    org.wso2.carbon.security.mgt.stub.config.GetSecurityConfigDataResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSecurityConfigData operation
           */
            public void receiveErrorgetSecurityConfigData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSecurityScenario method
            * override this method for handling normal response from getSecurityScenario operation
            */
           public void receiveResultgetSecurityScenario(
                    org.wso2.carbon.security.mgt.stub.config.GetSecurityScenarioResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSecurityScenario operation
           */
            public void receiveErrorgetSecurityScenario(java.lang.Exception e) {
            }
                


    }
    