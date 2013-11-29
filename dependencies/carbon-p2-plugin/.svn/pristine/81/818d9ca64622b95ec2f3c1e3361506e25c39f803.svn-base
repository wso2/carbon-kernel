/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.maven.p2.generate.utils;


import java.util.Properties;

public class PropertyReplacer {
    // States of the state machine
    private enum State {
        NORMAL, SEEN_DOLLAR, WITHIN_BRACKET
    }

    private PropertyReplacer() {
        // you can't instantiate this class
    }


    public static String replaceProperties(final String origString, final Properties props) {

        final char[] chars = origString.toCharArray();
        StringBuffer buffer = new StringBuffer();
        boolean properties = false;
        State state = State.NORMAL;
        int start = 0;
        for (int i = 0; i < chars.length; ++i) {

            char c = chars[i];
            if (c == '$' && state != State.WITHIN_BRACKET) {  // found a "$"
                state = State.SEEN_DOLLAR;
            } else if (c == '{' && state == State.SEEN_DOLLAR) {  // found a "{" after "$"
                buffer.append(origString.substring(start, i - 1));
                state = State.WITHIN_BRACKET;
                start = i - 1;
            } else if (state == State.SEEN_DOLLAR) {   // just a single "$" ; no brackets
                state = State.NORMAL;
            } else if (c == '}' && state == State.WITHIN_BRACKET) {  // its a  ${property} sequence extract the value
                String key = origString.substring(start + 2, i);
                String value = props.getProperty(key);
                if (value != null) {      // if the value of the property found; replace
                    properties = true;
                    buffer.append(value);
                } else {                  // if the value of the property not give do not replace
                    buffer.append("${");
                    buffer.append(key);
                    buffer.append('}');
                }
                 start = i + 1;
                 state = State.NORMAL;
            }
        }
        // No properties hence returning the original string
        if (properties == false) {
            return origString;
        }
        // Collect the trailing characters
        if (start != chars.length) {
            buffer.append(origString.substring(start, chars.length));
        }
        return buffer.toString();
    }

}
