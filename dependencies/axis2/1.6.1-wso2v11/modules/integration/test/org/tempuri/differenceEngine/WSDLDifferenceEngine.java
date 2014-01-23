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

package org.tempuri.differenceEngine;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Attr;

import java.lang.reflect.Method;

/**
 * This class extends the DifferenceEngine class to overwrite the methods that
 * incorrectly fail Diff.similar() for certain equivalent but not identical
 * XML file comparisons.
 */
public class WSDLDifferenceEngine extends DifferenceEngine {
    /**
     * Simple constructor
     * @param controller the instance used to determine whether a Difference
     *  detected by this class should halt further comparison or not
     * @see ComparisonController#haltComparison(Difference)
     */
    public WSDLDifferenceEngine(ComparisonController controller) {
    	super(controller);
    }
        
    /**
     * Compare two attributes to determine if they are equivalent
     * @param control a known attribute against which a test attribute is being compared
     * @param test the generated attribute that is being tested against a known attribute
     * @param listener 
     * @throws DifferenceFoundException if there is a difference detected between
     *  the two attributes
     */
    protected void compareAttribute(Attr control, Attr test,
    DifferenceListener listener) throws DifferenceFoundException {    	
    	// There are no getter methods for these private fields in DifferenceEngine
    	// controlTracker.visited(control);
    	// testTracker.visited(test);
    	
    	compare(control.getPrefix(), test.getPrefix(), control, test, 
    		listener, NAMESPACE_PREFIX);

        compare(getValueWithoutNamespace(control), getValueWithoutNamespace(test), control, test,
            listener, ATTR_VALUE);
        
        compare(getValueNamespaceURI(control), getValueNamespaceURI(test), control, test,
                listener, ATTR_VALUE);        
        
        compare(control.getSpecified() ? Boolean.TRUE : Boolean.FALSE,
            test.getSpecified() ? Boolean.TRUE : Boolean.FALSE,
            control, test, listener, ATTR_VALUE_EXPLICITLY_SPECIFIED);
    }
    
    /**
     * Obtain the value of this attribute with the namespace prefix removed (if present)
     * @param attr the attribute for which the non namespaced value is sought
     * @return the value of this attribute without a namespace prefix
     */
    protected String getValueWithoutNamespace(Attr attr) {
    	String value = attr.getValue();
    	int index = value.indexOf(':');
    	int protocol = value.indexOf("://");
    	
    	if (value == null)
    		return null;
    	
    	if (index == -1 || index == protocol)
    		return value;
    	
    	return value.substring(index + 1);
    }
    
    /**
     * Obtain the namespace URI for the value of this attr (if a namespace prefix is present)
     * @param attr the attribute for which the namespace URI of the value is sought
     * @return the namespace URI of the attribute's value if a namespace prefix is 
     *  present, otherwise return null
     */
    protected String getValueNamespaceURI(Attr attr) {
    	String value = attr.getValue();
    	int index = value.indexOf(':');
    	int protocol = value.indexOf("://");
    	
    	if (value == null || index == -1 || index == protocol)
    		return null;

        try {
            Method m = Attr.class.getMethod("lookupNamespaceURI", new Class[]{String.class});
            return (String) m.invoke(attr, new Object[]{value.substring(0, index)});
        } catch (Exception e) {
            return null;
        }
    }
}
