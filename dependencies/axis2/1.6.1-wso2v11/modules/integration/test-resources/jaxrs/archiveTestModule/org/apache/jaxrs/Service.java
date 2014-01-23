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

package org.apache.jaxrs;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.WSDL2Constants;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.namespace.QName;
import java.util.Iterator;

//root level annotations. some of these can be overriden by method level annotations.
@Path("testroot/")
@Consumes(value={"text/xml","application/xml"})
@Produces("text/xml")
public class Service {
/*
 * These are some operations for testing with plain archive deployment. here we try to test
 * proper functionality of http:location & http:method. since these values can be overriden in the Services xml
 * there are some test methods to test that behaviour too.
 */
    // uses the root path, root input-output serialization & default httpMethod

    @Path("add/{data1}")
    public String addDataFromURL(String data1){

          return data1+" created";
    }

    @POST
    @Path("getFromBody/{data1}")
    @Consumes("application/xml")
    @Produces("text/xml")
    public String addDataFromURLandBody(String data1,String data2){

          return data1+" and "+data2+" created";

    }

    @PUT
    @Produces("application/xml")
    @Path("update/{data1}")
    public String updateDataFromURL(String data1){
        return data1+" updated";

    }

    @PUT
    @Path("getFromBody/{data1}")
    public String updateDataFromURLandBody(String data1,String data2){

        return data1+" and "+data2+" updated";
    }

    @GET
    @Path("get/{data1}")
    public String getDataFromURL(String data1){

       return data1+" read";


    }

    @GET
    @Path("getFromBody/{data1}")
    public String getDataFromURLandBody(String data1,String data2){

        return data1+" and "+data2+" read";
    }

    @DELETE
    @Path("delete/{data1}")
    public String deleteDataFromURL(String data1){

        return data1+" deleted";
    }

    @DELETE
    @Path("getFromBody/{data1}")
    public String deleteDataFromURLandBody(String data1,String data2){

        return data1+" and "+data2+" deleted";

    }


    @POST
    @Path("wrong/error/{data1}")
    public String checkServicesXMLoverriding(String data1,String data2){

        return data1+" and "+data2+" correct";

    }


   public boolean checkTypePublishing(){
      
       MessageContext msgctx= MessageContext.getCurrentMessageContext();
       AxisService axisServce= msgctx.getAxisService();
       AxisEndpoint axisEndpoint= axisServce.getEndpoint("AnnotationServiceHttpEndpoint");
       AxisBinding axisBinding=axisEndpoint.getBinding();
       AxisBindingOperation axisBindingOperation;
       Iterator<AxisBindingOperation> iterator=axisBinding.getChildren();


         while(iterator.hasNext()){
           axisBindingOperation=iterator.next();
             if(axisBindingOperation.getName().equals(new QName("addDataFromURLandBody"))){
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION).equals("testroot/getFromBody/{data1}")){
                 System.out.println("wrong http location : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION)+" expected : testroot/getFromBody/{data1}" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD).equals("POST")){
                  System.out.println("wrong http method : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD)+" expected : POST" );
                  return false;
                }

                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION).equals("application/xml")){
                     System.out.println("INPUT SERIALIZATION TESTS");
                  System.out.println("wrong input serialization : found : "+ axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION) +" expected : application/xml" );
                  return false;
                }

                if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION).equals("text/xml")){
                     System.out.println("OUTPUT SERIALIZATION TEST");
                  System.out.println("wrong output serialization : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION)+" expected : text/xml" );
                  return false;
                }
                 System.out.println("Type publishing test \"addDataFromURLandBody\" OK.........");
             }   

             if(axisBindingOperation.getName().equals(new QName("updateDataFromURL"))){
              
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION).equals("testroot/update/{data1}")){
                 System.out.println("wrong http location : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION)+" expected : testroot/update/{data1}" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD).equals("PUT")){
                  System.out.println("wrong http method : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD)+" expected : PUT" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION).equals("text/xml")){
                  System.out.println("wrong input serialization : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION)+" expected : text/xml" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION).equals("application/xml")) {
                  System.out.println("wrong output serialization : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION)+" expected : application/xml" );
                  return false;
                }
                 System.out.println("Type publishing test \"updateDataFromURL\" OK.........");
             }

             if(axisBindingOperation.getName().equals(new QName("checkServicesXMLoverriding"))){
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION).equals("serviceroot/check")){
                 System.out.println("wrong http location : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION)+" expected : serviceroot/check" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD).equals("POST")){
                  System.out.println("wrong http method : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_METHOD)+" expected : POST" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION).equals("application/xml")){
                  System.out.println("wrong input serialization : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION)+" expected : application/xml" );
                  return false;
                }
                 if(!axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION).equals("application/xml")) {
                  System.out.println("wrong output serialization : found : "+axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION)+" expected : application/xml" );
                  return false;
                }
                 System.out.println("Type publishing test \"checkServicesXMLoverriding\" OK.........");
             }
         }
      return true; // all the test are ok     
   }



}



