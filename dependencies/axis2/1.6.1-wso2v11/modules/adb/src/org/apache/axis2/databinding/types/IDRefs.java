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

package org.apache.axis2.databinding.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Custom class for supporting XSD data type IDRefs
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#IDREFS">XML Schema 3.3.10 IDREFS</a>
 */
public class IDRefs extends NCName {

    private static final long serialVersionUID = 5394097798361075902L;

    private IDRef[] idrefs;

    public IDRefs() {
        super();
    }

    /**
     * ctor for IDRefs
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public IDRefs(String stValue) throws IllegalArgumentException {
        setValue(stValue);
    }

    public void setValue(String stValue) {
        StringTokenizer tokenizer = new StringTokenizer(stValue);
        int count = tokenizer.countTokens();
        idrefs = new IDRef[count];
        for (int i = 0; i < count; i++) {
            idrefs[i] = new IDRef(tokenizer.nextToken());
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < idrefs.length; i++) {
            IDRef ref = idrefs[i];
            if (i > 0) buf.append(" ");
            buf.append(ref.toString());
        }
        return buf.toString();
    }

    /**
     * IDREFs can be equal without having identical ordering because they represent a set of
     * references.  Hence we have to compare values here as a set, not a list.
     *
     * @param object an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;        // succeed quickly, when possible
        }
        if (object instanceof IDRefs) {
            IDRefs that = (IDRefs)object;
            if (this.idrefs.length == that.idrefs.length) {
                Set ourSet = new HashSet(Arrays.asList(this.idrefs));
                Set theirSet = new HashSet(Arrays.asList(that.idrefs));
                return ourSet.equals(theirSet);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns the sum of the hashcodes of the underlying idrefs, an operation which is not
     * sensitive to ordering.
     *
     * @return an <code>int</code> value
     */
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < idrefs.length; i++) {
            hash += idrefs[i].hashCode();
        }
        return hash;
    }
}
