
/**
 * CertData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v12  Built on : Mar 19, 2015 (08:32:46 UTC)
 */

            
                package org.wso2.carbon.security.mgt.stub.keystore.xsd;
            

            /**
            *  CertData bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class CertData
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = CertData
                Namespace URI = http://service.keystore.security.carbon.wso2.org/xsd
                Namespace Prefix = ns2
                */
            

                        /**
                        * field for Alias
                        */

                        
                                    protected java.lang.String localAlias ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAliasTracker = false ;

                           public boolean isAliasSpecified(){
                               return localAliasTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAlias(){
                               return localAlias;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Alias
                               */
                               public void setAlias(java.lang.String param){
                            localAliasTracker = true;
                                   
                                            this.localAlias=param;
                                    

                               }
                            

                        /**
                        * field for IssuerDN
                        */

                        
                                    protected java.lang.String localIssuerDN ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIssuerDNTracker = false ;

                           public boolean isIssuerDNSpecified(){
                               return localIssuerDNTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIssuerDN(){
                               return localIssuerDN;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IssuerDN
                               */
                               public void setIssuerDN(java.lang.String param){
                            localIssuerDNTracker = true;
                                   
                                            this.localIssuerDN=param;
                                    

                               }
                            

                        /**
                        * field for NotAfter
                        */

                        
                                    protected java.lang.String localNotAfter ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotAfterTracker = false ;

                           public boolean isNotAfterSpecified(){
                               return localNotAfterTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotAfter(){
                               return localNotAfter;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotAfter
                               */
                               public void setNotAfter(java.lang.String param){
                            localNotAfterTracker = true;
                                   
                                            this.localNotAfter=param;
                                    

                               }
                            

                        /**
                        * field for NotBefore
                        */

                        
                                    protected java.lang.String localNotBefore ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNotBeforeTracker = false ;

                           public boolean isNotBeforeSpecified(){
                               return localNotBeforeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNotBefore(){
                               return localNotBefore;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NotBefore
                               */
                               public void setNotBefore(java.lang.String param){
                            localNotBeforeTracker = true;
                                   
                                            this.localNotBefore=param;
                                    

                               }
                            

                        /**
                        * field for PublicKey
                        */

                        
                                    protected java.lang.String localPublicKey ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPublicKeyTracker = false ;

                           public boolean isPublicKeySpecified(){
                               return localPublicKeyTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPublicKey(){
                               return localPublicKey;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PublicKey
                               */
                               public void setPublicKey(java.lang.String param){
                            localPublicKeyTracker = true;
                                   
                                            this.localPublicKey=param;
                                    

                               }
                            

                        /**
                        * field for SerialNumber
                        */

                        
                                    protected java.math.BigInteger localSerialNumber ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSerialNumberTracker = false ;

                           public boolean isSerialNumberSpecified(){
                               return localSerialNumberTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.math.BigInteger
                           */
                           public  java.math.BigInteger getSerialNumber(){
                               return localSerialNumber;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SerialNumber
                               */
                               public void setSerialNumber(java.math.BigInteger param){
                            localSerialNumberTracker = true;
                                   
                                            this.localSerialNumber=param;
                                    

                               }
                            

                        /**
                        * field for SubjectDN
                        */

                        
                                    protected java.lang.String localSubjectDN ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectDNTracker = false ;

                           public boolean isSubjectDNSpecified(){
                               return localSubjectDNTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectDN(){
                               return localSubjectDN;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectDN
                               */
                               public void setSubjectDN(java.lang.String param){
                            localSubjectDNTracker = true;
                                   
                                            this.localSubjectDN=param;
                                    

                               }
                            

                        /**
                        * field for Version
                        */

                        
                                    protected int localVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localVersionTracker = false ;

                           public boolean isVersionSpecified(){
                               return localVersionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return int
                           */
                           public  int getVersion(){
                               return localVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Version
                               */
                               public void setVersion(int param){
                            
                                       // setting primitive attribute tracker to true
                                       localVersionTracker =
                                       param != java.lang.Integer.MIN_VALUE;
                                   
                                            this.localVersion=param;
                                    

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
                           namespacePrefix+":CertData",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "CertData",
                           xmlWriter);
                   }

               
                   }
                if (localAliasTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "alias", xmlWriter);
                             

                                          if (localAlias==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAlias);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIssuerDNTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "issuerDN", xmlWriter);
                             

                                          if (localIssuerDN==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIssuerDN);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotAfterTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notAfter", xmlWriter);
                             

                                          if (localNotAfter==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotAfter);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNotBeforeTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "notBefore", xmlWriter);
                             

                                          if (localNotBefore==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNotBefore);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPublicKeyTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "publicKey", xmlWriter);
                             

                                          if (localPublicKey==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPublicKey);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSerialNumberTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "serialNumber", xmlWriter);
                             

                                          if (localSerialNumber==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSerialNumber));
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectDNTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "subjectDN", xmlWriter);
                             

                                          if (localSubjectDN==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectDN);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localVersionTracker){
                                    namespace = "http://service.keystore.security.carbon.wso2.org/xsd";
                                    writeStartElement(null, namespace, "version", xmlWriter);
                             
                                               if (localVersion==java.lang.Integer.MIN_VALUE) {
                                           
                                                         throw new org.apache.axis2.databinding.ADBException("version cannot be null!!");
                                                      
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersion));
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

                 if (localAliasTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "alias"));
                                 
                                         elementList.add(localAlias==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAlias));
                                    } if (localIssuerDNTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "issuerDN"));
                                 
                                         elementList.add(localIssuerDN==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIssuerDN));
                                    } if (localNotAfterTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "notAfter"));
                                 
                                         elementList.add(localNotAfter==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotAfter));
                                    } if (localNotBeforeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "notBefore"));
                                 
                                         elementList.add(localNotBefore==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNotBefore));
                                    } if (localPublicKeyTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "publicKey"));
                                 
                                         elementList.add(localPublicKey==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPublicKey));
                                    } if (localSerialNumberTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "serialNumber"));
                                 
                                         elementList.add(localSerialNumber==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSerialNumber));
                                    } if (localSubjectDNTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "subjectDN"));
                                 
                                         elementList.add(localSubjectDN==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectDN));
                                    } if (localVersionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd",
                                                                      "version"));
                                 
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localVersion));
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
        public static CertData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CertData object =
                new CertData();

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
                    
                            if (!"CertData".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (CertData)org.wso2.carbon.security.mgt.stub.keystore.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","alias").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAlias(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","issuerDN").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIssuerDN(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","notAfter").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotAfter(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","notBefore").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNotBefore(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","publicKey").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPublicKey(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","serialNumber").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSerialNumber(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInteger(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","subjectDN").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectDN(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://service.keystore.security.carbon.wso2.org/xsd","version").equals(reader.getName())){
                                
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"version" +"  cannot be null");
                                    }
                                    

                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                               object.setVersion(java.lang.Integer.MIN_VALUE);
                                           
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
           
    