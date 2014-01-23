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

package org.apache.axiom.om.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.commons.logging.Log;

/**
 * Common Utilities
 */
public class CommonUtils {

    /**
     * Private constructor.  All methods of this class are static.
     */
    private CommonUtils() {}

    /**
     * replace: Like String.replace except that the old and new items are strings.
     *
     * @param name string
     * @param oldT old text to replace
     * @param newT new text to use
     * @return replacement string
     */
    public static final String replace(String name,
                                       String oldT, String newT) {

        if (name == null) return "";

        // Create a string buffer that is twice initial length.
        // This is a good starting point.
        StringBuffer sb = new StringBuffer(name.length() * 2);

        int len = oldT.length();
        try {
            int start = 0;
            int i = name.indexOf(oldT, start);

            while (i >= 0) {
                sb.append(name.substring(start, i));
                sb.append(newT);
                start = i + len;
                i = name.indexOf(oldT, start);
            }
            if (start < name.length())
                sb.append(name.substring(start));
        } catch (NullPointerException e) {
            // No FFDC code needed
        }

        return new String(sb);
    }
    /**
     * Get a string containing the stack of the current location
     *
     * @return String
     */
    public static String callStackToString() {
        return stackToString(new RuntimeException());
    }

    /**
     * Get a string containing the stack of the specified exception
     *
     * @param e
     * @return TODO
     */
    public static String stackToString(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.BufferedWriter bw = new java.io.BufferedWriter(sw);
        java.io.PrintWriter pw = new java.io.PrintWriter(bw);
        e.printStackTrace(pw);
        pw.close();
        String text = sw.getBuffer().toString();
        // Jump past the throwable
        text = text.substring(text.indexOf("at"));
        text = replace(text, "at ", "DEBUG_FRAME = ");
        return text;
    }
    
    /**
     * Writes the om to a log.debug.
     * This method assumes optimized mtom attachments
     * Also calculates the length of the message.
     * @param om OMElement
     * @param log Log
     * @return length of entire message
     */
    public static long logDebug(OMElement om, Log log) {
        return logDebug(om, log, Integer.MAX_VALUE);
    }
    
    /**
     * Writes the om to a log.debug.
     * This method assumes optimized mtom attachments
     * Also calculates the length of the message.
     * @param om OMElement
     * @param log Log
     * @param limit limit of message to write
     * @return length of entire message
     */
    public static long logDebug(OMElement om, Log log, int limit) {
        OMOutputFormat format = new OMOutputFormat();
        format.setDoOptimize(true);
        format.setIgnoreXMLDeclaration(true);
        return logDebug(om, log, limit, format);
    }
    
    /**
     * Writes the om to a log.debug.
     * Also calculates the length of the message.
     * @param om OMElement
     * @param log Log
     * @param limit limit of message to write
     * @param format OMOutputFormat
     * @return length of entire message
     */
    public static long logDebug(OMElement om, 
                             Log log, 
                             int limit,
                             OMOutputFormat format) {
        LogOutputStream logStream = new LogOutputStream(log, limit);
        
        try {
            om.serialize(logStream, format);
            logStream.flush();
            logStream.close();
        } catch (Throwable t) {
            // Problem occur while logging. Log error and continue
            log.debug(t);
            log.error(t);
        }
        
        return logStream.getLength();
    }
    
    /** 
     * A "textual part" has one or more of the following criteria
     *   1) a content-type that start with "text"
     *      "application/xml" or "application/soap"
     *   2) has a charset parameter on the content-type.
     *
     * Example:
     *   An part with a content-type of "image/gif" is a non-textual attachment.
     *   An part with a content-type of "application/soap+xml" is an textual attachment
     *
     * @param contentType
     * @return true if text, false otherwise
     */
    public static boolean isTextualPart(String contentType) {
        String ct = contentType.trim();
        // REVIEW: What about content-type with a type of "message"
        return ct.startsWith("text/") ||
               ct.startsWith("application/soap") ||
               ct.startsWith("application/xml") ||
               ct.indexOf("charset") != -1;
    }
}
