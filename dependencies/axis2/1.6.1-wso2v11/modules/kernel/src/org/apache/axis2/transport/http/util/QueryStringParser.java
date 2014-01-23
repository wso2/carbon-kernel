/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.http.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * Parser for URL query strings.
 */
public class QueryStringParser {
    private final String queryString;
    
    /**
     * The position of the current parameter.
     */
    private int paramBegin;
    private int paramEnd;
    private int paramNameEnd;
    private String paramName;
    private String paramValue;

    /**
     * Construct a parser from the given URL query string.
     * 
     * @param queryString the query string, i.e. the part of the URL starting
     *                    after the '?' character
     */
    public QueryStringParser(String queryString) {
        this.queryString = queryString;
    }
    
    /**
     * Move to the next parameter in the query string.
     * 
     * @return <code>true</code> if a parameter has been found; 
     * <code>false</code> if there are no more parameters
     */
    public boolean next() {
        int len = queryString.length();
        if (paramEnd == len) {
            return false;
        }
        paramBegin = paramEnd == 0 ? 0 : paramEnd+1;
        int idx = queryString.indexOf('&', paramBegin);
        paramEnd = idx == -1 ? len : idx;
        idx = queryString.indexOf('=', paramBegin);
        paramNameEnd = idx == -1 || idx > paramEnd ? paramEnd : idx;
        paramName = null;
        paramValue = null;
        return true;
    }
    
    /**
     * Search for a parameter with a name in a given collection.
     * This method iterates over the parameters until a parameter with
     * a matching name has been found. Note that the current parameter is not
     * considered.
     * 
     * @param names
     * @return
     */
    public boolean search(Collection<String> names) {
        while (next()) {
            if (names.contains(getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the name of the current parameter.
     * Calling this method is only allowed if {@link #next()} has been called
     * previously and the result of this call was <code>true</code>. Otherwise the
     * result of this method is undefined.
     * 
     * @return the name of the current parameter
     */
    public String getName() {
        if (paramName == null) {
            paramName = queryString.substring(paramBegin, paramNameEnd);
        }
        return paramName;
    }
    
    /**
     * Get the value of the current parameter.
     * Calling this method is only allowed if {@link #next()} has been called
     * previously and the result of this call was <code>true</code>. Otherwise the
     * result of this method is undefined.
     * 
     * @return the decoded value of the current parameter
     */
    public String getValue() {
        if (paramValue == null) {
            if (paramNameEnd == paramEnd) {
                return null;
            }
            try {
                paramValue = URIEncoderDecoder.decode(queryString.substring(paramNameEnd+1, paramEnd));
            } catch (UnsupportedEncodingException ex) {
                // TODO: URIEncoderDecoder.decode should be changed to not throw
                // UnsupportedEncodingException since this actually never happens (the charset
                // being looked up is UTF-8 which always exists).
                throw new Error(ex);
            }
        }
        return paramValue;
    }
}
