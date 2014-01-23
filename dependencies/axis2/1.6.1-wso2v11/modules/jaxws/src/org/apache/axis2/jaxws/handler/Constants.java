/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.handler;

public class Constants {

    /**
     * This constant is used to decide if we should track a JAX-WS handler's usage
     * of the SOAPHeadersAdapter along with SAAJ.  If this property is set to 'true',
     * then the handler framework will track whether a handler has called any getters/setters/etc
     * on the SOAPHeadersAdapter AND called any API under Message to get SAAJ objects.  Both
     * of these cause transformations of data, and together would be more expensive than
     * if they were separated.
     */
    public static final String JAXWS_HANDLER_TRACKER = "org.apache.axis2.jaxws.handler.tracker";
    
}
