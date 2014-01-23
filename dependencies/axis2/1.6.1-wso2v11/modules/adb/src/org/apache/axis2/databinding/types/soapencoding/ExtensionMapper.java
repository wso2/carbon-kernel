
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT  Built on : Dec 21, 2007 (04:03:30 LKT)
 */

            package org.apache.axis2.databinding.types.soapencoding;
            /**
            *  ExtensionMapper class
            */
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "arrayCoordinate".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.ArrayCoordinate.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "nonPositiveInteger".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NonPositiveInteger.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "int".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._int.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "NMTOKEN".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NMTOKEN.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "unsignedInt".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.UnsignedInt.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "IDREFS".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.IDREFS.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "short".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._short.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "negativeInteger".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NegativeInteger.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "normalizedString".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NormalizedString.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "boolean".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._boolean.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "unsignedLong".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.UnsignedLong.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "IDREF".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.IDREF.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "base64Binary".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Base64Binary.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "ID".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.ID.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "double".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._double.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "anyURI".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.AnyURI.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "language".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Language.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "ENTITY".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.ENTITY.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "unsignedShort".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.UnsignedShort.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "NMTOKENS".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NMTOKENS.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "NCName".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NCName.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "gMonthDay".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.GMonthDay.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "time".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Time.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "token".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Token.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "unsignedByte".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.UnsignedByte.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "nonNegativeInteger".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NonNegativeInteger.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "base64".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Base64.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "string".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.String.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "hexBinary".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.HexBinary.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "NOTATION".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.NOTATION.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "date".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Date.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "positiveInteger".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.PositiveInteger.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "Name".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Name.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "decimal".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Decimal.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "QName".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.QName.Factory.parse(reader);
                        

                  }
              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "duration".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Duration.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "Struct".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Struct.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "gYearMonth".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.GYearMonth.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "gMonth".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.GMonth.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "long".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._long.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "gYear".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.GYear.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "integer".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.Integer.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "gDay".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.GDay.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "float".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._float.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "ENTITIES".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.ENTITIES.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "dateTime".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding.DateTime.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://schemas.xmlsoap.org/soap/encoding/".equals(namespaceURI) &&
                  "byte".equals(typeName)){
                   
                            return  org.apache.axis2.databinding.types.soapencoding._byte.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    