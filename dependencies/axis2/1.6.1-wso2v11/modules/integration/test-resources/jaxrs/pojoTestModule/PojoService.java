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


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;

//root level annotations some of these can be overriden by method level annotations.
@Path("testroot/")
@Consumes(value={"text/xml","application/xml"})
@Produces("text/xml")
public class PojoService {
/*
 * These are some operations for testing with plain old java deployment
 */
    @Path("add/{data1}")
    public String addDataFromURL(String data1){

          return data1+" created";
    }

    @POST
    @Path("getFromBody/{data1}")
    @Consumes("application/xml")
    public String addDataFromURLandBody(String data1,String data2){

          return data1+" and "+data2+" created";

    }

    @PUT
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


    

}



