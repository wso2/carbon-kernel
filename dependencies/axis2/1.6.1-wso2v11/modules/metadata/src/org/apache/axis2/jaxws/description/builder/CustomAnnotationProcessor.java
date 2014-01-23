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

package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.jaxws.description.EndpointDescription;

/**
 * This interface represents processors that will be used to 
 * handle CustomAnnotationInstances that are added to the MDQ layer 
 * by the uaser. These processors will also be registered with 
 * MDQ, and they will be called by MDQ as needed. 
 *
 */
public interface CustomAnnotationProcessor {
    
    /**
     * This method sets the fully qualifed name of the 
     * CustomAnnotationInstance class that this processor
     * is responsible for handling.
     */
    public void setAnnotationInstanceClassName(String annotationInstanceClassName);
    
    /**
     * This method returns the fully qualifed name of the 
     * CustomAnnotationInstance class that this processor
     * is responsible for handling.
     */
    public String getAnnotationInstanceClassName();
    
    /**
     * This method will be called to process an annotation type
     * recognized by this processor that was found at the type
     * level.
     * @param ed - EndpointDescription that the annotation was associated with
     * @param annotation - The CustomAnnotationInstance that should be processed
     * by this processor
     */
    public void processTypeLevelAnnotation(EndpointDescription ed, CustomAnnotationInstance annotation);

}
