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

package org.apache.axis2.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SelfManagedDataHolder implements Externalizable {

    private transient String classname;
    private transient String id;
    private transient byte[] data;

    public SelfManagedDataHolder() {
        // should only be used by the ObjectStateUtils
    }

    // TODO better exception
    public SelfManagedDataHolder(String classname, String id, byte[] data) throws Exception {
        if ((classname == null)
                || (id == null)) {
            throw new Exception(
                    "Argument cannot be null: classname = " + classname + ", id = " + id);
        }
        this.classname = classname;
        this.id = id;

        // TODO deep copy necessary?
        this.data = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }


    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        classname = (String) in.readUTF();
        id = (String) in.readUTF();
        int datalength = in.readInt();
        data = new byte[datalength];
        in.read(data);

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(classname);
        out.writeUTF(id);
        out.writeInt(data.length);
        out.write(data);
    }


    public String getClassname() {
        return classname;
    }

    public byte[] getData() {
        return data;
    }

    public String getId() {
        return id;
    }

}
