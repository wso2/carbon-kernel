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

package org.apache.axis2.context.externalize;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectStreamConstants;

/**
 * DebugObjectInput delegates to an ObjectInput object.
 * Each method logs in/out trace information.
 */
public class DebugObjectInput implements ObjectInput, ObjectStreamConstants {
    
    private static final Log log = LogFactory.getLog(DebugObjectInput.class);
    private static final boolean isDebug = log.isDebugEnabled();
    ObjectInput oi; // delegate

    public DebugObjectInput(ObjectInput oi) {
        super();
        this.oi = oi;
    }

    public int available() throws IOException {
        trace("start available()");
        int value = oi.available();
        trace("end available() =" + value);
        return value;
    }

    public void close() throws IOException {
        trace("start close()");
        oi.close();
        trace("end close()");
    }

    public int read() throws IOException {
        trace("start read()");
        int value = oi.read();
        trace("end read()=" + value);
        return value;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        trace("start read(b, off, len) off=" + off + " len="+len);
        int value = oi.read(b, off, len);
        trace("end read(b,off,len)=" + value);
        return value;
    }

    public int read(byte[] b) throws IOException {
        trace("start read(b) b.length=" + b.length);
        int value = oi.read(b);
        trace("end read(b)=" + value);
        return value;
    }

    public boolean readBoolean() throws IOException {
        trace("start readBoolean()");
        boolean value = oi.readBoolean();
        trace("end readBoolean()=" + value);
        return value;
    }

    public byte readByte() throws IOException {
        trace("start readByte");
        byte value = oi.readByte();
        trace("end readByte()=" + value);
        return value;
    }

    public char readChar() throws IOException {
        trace("start readChar");
        char value = oi.readChar();
        trace("end readChar()=" + value);
        return value;
    }

    public double readDouble() throws IOException {
        trace("start readDouble");
        double value = oi.readDouble();
        trace("end readDouble()=" + value);
        return value;
    }

    public float readFloat() throws IOException {
        trace("start readFloat");
        float value = oi.readFloat();
        trace("end readFloat()=" + value);
        return value;
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        trace("start readFully(b,off,len) off=" + off + " len="+len);
        oi.readFully(b, off, len);
        trace("end readFully(b,off,len)");
    }

    public void readFully(byte[] b) throws IOException {
        trace("start readFully(b) b.length="+ b.length);
        oi.readFully(b);
        trace("end readFully(b)");
    }

    public int readInt() throws IOException {
        trace("start readInt()");
        int value = oi.readInt();
        trace("end readInt()="+ value);
        return value;
    }

    public String readLine() throws IOException {
        trace("start readLine()");
        String value = oi.readLine();
        trace("end readLine()="+ value);
        return value;
    }

    public long readLong() throws IOException {
        trace("start readLong()");
        long value = oi.readLong();
        trace("end readLong()="+ value);
        return value;
    }

    public Object readObject() throws ClassNotFoundException, IOException {
        trace("start readObject()");
        Object value = oi.readObject();
        
        trace("end readObject()="+ valueName(value));
        return value;
    }

    public short readShort() throws IOException {
        trace("start readShort()");
        short value = oi.readShort();
        trace("end readShort()="+ value);
        return value;
    }

    public int readUnsignedByte() throws IOException {
        trace("start readLong()");
        int value = oi.readUnsignedByte();
        trace("end readUnsignedByte()="+ value);
        return value;
    }

    public int readUnsignedShort() throws IOException {
        trace("start readShort()");
        int value = oi.readUnsignedShort();
        trace("end readShort()="+ value);
        return value;
    }

    public String readUTF() throws IOException {
        trace("start readUTF()");
        String value = oi.readUTF();
        trace("end readUTF()="+ value);
        return value;
    }

    public long skip(long n) throws IOException {
        trace("start skip(n) n="+n);
        long value = oi.skip(n);
        trace("end skip(n)="+ value);
        return value;
    }

    public int skipBytes(int n) throws IOException {
        trace("start skipBytes(n) n="+n);
        int value = oi.skipBytes(n);
        trace("end skipBytes(n)="+ value);
        return value;
    }
    
    public void trace(String str) {
        if (isDebug) {
            log.debug(str);
        }
    }
    
    private String valueName(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return (String) obj;
        } else {
            return "Object of class = " + obj.getClass().getName();
        }
    }
}
