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
import org.apache.axis2.corba.idl.types.DataType;
import org.apache.axis2.corba.idl.types.SequenceType;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class SequenceValue extends AbstractCollectionValue {
    public SequenceValue(SequenceType sequenceType) {
        super(sequenceType);
    }

    public void read(InputStream inputStream) {
        AbstractCollectionType collectionType = (AbstractCollectionType) dataType;
        DataType memberType = collectionType.getDataType();
        int length = inputStream.read_long();
        values = new Object[length];
        for (int i = 0; i < length; i++) {
            values[i] = read(memberType, inputStream);
        }
    }

    public void write(OutputStream outputStream) {
        AbstractCollectionType collectionType = (AbstractCollectionType) dataType;
        DataType memberType = collectionType.getDataType();
        int length = values.length;
        outputStream.write_long(length);
        for (int i = 0; i < length; i++) {
            write(values[i], memberType, outputStream);
        }
    }
}
