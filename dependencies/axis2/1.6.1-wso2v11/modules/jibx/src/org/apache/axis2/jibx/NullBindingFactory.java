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

package org.apache.axis2.jibx;

import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.impl.BindingFactoryBase;

/**
 * Dummy binding factory for when the generated Axis2 linkage code uses only simple value
 * conversions (and hence doesn't need a real JiBX binding).
 */
public class NullBindingFactory extends BindingFactoryBase implements IBindingFactory {

    private static final String[] EMPTY_ARRAY = new String[0];
    
    public NullBindingFactory() {
        super("null", 0, 0, "", "", "", "", EMPTY_ARRAY, EMPTY_ARRAY, "", "",
            EMPTY_ARRAY, "", "", "", "", "", EMPTY_ARRAY);
    }

    public String getCompilerDistribution() {
        // normally only used by BindingDirectory code, so okay to punt
        return "";
    }

    public int getCompilerVersion() {
        // normally only used by BindingDirectory code, so okay to punt
        return 0;
    }

    public int getTypeIndex(String type) {
        return -1;
    }
}