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

/**
 * General purpose command line options parser.
 * If this is used outside of Axis just remove the Axis specific sections.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

public class OptionsParser {
    private static final Log log = LogFactory.getLog(OptionsParser.class);
    String args[] = null;
    Vector usedArgs = null;

    /**
     * Constructor - just pass in the <b>args</b> from the command line.
     */
    public OptionsParser(String _args[]) throws MalformedURLException {
        if (_args == null) {
            _args = new String[]{};
        }

        args = _args;
        usedArgs = null;
        getUser();
        getPassword();
    }

    public String getPassword() {
        return (isValueSet('w'));
    }

    /**
     * This returns an array of unused args - these are the non-option
     * args from the command line.
     */
    public String[] getRemainingArgs() {
        ArrayList al = null;
        int loop;

        for (loop = 0; loop < args.length; loop++) {
            if ((args[loop] == null) || (args[loop].length() == 0)) {
                continue;
            }

            if (args[loop].charAt(0) == '-') {
                continue;
            }

            if (al == null) {
                al = new ArrayList();
            }

            al.add(args[loop]);
        }

        if (al == null) {
            return (null);
        }

        String a[] = new String[al.size()];

        for (loop = 0; loop < al.size(); loop++) {
            a[loop] = (String) al.get(loop);
        }

        return (a);
    }

    /**
     * This just returns a string with the unprocessed flags - mainly
     * for error reporting - so you can report the unknown flags.
     */
    public String getRemainingFlags() {
        StringBuffer sb = null;
        int loop;

        for (loop = 0; loop < args.length; loop++) {
            if ((args[loop] == null) || (args[loop].length() == 0)) {
                continue;
            }

            if (args[loop].charAt(0) != '-') {
                continue;
            }

            if (sb == null) {
                sb = new StringBuffer();
            }

            sb.append(args[loop].substring(1));
        }

        return ((sb == null)
                ? null
                : sb.toString());
    }

    public String getUser() {
        return (isValueSet('u'));
    }

    /**
     * Returns an int specifying the number of times that the flag was
     * specified on the command line.  Once this flag is looked for you
     * must save the result because if you call it again for the same
     * flag you'll get zero.
     */
    public int isFlagSet(char optChar) {
        int value = 0;
        int loop;
        int i;

        for (loop = 0; (usedArgs != null) && (loop < usedArgs.size()); loop++) {
            String arg = (String) usedArgs.elementAt(loop);

            if (arg.charAt(0) != '-') {
                continue;
            }

            for (i = 0; i < arg.length(); i++) {
                if (arg.charAt(i) == optChar) {
                    value++;
                }
            }
        }

        for (loop = 0; loop < args.length; loop++) {
            if ((args[loop] == null) || (args[loop].length() == 0)) {
                continue;
            }

            if (args[loop].charAt(0) != '-') {
                continue;
            }

            while ((args[loop] != null) && (i = args[loop].indexOf(optChar)) != -1) {
                args[loop] = args[loop].substring(0, i) + args[loop].substring(i + 1);

                if (args[loop].length() == 1) {
                    args[loop] = null;
                }

                value++;

                if (usedArgs == null) {
                    usedArgs = new Vector();
                }

                usedArgs.add("-" + optChar);
            }
        }

        return (value);
    }

    /**
     * Returns a string (or null) specifying the value for the passed
     * option.  If the option isn't there then null is returned.  The
     * option's value can be specified one of two ways:
     * -x value
     * -xvalue
     * Note that:  -ax value
     * is not value (meaning flag 'a' followed by option 'x'.
     * Options with values must be the first char after the '-'.
     * If the option is specified more than once then the last one wins.
     */
    public String isValueSet(char optChar) {
        String value = null;
        int loop;
        int i;

        for (loop = 0; (usedArgs != null) && (loop < usedArgs.size()); loop++) {
            String arg = (String) usedArgs.elementAt(loop);

            if ((arg.charAt(0) != '-') || (arg.charAt(1) != optChar)) {
                continue;
            }

            value = arg.substring(2);

            if (loop + 1 < usedArgs.size()) {
                value = (String) usedArgs.elementAt(++loop);
            }
        }

        for (loop = 0; loop < args.length; loop++) {
            if ((args[loop] == null) || (args[loop].length() == 0)) {
                continue;
            }

            if (args[loop].charAt(0) != '-') {
                continue;
            }

            i = args[loop].indexOf(optChar);

            if (i != 1) {
                continue;
            }

            if (i != args[loop].length() - 1) {

                // Not at end of arg, so use rest of arg as value
                value = args[loop].substring(i + 1);
                args[loop] = args[loop].substring(0, i);
            } else {

                // Remove the char from the current arg
                args[loop] = args[loop].substring(0, i);

                // Nothing after char so use next arg
                if ((loop + 1 < args.length) && (args[loop + 1] != null)) {

                    // Next arg is there and non-null
                    if (args[loop + 1].charAt(0) != '-') {
                        value = args[loop + 1];
                        args[loop + 1] = null;
                    }
                } else {

                    // Next is null or not there - do nothing
                    // value = null ;
                }
            }

            if (args[loop].length() == 1) {
                args[loop] = null;
            }

            // For now, keep looping to get that last on there
            // break ;
        }

        if (value != null) {
            if (usedArgs == null) {
                usedArgs = new Vector();
            }

            usedArgs.add("-" + optChar);

            if (value.length() > 0) {
                usedArgs.add(value);
            }
        }

        return (value);
    }
}
