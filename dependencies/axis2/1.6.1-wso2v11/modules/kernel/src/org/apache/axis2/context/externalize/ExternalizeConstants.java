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

package org.apache.axis2.context.externalize;

/**
 * Common Externalize Constants
 */
public interface ExternalizeConstants {
    
    // Objects are preceeded by a bit indicating if the object is empty or active
    public static final boolean EMPTY_OBJECT = false;  // null object or empty object
    public static final boolean ACTIVE_OBJECT = true;  // otherwise it is an active object
    
    // used to indicate the end of a list
    public static String LAST_ENTRY = "LAST_OBJ";

    // used to indicate an "empty" object
    public static String EMPTY_MARKER = "EMPTY_OBJ";
    
    //  message/trace/logging strings
    public static final String UNSUPPORTED_SUID = "Serialization version ID is not supported.";
    public static final String UNSUPPORTED_REVID = "Revision ID is not supported.";

    public static final String OBJ_SAVE_PROBLEM =
            "The object could not be saved to the output stream.  The object may or may not be important for processing the message when it is restored. Look at how the object is to be used during message processing.";
    public static final String OBJ_RESTORE_PROBLEM =
            "The object could not be restored from the input stream.  The object may or may not be important for processing the message when it is restored. Look at how the object is to be used during message processing.";

}
