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

package org.apache.axis2.scripting.convertors;

import org.apache.axiom.om.OMElement;
import org.apache.bsf.BSFEngine;

/**
 * The OMElementConvertor interface enables customizing the conversion of 
 * XML between Synapse and a script language. Some script languages have their
 * own ways of using XML, such as E4X in JavaScript or REXML in Ruby. But BSF
 * has no support for those so Synapse needs to handle this itself, which is what
 * the OMElementConvertor does.
 * 
 * Which OMElementConvertor type to use is discovered based on the file name suffix of 
 * the mediator script. The suffix is converted to uppercase and used as the prefix to 
 * the OMElementConvertor classname. For example, with a JavaScript script named myscript.js
 * the .js suffix is taken to make the convertor class name 
 * "org.apache.synapse.mediators.bsf.convertors.JSOMElementConvertor"
 * If the convertor class is not found then a default convertor is used which converts
 * XML to a String representation.
 */
public interface OMElementConvertor {
    
    public void setEngine(BSFEngine e);
    public Object toScript(OMElement omElement);
    
    public OMElement fromScript(Object o);

}
