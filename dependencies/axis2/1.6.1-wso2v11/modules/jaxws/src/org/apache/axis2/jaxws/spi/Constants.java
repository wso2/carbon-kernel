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

package org.apache.axis2.jaxws.spi;

/** JAXWS SPI Constants */
public class Constants {

    // ----------------------------
    // MessageContext Property Keys
    // ----------------------------

    // Value = Boolean
    // Usage: Setting this property to true will cause the entire request message
    //   to be saved and restored. A reliable messaging inbound handler should set 
    //   this flag if the entire message should be saved.  Setting this flag will substantially
    //   degrade performance.
    //
    //   The default is to not save the entire message.  After server dispatch processing, the 
    //   body of the request message will not be available.  This is acceptable in most scenarios.
    //
    // REVIEW Only honored on the server: Saved before inbound application handler processing and
    //   restored after outbound application handler processing.
    //
    public static final String SAVE_REQUEST_MSG = "org.apache.axis2.jaxws.spi.SAVE_REQUEST_MSG";

    // Value = String
    // Usage: Value of saved request
    //
    public static final String SAVED_REQUEST_MSG_TEXT =
            "org.apache.axis2.jaxws.spi.SAVED_REQUEST_MSG_TEXT";

    // Value = Collection
    // Usage: A list of ApplicationContextMigrator objects that are to be called for an invocation.
    public static final String APPLICATION_CONTEXT_MIGRATOR_LIST_ID =
            "org.apache.axis2.jaxws.spi.ApplicationContextMigrators";
    
    // Value = ClassLoader
    // Usage: Stores ClassLoader instance on response message context that ensures the 
    // JAXBUtils class will use the same ClassLoader to retrieve a JAXBContext as the
    // one that was used to create the request
    public static final String CACHE_CLASSLOADER = "CACHE_CLASSLOADER";
    
    // Value = List
    // Usage: Store list of InvocationListener instances for a given request/response
    public static final String INVOCATION_LISTENER_LIST = 
        "org.apache.axis2.jaxws.spi.INVOCATION_LISTENER_LIST";
    
    // Value = Throwable
    // Usage: Store Throwable type that should be used when constructing message to be
    // sent back to the client
    public static final String MAPPED_EXCEPTION = "org.apache.axis2.jaxws.spi.MAPPED_EXCEPTION";
    
    /** Intentionally Private */
    private Constants() {
    }

}
