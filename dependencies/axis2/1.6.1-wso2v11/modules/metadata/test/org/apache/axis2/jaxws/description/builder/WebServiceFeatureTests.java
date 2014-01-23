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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


/**
 * Validate setting WebServiceFeatures on client-side DBC objects.
 */
public class WebServiceFeatureTests extends TestCase {
    
    public void testSetFeaturePropertyOnDBC() {
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        sparseComposite.getProperties().put(MDQConstants.SEI_FEATURES_MAP, null);
        assertNull("Property could not be retreived", 
                sparseComposite.getProperties().get(MDQConstants.SEI_FEATURES_MAP));
    }

    public void testSetFeatureValueOnDBC() {
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        Map<String, List<Annotation>> map = new HashMap();

        ArrayList<Annotation> wsFeatures1 = new ArrayList<Annotation>();
        Annotation seiFeature1 = new MTOMAnnot();
        wsFeatures1.add(seiFeature1);
        Annotation seiFeature2 = new RespectBindingAnnot();
        wsFeatures1.add(seiFeature2);
        Annotation seiFeature3 = new AddressingAnnot();
        wsFeatures1.add(seiFeature3);
        map.put("sei1", wsFeatures1);

        ArrayList<Annotation> wsFeatures2 = new ArrayList<Annotation>();
        Annotation sei2Feature1 = new RespectBindingAnnot();
        wsFeatures2.add(sei2Feature1);
        map.put("sei2", wsFeatures2);
        
        sparseComposite.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        
        // Validate we got back out what we put in
        
        Map<String, List<Annotation>> checkMap = (Map<String, List<Annotation>>)
            sparseComposite.getProperties().get(MDQConstants.SEI_FEATURES_MAP);
        
        assertTrue(checkMap.containsKey("sei1"));
        assertTrue(checkMap.containsKey("sei2"));
        
        ArrayList<Annotation> checkSEI1 = (ArrayList<Annotation>) checkMap.get("sei1");
        assertEquals("Wrong number of WS Features", 3, checkSEI1.size());
        assertTrue(checkSEI1.contains(seiFeature1));
        assertTrue(checkSEI1.contains(seiFeature2));
        assertTrue(checkSEI1.contains(seiFeature3));
        
        ArrayList<Annotation> checkSEI2 = (ArrayList<Annotation>) checkMap.get("sei2");
        assertEquals("Wrong number of WS Features", 1, checkSEI2.size());
    }
}
