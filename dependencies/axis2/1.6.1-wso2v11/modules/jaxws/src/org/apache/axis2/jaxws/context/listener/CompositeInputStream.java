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

package org.apache.axis2.jaxws.context.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * CompositeInputStream has ability to store multiple inputStreams in a LinkList
 * and perform various input stream operaitons on these inputStream in a serialized 
 * manner or first in first read model.
 */
public class CompositeInputStream extends InputStream {
    private static final Log log = 
        LogFactory.getLog(CompositeInputStream.class);
    private LinkedList<InputStream> compositeIS = new LinkedList<InputStream>();
    //pointer to current node on Link list
    private InputStream curr = null;
   
    public CompositeInputStream() {
        //do nothing
    }

    public CompositeInputStream(InputStream is){
        append(is);
    }

    public CompositeInputStream(InputStream[] isArray){
        for (InputStream is:isArray){
            append(is);
        }
    }

    
    public int read() throws IOException {
        
        // Note that reading is destructive.  
        // The InputStreans are released when read.
        int count = -1;
        if(curr !=null){
            count = curr.read();
        }
        //if we read all the bits from current and there are more InputStreams to read.
        if(count == -1 && compositeIS.size()>0){
            curr.close();
            //release for GC
            curr =null;
            curr = compositeIS.removeFirst();
            count = curr.read();
        }
        return count;
    }

    public void append(InputStream is){
        compositeIS.addLast(is);
        if(curr == null){
            curr = compositeIS.removeFirst();
        }
    }

    public int available() throws IOException {
        
        int available= 0;
        if (curr != null) {
            available = curr.available();
        }
        if (compositeIS != null) {
            for(InputStream is:compositeIS){
                if (is != null) {
                    available+=is.available();
                }
            }
        }
        return available;
    }

    public void close() throws IOException {
        if(curr!=null){
            try {
                curr.close();
                curr = null;
            } catch (IOException e) {
                // TODO swallow so that other streams can be closed
            }
        }
        if (compositeIS != null) {
            for(InputStream is:compositeIS){
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO swallow so that other streams can be closed
                }
            }
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {  
        
        // Read from the current buffer
        int count = -1;
        if(curr !=null){
            count = curr.read(b, off, len);
        }
        
        // If more bytes are needed, then access the next stream
        // And recursively call read to get more data.
        if(count < len && compositeIS.size()>0){
            curr.close();
            //release for GC
            curr =null;
            curr = compositeIS.removeFirst();
            
            // Recursive call to read the next buffer
            int numRead = count <= 0 ? 0 : count;
            int count2 = read(b, off+numRead, len-numRead);
            
            // Calculate the total count
            if (count2 == -1 && count == -1 ) {
                // All buffers are empty.
                count = -1;
            } else if (count2 == -1) {
                // subsequent buffers are all empty, return numRead
                count = numRead;
            } else{
                // normal case
                count = count2 + numRead;
            }
        }
        return count;
    }
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

}
