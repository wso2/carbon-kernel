

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
         * EchoServiceMessageReceiverInOut.java
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2 version: SNAPSHOT Apr 09, 2006 (10:20:36 CDT)
         */
        package server;

        /**
         *  EchoServiceMessageReceiverInOut message receiver
         */

        public class EchoServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        //Inject the Message Context if it is asked for
        //org.apache.axis2.engine.DependencyManager.configureBusinessLogicProvider(obj, msgContext.getOperationContext());

        EchoServiceSkeleton skel = (EchoServiceSkeleton)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        String methodName;
        if(op.getName() != null & (methodName = op.getName().getLocalPart()) != null){

        


            if("echoString".equals(methodName)){


            server.EchoStringResponse param3 = null;
            
                    //doc style
                    param3 =skel.echoString(
                            (server.EchoString)fromOM(msgContext.getEnvelope().getBody().getFirstElement(), server.EchoString.class));
                        
                        envelope = toEnvelope(getSOAPFactory(msgContext), param3, false);
                      

            }
        

        newMsgContext.setEnvelope(envelope);
       }
        
                    
                    }
            
            catch (Exception e) {
              throw org.apache.axis2.AxisFault.makeFault(e);
            }
        }
         
        //
                    private  org.apache.axiom.om.OMElement  toOM(server.EchoString param, boolean optimizeContent){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axiom.om.impl.builder.StAXOMBuilder builder
                                       = new org.apache.axiom.om.impl.builder.StAXOMBuilder
                            (org.apache.axiom.om.OMAbstractFactory.getOMFactory(),
                               new org.apache.axis2.util.StreamWrapper(param.getPullParser(server.EchoString.MY_QNAME)));
                            org.apache.axiom.om.OMElement documentElement = builder.getDocumentElement(true);
                            return documentElement;
                        }else{
                           
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }

                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, server.EchoString param, boolean optimizeContent){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axis2.databinding.ADBSOAPModelBuilder builder = new
                                    org.apache.axis2.databinding.ADBSOAPModelBuilder(param.getPullParser(server.EchoString.MY_QNAME),
                                                                                     factory);
                            return builder.getEnvelope();
                        }else{
                           
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }
                
                    private  org.apache.axiom.om.OMElement  toOM(server.EchoStringResponse param, boolean optimizeContent){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axiom.om.impl.builder.StAXOMBuilder builder
                                       = new org.apache.axiom.om.impl.builder.StAXOMBuilder
                            (org.apache.axiom.om.OMAbstractFactory.getOMFactory(),
                               new org.apache.axis2.util.StreamWrapper(param.getPullParser(server.EchoStringResponse.MY_QNAME)));
                            org.apache.axiom.om.OMElement documentElement = builder.getDocumentElement();
                            ((org.apache.axiom.om.impl.OMNodeEx) documentElement).setParent(null); // remove the parent link
                            return documentElement;
                        }else{
                           
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }

                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, server.EchoStringResponse param, boolean optimizeContent){
                        if (param instanceof org.apache.axis2.databinding.ADBBean){
                            org.apache.axis2.databinding.ADBSOAPModelBuilder builder = new
                                    org.apache.axis2.databinding.ADBSOAPModelBuilder(param.getPullParser(server.EchoStringResponse.MY_QNAME),
                                                                                     factory);
                            return builder.getEnvelope();
                        }else{
                           
                           //todo finish this onece the bean serializer has the necessary methods
                            return null;
                        }
                    }
                

           /**
           *  get the default envelope
           */
           private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
                return factory.getDefaultEnvelope();
           }


            private  java.lang.Object fromOM(org.apache.axiom.om.OMElement param,
            java.lang.Class type){

                try {
                       
                      if (server.EchoString.class.equals(type)){
                           return server.EchoString.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                      }
                              
                      if (server.EchoStringResponse.class.equals(type)){
                           return server.EchoStringResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                      }
                              
                } catch (Exception e) {
                     throw new RuntimeException(e);
                }

                return null;
            }

        

        }
    