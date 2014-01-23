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
package org.apache.axis2.jaxws.description.builder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 */
public class PortCompositeTests extends TestCase {
    
    /**
     * Validate that the toString method of the superclass DBC is called by the 
     * PortComposite.  The WebService Features are output in the superclass, so verify
     * that information is in the returned string.
     */
    public void testBasicPortComposite_toString() {
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        List<Annotation> wsFeatureList = new ArrayList<Annotation>();
        wsFeatureList.add(new AddressingAnnot());
        wsFeatureList.add(new MTOMAnnot());
        wsFeatureList.add(new RespectBindingAnnot());
        
        PortComposite pc = new PortComposite(dbc);
        pc.setWebServiceFeatures(wsFeatureList);
        
        String string = pc.toString();
        
        assertNotNull(string);
        assertTrue("Does not contain Features", string.contains("WebService Feature Objects (as annotations):"));
        assertTrue("Does not contain AddressingAnnot", string.contains("AddressingAnnot"));
        assertTrue("Does not contain MTOMAnnot", string.contains("MTOMAnnot"));
        assertTrue("Does not contain RespectBindingAnnot", string.contains("RespectBindingAnnot"));
    }

}
