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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an XMLStreamWriterFilter that removes illegal characters.
 * 
 * Valid and invalid character ranges are defined by:
 * http://www.w3.org/TR/2008/REC-xml-20081126/#NT-Char
 *
 *
 */
public class XMLStreamWriterRemoveIllegalChars extends
		XMLStreamWriterFilterBase {

    private static Log log = LogFactory.getLog(XMLStreamWriterRemoveIllegalChars.class);
    
    public XMLStreamWriterRemoveIllegalChars() {
        super();
        if (log.isDebugEnabled()) {
            log.debug("Creating XMLStreamWriterRemoveIllegalChars object " + this);
        }
    }
    // Characters less than 0x20 may be control characters and should be removed
    // Note the non-initialized bytes in this array are zero
    private static byte[] REMOVE = new byte[32];
    static {
        REMOVE[0x00] = 1;
        REMOVE[0x01] = 1;
        REMOVE[0x02] = 1;
        REMOVE[0x03] = 1;
        REMOVE[0x04] = 1;
        REMOVE[0x05] = 1;
        REMOVE[0x06] = 1;
        REMOVE[0x07] = 1;
        REMOVE[0x08] = 1;
        // 0x09 is TAB...which is allowed
        // 0x0A is LINEFEED...which is allowed
        REMOVE[0x0B] = 1;
        REMOVE[0x0C] = 1;
        // 0x0D is CARRIAGE RETURN, which is allowed
        REMOVE[0x0E] = 1;
        REMOVE[0x0F] = 1;
        REMOVE[0x10] = 1;
        REMOVE[0x11] = 1;
        REMOVE[0x12] = 1;
        REMOVE[0x13] = 1;
        REMOVE[0x14] = 1;
        REMOVE[0x15] = 1;
        REMOVE[0x16] = 1;
        REMOVE[0x17] = 1;
        REMOVE[0x18] = 1;
        REMOVE[0x19] = 1;
        REMOVE[0x1A] = 1;
        REMOVE[0x1B] = 1;
        REMOVE[0x1C] = 1;
        REMOVE[0x1D] = 1;
        REMOVE[0x1E] = 1;
        REMOVE[0x1F] = 1;
    }
    
    // These two characters are not allowed
    private final int FFFE = 0xFFFE;
    private final char FFFF = 0xFFFF;
    
    // Characters in the surrogate range are not allowed
    // (unless the result is a valid supplemental character)
    private final char SURROGATE_START = 0xD800;
    private final char SURROGATE_END =   0xDFFF;
    
    
    /* (non-Javadoc)
     * @see org.apache.axiom.om.util.XMLStreamWriterFilterBase#xmlData(java.lang.String)
     */
    protected String xmlData(String value) {
        
        char[] buffer = null;
        int len = value.length();
        int srcI = 0;
        int tgtI = 0;
        int copyLength = 0;
        int i = 0;
        
        // Traverse all of the characters in the input String (value)
        while (i < len) {
            
            // Get the codepoint of the character at the index
            // Note that the code point may be two characters long (a supplemental character)
            int cp = value.codePointAt(i);
            
            if (cp > FFFF) {
                // Supplemental Character...Increase index by 2
                // Increase the length of good characters to copy by 2
                i = i+2;
                copyLength = copyLength+2;
            } else {
                // See if the character is invalid
                if ((cp < 0x20 && (REMOVE[cp] > 0)) || // Control Character
                        (cp >= SURROGATE_START && cp <= SURROGATE_END ) ||  // Bad surrogate
                        (cp == FFFF || cp == FFFE)) {  // or illegal character
                    // Flow to here indicates that the character is not allowed.  
                    // The good characters (up to this point) are copied into the buffer.
                    
                    // Note that the buffer is initialized with the original characters.
                    // Thus the buffer copy is always done on the same buffer (saving
                    // both time and space).
                    
                    
                    // Make the buffer on demand
                    if (buffer == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("One or more illegal characterss found.  Codepoint=" + cp);
                        }
                        buffer = value.toCharArray();
                    }
                    
                    // Copy the good characters into the buffer
                    System.arraycopy(buffer, srcI, buffer, tgtI, copyLength);
                    tgtI = tgtI + copyLength;  // Update the target location in the array
                    srcI = i + 1;  // Skip over the current character
                    copyLength = 0; // reset new copy length
                } else {
                    // Valid character, increase copy length
                    copyLength = copyLength+1;
                }
                // Single bit16 character, increase index by 1
                i = i+1;
            }
        }
        
        if (buffer == null) {
            // Normal case, no illegal characters removed..No buffer
            return value;
        } else {
            // Move the final valid characters to the buffer
            // and return a string representing the value
            System.arraycopy(buffer, srcI, buffer, tgtI, copyLength);
            String newValue = new String(buffer, 0, tgtI + copyLength);
            return newValue;
        }
        
    }
}
