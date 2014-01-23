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

package org.apache.axis2.jaxrs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JAXRSModel {

    private String Path;

    private String Produces;

    private String Consumes;

    private String HttpMethod;

    private static Log log = LogFactory.getLog(JAXRSModel.class);
    /*
    *
    * Setter methods
    */
    public void setPath(String path) {
        this.Path = path;
    }

    public void setConsumes(String consumes) {
        this.Consumes = consumes;
    }

    public void setProduces(String produces) {
        this.Produces = produces;
    }

    public void setHTTPMethod(String httpmethod) {
        this.HttpMethod = httpmethod;

    }

    /**
     * getter methods
     */
    public String getPath() {

        return ((this.Path != null) && (!this.Path.equals(""))) ? this.Path : null;
    }


    /**
      * only returns the one mime type  as  wsdl 2.0 can publish only one mime type for an operation
      * @return
      */
    public String getConsumes() {
        if((this.Consumes != null) && (!this.Consumes.equals(""))){
                   String[] array=this.Consumes.split(",");
                   if(array.length > 1) {
                      log.warn("WSDL2 supports only one input serialization-considering only the first one ");
                       return array[0];
                   } else{
                       return array[0];
                   }
               }else{
                   return null;
               }

    }

    /**
      * only returns the one mime type  as  wsdl 2.0 can publish only one mime type for an operation
      * @return
      */
    public String getProduces() {
       if((this.Produces != null) && (!this.Produces.equals(""))){
           String[] array=this.Produces.split(",");
           if(array.length > 1) {
              log.warn("WSDL2 supports only one output-serialization");
               return array[0];
           } else{
               return array[0];
           }
       }else{
           return null;
       }

    }

    public String getHTTPMethod() {
        return ((this.HttpMethod != null) && (!this.HttpMethod.equals(""))) ? this.HttpMethod :
                null;
    }
}
