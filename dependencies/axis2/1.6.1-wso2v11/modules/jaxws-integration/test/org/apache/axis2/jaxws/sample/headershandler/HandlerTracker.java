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

package org.apache.axis2.jaxws.sample.headershandler;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HandlerTracker {

    private static final String filelogname = "target/HeadersHandlerTests.log";
    
    private String classname;
    
    private static final String CLOSE = "CLOSE";
    private static final String GET_HEADERS = "GET_HEADERS";
    private static final String HANDLE_FAULT_INBOUND = "HANDLE_FAULT_INBOUND";
    private static final String HANDLE_MESSAGE_INBOUND = "HANDLE_MESSAGE_INBOUND";
    private static final String HANDLE_FAULT_OUTBOUND = "HANDLE_FAULT_OUTBOUND";
    private static final String HANDLE_MESSAGE_OUTBOUND = "HANDLE_MESSAGE_OUTBOUND";
    private static final String POST_CONSTRUCT = "POST_CONSTRUCT";
    private static final String PRE_DESTROY = "PRE_DESTROY";
    private static final String ADDED_HEADER = "ADDED_HEADER";
    private static final String CHECKED_HEADER = "CHECKED_HEADER";
    private static final String REMOVED_HEADER = "REMOVED_HEADER";
    
    // should pass classname for "name"
    public HandlerTracker(String name) {
        classname = name;
    }
    
    public void postConstruct() {
        log_to_file(POST_CONSTRUCT);
    }
    
    public void preDestroy() {
        log_to_file(PRE_DESTROY);
    }
    
    public void close() {
        log_to_file(CLOSE);
    }

    public void getHeaders() {
        log_to_file(GET_HEADERS);
    }
    
    public void addHeader(String xmlHeader) {
    	log_to_file(ADDED_HEADER + " " + xmlHeader);
    }
    
    public void checkHeader(String xmlHeader) {
    	log_to_file(CHECKED_HEADER + " " + xmlHeader);
    }
    
    public void removedHeader(String xmlHeader) {
    	log_to_file(REMOVED_HEADER + " " + xmlHeader);
    }
    
    public void handleFault(boolean outbound) {
        if (outbound) {
            log_to_file(HANDLE_FAULT_OUTBOUND);
        } else {
            log_to_file(HANDLE_FAULT_INBOUND);
        }
    }

    public void handleMessage(boolean outbound) {
        if (outbound) {
            log_to_file(HANDLE_MESSAGE_OUTBOUND);
        } else {
            log_to_file(HANDLE_MESSAGE_INBOUND);
        }
    }
    
    public void log(String msg, boolean outbound) {
        log_to_file(msg + " " + (outbound ? "OUTBOUND" : "INBOUND"));
    }
    
    /*
     * we have to open and close the file every time we write
     * as other handler method calls may be interleaved with this one
     */
    private void log_to_file(String msg) {
        try {
            FileWriter fw = new FileWriter(filelogname, true);
            fw.write(classname + " " + msg + "\n");
            fw.flush();
            fw.close();
        } catch (Exception e) {
        }
    }
    
    public void log_exception_to_file(String filename, Throwable e) {
    	try {
    		FileWriter fw = new FileWriter(filename, true);
    		StringWriter sw = new StringWriter();
    	    PrintWriter pw = new PrintWriter(sw);
    	    e.printStackTrace(pw);
    	    fw.write(sw.toString());
    	    fw.flush();
    	    fw.close();
    	} catch (Exception ex) {
    	}
    }
    
    public void log_to_my_file(String filename, String msg) {
    	try {
    		FileWriter fw = new FileWriter(filename, true);
    	    fw.write(msg);
    	    fw.flush();
    	    fw.close();
    	} catch (Exception ex) {
    	}
    }
    
}
