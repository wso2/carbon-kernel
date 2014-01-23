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

package org.apache.axis2.policy.model;

import org.apache.neethi.Assertion;

/**
 * 
 * This abstract class specifies the common features of a MTOM assertion. 
 *
 */
public abstract class MTOMAssertion implements Assertion {

	/** Specifies if the MTOM assertion is optional. The request can be MTOMised or non-MTOMised,</br>
	 *  but the response will be MTOMised only if request is MTOMised. */
    protected boolean optional = false;
    
    /**
     * Checks if the MTOM assertion is optional. The request can be MTOMised or non-MTOMised,</br>
	 * but the response will be MTOMised only if request is MTOMised.
	 * 
	 * @return <code>true</code> if the MTOM assertion is optional, otherwise returns <code>false</code>.
     */
    public boolean isOptional() {
        return optional;
    }
    
    /**
     * Sets the <code>optional</code> parameter.  
     * 
     * @param isOptional sets if the MTOM assertion is optional or not. If set to <code>true</code> </br>
     * then if the request is MTOMised then the response should be MTOMised, too.
     */
    public void setOptional(boolean isOptional) {
        this.optional = isOptional;
    }

}
	