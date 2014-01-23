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

package org.apache.axis2.wsdl.util;


import javax.wsdl.Definition;


/**
 * This interface provides support for processing a WSDL4J definition
 * with a lower memory footprint.  This is useful for certain
 * environments.
 */
public interface WSDLWrapperImpl extends Definition {

    /**
     * Returns the WSDL4J Definition object that is being wrapped
     */
    public Definition getUnwrappedDefinition();


    /**
     * Sets the WSDL4J Definition object that is being wrapped
     *
     * @param d  the WSDL4J Definition object
     */
    public void setDefinitionToWrap(Definition d);


    /**
     * Sets the location for the WSDL4J Definition object that is being wrapped
     */
    public void setWSDLLocation(String uriLocation);


    /**
     * Gets the location for the WSDL4J Definition object that is being wrapped
     */
    public String getWSDLLocation();


    /*
     * Release resources associated with the WSDL4J Definition object that is
     * being wrapped.
     */
    public void releaseResources();


    /**
     * Closes the use of the wrapper implementation and allows 
     * internal resources to be released.
     */
    public void close();

}
