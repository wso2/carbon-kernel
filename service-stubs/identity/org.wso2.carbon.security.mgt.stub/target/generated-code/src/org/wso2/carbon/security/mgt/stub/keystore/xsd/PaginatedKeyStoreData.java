
/**
 * PaginatedKeyStoreData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.security.mgt.stub.keystore.xsd;
            

            /**
            *  PaginatedKeyStoreData bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class PaginatedKeyStoreData
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = PaginatedKeyStoreData
                Namespace URI = http://service.keystore.security.carbon.wso2.org/xsd
                Namespace Prefix = ns2
                */
            

                        /**
                        * field for Key
                        */

                        
                                    protected org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData localKey ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localKeyTracker = false ;

                           public boolean isKeySpecified(){
                               return localKeyTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData
                           */
                           public  org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData getKey(){
                               return localKey;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Key
                               */
                               public void setKey(org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData param){
                            localKeyTracker = true;
                                   
                                            this.localKey=param;
                                    

                               }
                            

                        /**
                        * field for KeyStoreName
                        */

                        
                                    protected java.lang.String localKeyStoreName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localKeyStoreNameTracker = false ;

                           public boolean isKeyStoreNameSpecified(){
                               return localKeyStoreNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getKeyStoreName(){
                               return localKeyStoreName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param KeyStoreName
                               */
                               public void setKeyStoreName(java.lang.String param){
                            localKeyStoreNameTracker = true;
                                   
                                            this.localKeyStoreName=param;
                                    

                               }
                            

                        /**
                        * field for KeyStoreType
                        */

                        
                                    protected java.lang.String localKeyStoreType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localKeyStoreTypeTracker = false ;

                           public boolean isKeyStoreTypeSpecified(){
                               return localKeyStoreTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getKeyStoreType(){
                               return localKeyStoreType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param KeyStoreType
                               */
                               public void setKeyStoreType(java.lang.String param){
                            localKeyStoreTypeTracker = true;
                                   
                                            this.localKeyStoreType=param;
                                    

                               }
                            

                        /**
                        * field for KeyValue
                        */

                        
                                    protected java.lang.String localKeyValue ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localKeyValueTracker = false ;

                           public boolean isKeyValueSpecified(){
                               return localKeyValueTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getKeyValue(){
                               return localKeyValue;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param KeyValue
                               */
                               public void setKeyValue(java.lang.String param){
                            localKeyValueTracker = true;
                                   
                                            this.localKeyValue=param;
                                    

                               }
                            

                        /**
                        * field for PaginatedCertData
                        */

                        
                                    protected org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedCertData localPaginatedCertData ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPaginatedCertDataTracker = false ;

                           public boolean isPaginatedCertDataSpecified(){
                               return localPaginatedCertDataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedCertData
                           */
                           public  org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedCertData getPaginatedCertData(){
                               return localPaginatedCertData;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PaginatedCertData
                               */
                               public void setPaginatedCertData(org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedCertData param){
                            localPaginatedCertDataTracker = true;
                                   
                                            this.localPaginatedCertData=param;
                                    

                               }
                            

                        /**
                        * field for PrivateStore
                        */

                        
                                    protected boolean localPrivateStore ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrivateStoreTracker = false ;

                           public boolean isPrivateStoreSpecified(){
                               return localPrivateStoreTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getPrivateStore(){
                               return localPrivateStore;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PrivateStore
                               */
                               public void setPrivateStore(boolean param){
                            
                                       // setting primitive attribute tracker to true
                                       localPrivateStoreTracker =
                                       true;
                                   
                                            this.localPrivateStore=param;
                                    

                               }
                            

                        /**
                        * field for Provider
                        */

                        
                                    protected java.lang.String localProvider ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localProviderTracker = false ;

                           public boolean isProviderSpecified(){
                               return localProviderTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getProvider(){
                               return localProvider;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Provider
                               */
                               public void setProvider(java.lang.String param){
                            localProviderTracker = true;
                                   
                                            this.localProvider=param;
                                    

                               }
                            

                        /**
                        * field for PubKeyFilePath
                        */

                        
                                    protected java.lang.String localPubKeyFilePath ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPubKeyFilePathTracker = false ;

                           public boolean isPubKeyFilePathSpecified(){
                               return localPubKeyFilePathTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPubKeyFilePath(){
                               return localPubKeyFilePath;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PubKeyFilePath
                               */
                               public void setPubKeyFilePath(java.lang.String param){
                            localPubKeyFilePathTracker = true;
                                   
                                            this.localPubKeyFilePath=param;
                                    

                               }
                            

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://service.keystore.security.carbon.wso2.org/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":PaginatedKeyStoreData",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "PaginatedKeyStoreData",
                           xmlWriter);
                   }

               
                   }
                if (localKeyTracker){
                                    if (localKey==null){

                                        writeStartElement(null, "http://service.keystore.security.carbon.wso2.org/xsd", "key", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localKey.serialize(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","key"),
                                        xmlWriter);
                                    }
                                } if (localKeyStoreNameTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "keyStoreName", xmlWriter);
                             

                                          if (localKeyStoreName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localKeyStoreName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localKeyStoreTypeTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "keyStoreType", xmlWriter);
                             

                                          if (localKeyStoreType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localKeyStoreType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localKeyValueTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "keyValue", xmlWriter);
                             

                                          if (localKeyValue==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localKeyValue);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPaginatedCertDataTracker){
                                    if (localPaginatedCertData==null){

                                        writeStartElement(null, "http://service.keystore.security.carbon.wso2.org/xsd", "paginatedCertData", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localPaginatedCertData.serialize(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","paginatedCertData"),
                                        xmlWriter);
                                    }
                                } if (localPrivateStoreTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "privateStore", xmlWriter);
                             
                                               if (false) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("privateStore cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrivateStore));
                                               }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localProviderTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "provider", xmlWriter);
                             

                                          if (localProvider==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localProvider);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPubKeyFilePathTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "pubKeyFilePath", xmlWriter);
                             

                                          if (localPubKeyFilePath==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPubKeyFilePath);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://service.keystore.security.carbon.wso2.org/xsd")){
                return "ns2";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localKeyTracker){
                            elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "key"));
                            
                            
                                    elementList.add(localKey==null?null:
                                    localKey);
                                } if (localKeyStoreNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "keyStoreName"));
                                 
                                         elementList.add(localKeyStoreName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localKeyStoreName));
                                    } if (localKeyStoreTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "keyStoreType"));
                                 
                                         elementList.add(localKeyStoreType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localKeyStoreType));
                                    } if (localKeyValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "keyValue"));
                                 
                                         elementList.add(localKeyValue==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localKeyValue));
                                    } if (localPaginatedCertDataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "paginatedCertData"));
                            
                            
                                    elementList.add(localPaginatedCertData==null?null:
                                    localPaginatedCertData);
                                } if (localPrivateStoreTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "privateStore"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrivateStore));
                            } if (localProviderTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "provider"));
                                 
                                         elementList.add(localProvider==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProvider));
                                    } if (localPubKeyFilePathTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "pubKeyFilePath"));
                                 
                                         elementList.add(localPubKeyFilePath==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPubKeyFilePath));
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static PaginatedKeyStoreData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            PaginatedKeyStoreData object =
                new PaginatedKeyStoreData();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"PaginatedKeyStoreData".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (PaginatedKeyStoreData)org.wso2.carbon.security.mgt.stub.keystore.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","key").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setKey(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setKey(org.wso2.carbon.security.mgt.stub.keystore.xsd.CertData.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","keyStoreName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setKeyStoreName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","keyStoreType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setKeyStoreType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","keyValue").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setKeyValue(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","paginatedCertData").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setPaginatedCertData(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setPaginatedCertData(org.wso2.carbon.security.mgt.stub.keystore.xsd.PaginatedCertData.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","privateStore").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"privateStore" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPrivateStore(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","provider").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setProvider(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","pubKeyFilePath").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPubKeyFilePath(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    