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


import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;

public class JAXRSUtils {
    private static Log log = LogFactory.getLog(JAXRSModel.class);


    
    /**
      * returns a jaxrs class model reading class level annotation given the service class
      * @param serviceClass
      * @return
      */
    public static JAXRSModel getClassModel(Class serviceClass) {
        JAXRSModel model = new JAXRSModel();
        Annotation[] annotation = serviceClass.getAnnotations();
        for (Annotation a : annotation) {
            if (a != null) {
                if (a instanceof Produces) {
                    addProducesToClassModel((Produces) a, model);
                } else if (a instanceof Consumes) {
                    addConsumesToClassModel((Consumes) a, model);
                } else if (a instanceof Path) {
                    addPathToClassModel((Path) a, model);
                } else {
                    System.out.println("Could not identify the Annotation....");
                }

            }

        }

        return model;
    }


    /**
      * given a jaxrs class model & java method , construct a jaxrs model associated with
      * method , reading the method level annotations.
      * @param classModel
      * @param serviceMethod
      * @return
      */
    public static JAXRSModel getMethodModel(JAXRSModel classModel, Method serviceMethod) {
        JAXRSModel model=new JAXRSModel();
        addProducesToMethodModel(classModel,model);
        addConsumesToMethodModel(classModel,model);
        addPathToMethodModel(classModel,model);
        Annotation[] annotation=serviceMethod.getAnnotations();
        for(Annotation a:annotation){
           if(a!=null){
               if(a instanceof Produces){
                    addProducesToMethodModel((Produces)a,model);
               }else if(a instanceof Consumes){
                  addConsumesToMethodModel((Consumes)a,model);
               }else if(a instanceof Path){
                  addPathToMethodModel((Path)a,model);
               } else{
                  addHTTPMethodToMethodModel(a,model);
               }

           }
        }
        return model;
    }


    /**
      * add value of the produces annotation to the given jaxrs-class model.method breaks the
      * input String & convert it to a String[] before adding.
      * @param produces
      * @param classModel
      */
    private static void addProducesToClassModel(Produces produces, JAXRSModel classModel) {


        String[] array = null;
        String value = null;
        array = produces.value();
        for (String s : array) {
            if (value == null) {
                value = s;
            } else {
                value = value + "," + s;
            }
        }

        classModel.setProduces(value);

    }


    /**
      * add value of the consumes annotaiton to the given jaxrs-class model. breaks the input String
      * & convert it to a string[] before adding.
      * @param consumes
      * @param classModel
      */
    private static void addConsumesToClassModel(Consumes consumes, JAXRSModel classModel) {


        String[] array = null;
        String value = null;
        array = consumes.value();
        for (String s : array) {
            if (value == null) {
                value = s;
            } else {
                value = value + "," + s;
            }
        }
        classModel.setConsumes(value);

    }


    /**
      * adding value of the path annotation to the class model
      * @param path
      * @param classModel
      */
    private static void addPathToClassModel(Path path, JAXRSModel classModel) {


        String value = null;
        value = path.value();
        if(value!=null){
            if(value.startsWith("/")){
                value=value.substring(1);
            }
            if(value.endsWith("/")){
               value= value.substring(0,(value.length()-1));
            }
        }

        classModel.setPath(value);

    }
   private static void addProducesToMethodModel(Produces produces,JAXRSModel methodModel){

       String value = null;
       for (String s : produces.value()) {
           if (value != null) {
               value = value + s;
           } else {
               value = s;
           }
           methodModel.setProduces(value);
       }

   }

   private static void addProducesToMethodModel(JAXRSModel classModel,JAXRSModel methodModel){
           String value=classModel.getProduces();
       if(value!=null){
           methodModel.setProduces(value);
       }
   }



    private static void addConsumesToMethodModel(Consumes consumes,JAXRSModel methodModel){
        String value = null;
        for (String s : consumes.value()) {
            if (value != null) {
                value = value + s;
            } else {
                value = s;
            }
            methodModel.setConsumes(value);
        }

    }



    /**
      * copies class level Consumes value to method level model
      * @param classModel
      * @param methodModel
      */
   private static void addConsumesToMethodModel(JAXRSModel classModel,JAXRSModel methodModel){
         String value=classModel.getConsumes();
       if(value!=null){
           methodModel.setConsumes(value);
       }
   }


    /**
     * add value of the HTTPMethod to the jaxrs-method model. axis2 only supports POST,GET,PUT,DELETE.
      * it doesnt support HEAD. if HEAD is given it resolves to the default method (POST)
      * @param annotation
      * @param methodModel
      */

    private static void addHTTPMethodToMethodModel(Annotation annotation,JAXRSModel methodModel){


            if (annotation instanceof POST) {
                methodModel.setHTTPMethod(Constants.Configuration.HTTP_METHOD_POST);
            } else if (annotation instanceof GET) {
                methodModel.setHTTPMethod(Constants.Configuration.HTTP_METHOD_GET);
            } else if (annotation instanceof PUT) {
               methodModel.setHTTPMethod(Constants.Configuration.HTTP_METHOD_PUT);
            } else if (annotation instanceof DELETE) {
                methodModel.setHTTPMethod(Constants.Configuration.HTTP_METHOD_DELETE);
            }  else if (annotation instanceof HEAD) {
                 log.warn("HTTP Method HEAD is not supported by AXIS2");
            }

    }

    /**
      * add http location to jaxrs method model. if service level location is already available
      * it concatanates two.
      * @param path
      * @param methodModel
      */
  private static void addPathToMethodModel(Path path,JAXRSModel methodModel){
          String value = path.value();
            if(value!=null){
            if(value.startsWith("/")){
                value=value.substring(1);
            }
            if(value.endsWith("/")){
               value= value.substring(0,(value.length()-1));
            }
        }
            if (methodModel.getPath()!=null ) {

                     methodModel.setPath(methodModel.getPath() +"/"+ value);
            } else{

                methodModel.setPath(value);
            }
  }


    /**
      * this copies the class service level path to method level
      * @param classModel
      * @param methodModel
      */
  private static  void addPathToMethodModel(JAXRSModel classModel,JAXRSModel methodModel){
       String value=classModel.getPath();
      if(value!=null){
          methodModel.setPath(value);
      }
  }

}
