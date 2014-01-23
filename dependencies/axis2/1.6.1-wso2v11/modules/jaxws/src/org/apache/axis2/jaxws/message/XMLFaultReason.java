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

package org.apache.axis2.jaxws.message;

import java.util.Locale;

/** Agnostic representation of a Fault Reason/faultstring. See @XMLFault */
public class XMLFaultReason {
    String text;
    String lang;

    /**
     * A Fault Reason has the reason text and language
     *
     * @param text
     * @param lang
     */
    public XMLFaultReason(String text, String lang) {
        this.text = text;
        this.lang = lang;
    }

    /**
     * A Fault Reason with the default language
     *
     * @param text
     */
    public XMLFaultReason(String text) {
        this(text, getDefaultLang());
    }

    /** @return Returns the lang. */
    public String getLang() {
        return lang;
    }

    /** @return Returns the text. */
    public String getText() {
        return text;
    }

    /** @return the IS0 639 language identifier for the default locale */
    public static String getDefaultLang() {
        Locale locale = Locale.getDefault();
        // The spec indicates to use RFC 3066, which uses the values defined by ISO 639,
        // which is what getLanguage() returns.
        return locale.getLanguage();
    }

}
