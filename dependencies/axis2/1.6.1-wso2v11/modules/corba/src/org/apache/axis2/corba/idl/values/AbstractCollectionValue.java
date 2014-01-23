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

package org.apache.axis2.corba.idl.values;

import org.apache.axis2.corba.idl.types.AbstractCollectionType;
import org.apache.axis2.corba.idl.types.CompositeDataType;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public abstract class AbstractCollectionValue extends AbstractValue {
    protected Object[] values = null;

    public AbstractCollectionValue(CompositeDataType dataType) {
        super(dataType);
    }
    
    public AbstractCollectionValue(AbstractCollectionType dataType) {
        super(dataType);
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public abstract void read(InputStream inputStream);

    public abstract void write(OutputStream outputStream);

    public String toString() {
        AbstractCollectionType collectionType = (AbstractCollectionType) dataType;
        String type = null;
        if (collectionType.isArray())
            type = "Array: ";
        else if (collectionType.isSequence())
            type = "Sequence: ";
        return type + collection2String(values);
    }

    private String collection2String(Object[] collectionValues) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < collectionValues.length; i++) {
            Object value = collectionValues[i];
            String elem;
            if (value instanceof Object[]) {
                elem = collection2String((Object[]) value);
            } else {
                if (value != null) {
                    elem = value.toString();
                } else {
                    elem = "null";
                }
            }
            str.append(", ");
            str.append(elem);
        }
        return "{" + str.substring(2) + "}";
    }
}
