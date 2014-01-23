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


/**
 * Custom class for supporting XSD data type language language represents natural language
 * identifiers as defined by [RFC 1766]. The value space of language is the set of all strings that
 * are valid language identifiers as defined in the language identification section of [XML 1.0
 * (Second Edition)]. The lexical space of language is the set of all strings that are valid
 * language identifiers as defined in the language identification section of [XML 1.0 (Second
 * Edition)]. The base type of language is token.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#language">XML Schema 3.3.3</a>
 */
public class Language extends Token {

    private static final long serialVersionUID = -4105320293090087959L;

    public Language() {
        super();
    }

    /**
     * ctor for Language
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public Language(String stValue) throws IllegalArgumentException {
        try {
            setValue(stValue);
        }
        catch (IllegalArgumentException e) {
            // recast normalizedString exception as token exception
            throw new IllegalArgumentException(
//                Messages.getMessage("badLanguage00") +
"data=[" + stValue + "]");
        }
    }

    /**
     * Validates the value against the xsd definition. Language-Tag = Primary-tag *( "-" Subtag )
     * Primary-tag = 1*8ALPHA Subtag = 1*8ALPHA TODO
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1766.txt">RFC1766</a>
     */
    public static boolean isValid(String stValue) {
        return true;
    }
}
