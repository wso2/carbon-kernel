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

package org.apache.axis2.jaxws.core;

import junit.framework.TestCase;

import java.util.Map;

/*
 * Testing jaxws message context's interation with axis2 message context.
 * JAXWS delegates all property setting/getting up to axis2, but we need to 
 * be careful how we use axis2's MC.  We should not have access to the options
 * bag in the axis2 MC, for example.
 */

public class MessageContextTests extends TestCase {

    static final String key1 = "ONaxisMC";
    static final String key2 = "ONaxisMCOptions";
    static final String key3 = "ONjaxwsMC";
    
    /* TODO:
     * should also test to make sure service or operation context properties
     * on the axis2 MC are not accessible.  That's probably best left for another test.
     */
    public void testMessageContextPropertiesAccessibility() throws Exception {
        org.apache.axis2.context.MessageContext axisMC = new org.apache.axis2.context.MessageContext();
        MessageContext jaxwsMC = new MessageContext(axisMC);
        axisMC.setProperty(key1, "value");
        axisMC.getOptions().setProperty(key2, "value");
        jaxwsMC.setProperty(key3, "value");
        
        assertNotNull(jaxwsMC.getProperty(key1));
        assertNull(jaxwsMC.getProperty(key2));
        assertNotNull(jaxwsMC.getProperty(key3));
        
        Map props = jaxwsMC.getProperties();
        
        assertNotNull(props.get(key1));
        assertNull(props.get(key2));
        assertNotNull(props.get(key3));
    }
    
}
