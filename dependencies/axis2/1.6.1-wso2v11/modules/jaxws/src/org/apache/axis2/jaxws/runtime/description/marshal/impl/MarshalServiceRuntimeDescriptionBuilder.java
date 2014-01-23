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

package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.utility.PropertyDescriptorPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class MarshalServiceRuntimeDescriptionBuilder {

    private static Log log = LogFactory.getLog(MarshalServiceRuntimeDescriptionBuilder.class);

    /** Intentionally Private */
    private MarshalServiceRuntimeDescriptionBuilder() {
    }

    /**
     * create
     *
     * @param opDesc
     * @param implClassName
     * @return
     */
    static public MarshalServiceRuntimeDescription create(ServiceDescription serviceDesc) {
        MarshalServiceRuntimeDescriptionImpl desc =
                new MarshalServiceRuntimeDescriptionImpl(getKey(), serviceDesc);
        init(desc, serviceDesc);
        return desc;
    }

    static public String getKey() {
        return "JAXWS-MARSHAL";
    }

    /**
     * @param implClass
     * @return true if Field or Method has a @Resource annotation
     */
    static private void init(MarshalServiceRuntimeDescriptionImpl marshalDesc,
                             ServiceDescription serviceDesc) {
    	
    	if (log.isDebugEnabled()) {
    		log.debug("start init");
    	}

    	if (log.isDebugEnabled()) {
    		log.debug("Discover the artifacts");
    	}
        // Artifact class discovery/builder
        ArtifactProcessor artifactProcessor = new ArtifactProcessor(serviceDesc);
        try {
            artifactProcessor.build();
        } catch (Throwable t) {
            throw ExceptionFactory.makeWebServiceException(t);
        }
        marshalDesc.setRequestWrapperMap(artifactProcessor.getRequestWrapperMap());
        marshalDesc.setResponseWrapperMap(artifactProcessor.getResponseWrapperMap());
        marshalDesc.setFaultBeanDescMap(artifactProcessor.getFaultBeanDescMap());
        marshalDesc.setMethodMap(artifactProcessor.getMethodMap());

        if (log.isDebugEnabled()) {
    		log.debug("Build the annotations map");
    	}
        // Build the annotation map
        Map<String, AnnotationDesc> map;
        try {
            map = AnnotationBuilder.getAnnotationDescs(serviceDesc, artifactProcessor);
        } catch (Throwable t) {
            // Since we are building a cache, proceed without exception
            if (log.isDebugEnabled()) {
                log.debug(
                        "Exception occurred during cache processing.  This will impact performance:" +
                                t);
            }
            map = new HashMap<String, AnnotationDesc>();
        }
        marshalDesc.setAnnotationMap(map);

       
        if (log.isDebugEnabled()) {
    		log.debug("Build the property descriptor cache");
    	}
        // Build the property descriptor map
        Map<Class, Map<String, PropertyDescriptorPlus>> cache;
        try {
            cache = PropertyDescriptorMapBuilder
                    .getPropertyDescMaps(serviceDesc, artifactProcessor);
        } catch (Throwable t) {
            // Since we are building a cache, proceed without exception
            if (log.isDebugEnabled()) {
                log.debug(
                        "Exception occurred during cache processing.  This will impact performance:" +
                                t);
            }
            cache = new HashMap<Class, Map<String, PropertyDescriptorPlus>>();
        }
        marshalDesc.setPropertyDescriptorMapCache(cache);

        // @TODO There are two ways to get the packages.
        // Schema Walk (prefered) and Annotation Walk.
        // The Schema walk requires an existing or generated schema.
        // 
        // There are some limitations in the current schema walk
        // And there are problems in the annotation walk.
        // So for now we will do both.
        TreeSet<String> packages = new TreeSet<String>();
        boolean doSchemaWalk = true;
        boolean doAnnotationWalk = true;
        packages = new TreeSet<String>();
        if (doSchemaWalk) {
        	if (log.isDebugEnabled()) {
        		log.debug("Get the packages using the schema");
        	}
            packages.addAll(PackageSetBuilder.getPackagesFromSchema(serviceDesc));
        }
        if (doAnnotationWalk) {
            // Get the package names from the annotations.  Use the annotation map to reduce Annotation introspection
        	if (log.isDebugEnabled()) {
        		log.debug("Get the packages using the class annotations");
        	}
            packages.addAll(PackageSetBuilder.getPackagesFromAnnotations(serviceDesc, marshalDesc));
        }
        marshalDesc.setPackages(packages);
        
        if (log.isDebugEnabled()) {
        	log.debug("MarshalDesc = " + marshalDesc);
    		log.debug("end init");
    	}
    }
}