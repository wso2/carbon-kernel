
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

        
            package org.wso2.carbon.security.xsd;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://service.config.security.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "SecurityScenarioData".equals(typeName)){
                   
                            return  org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://service.config.security.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "SecurityScenarioDataWrapper".equals(typeName)){
                   
                            return  org.wso2.carbon.security.mgt.stub.config.xsd.SecurityScenarioDataWrapper.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://service.config.security.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "SecurityConfigData".equals(typeName)){
                   
                            return  org.wso2.carbon.security.mgt.stub.config.xsd.SecurityConfigData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://service.config.security.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "KerberosConfigData".equals(typeName)){
                   
                            return  org.wso2.carbon.security.mgt.stub.config.xsd.KerberosConfigData.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://security.carbon.wso2.org/xsd".equals(namespaceURI) &&
                  "SecurityConfigException".equals(typeName)){
                   
                            return  org.wso2.carbon.security.xsd.SecurityConfigException.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    