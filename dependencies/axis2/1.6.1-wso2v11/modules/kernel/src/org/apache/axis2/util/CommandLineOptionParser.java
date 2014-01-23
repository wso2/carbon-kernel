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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandLineOptionParser implements CommandLineOptionConstants {

    //states
    private static int STARTED = 0;
    private static int NEW_OPTION = 1;
    private static int SUB_PARAM_OF_OPTION = 2;

    private Map commandLineOptions;

    public CommandLineOptionParser(Map commandLineOptions) {
        this.commandLineOptions = commandLineOptions;
    }

    public CommandLineOptionParser(String[] args) {
        this.commandLineOptions = this.parse(args);

    }

    /**
     * Return a list with <code>CommandLineOption</code> objects
     *
     * @param args
     * @return CommandLineOption List
     */
    private Map parse(String[] args) {
        Map commandLineOptions = new HashMap();

        if (0 == args.length) {
            return commandLineOptions;
        }

        //State 0 means started
        //State 1 means earlier one was a new -option
        //State 2 means earlier one was a sub param of a -option

        int state = STARTED;
        ArrayList optionBundle = null;
        String optionType = null;
        CommandLineOption commandLineOption;

        for (int i = 0; i < args.length; i++) {

            if (args[i].startsWith("-")) {
                if (STARTED == state) {
                    // fresh one
                    state = NEW_OPTION;
                    optionType = args[i];
                } else if (SUB_PARAM_OF_OPTION == state || NEW_OPTION == state) {
                    // new one but old one should be saved
                    commandLineOption =
                            new CommandLineOption(optionType, optionBundle);
                    commandLineOptions.put(commandLineOption.getOptionType(),
                                           commandLineOption);
                    state = NEW_OPTION;
                    optionType = args[i];
                    optionBundle = null;

                }
            } else {
                if (STARTED == state) {
                    commandLineOption =
                            new CommandLineOption(
                                    CommandLineOptionConstants.SOLE_INPUT,
                                    args);
                    commandLineOptions.put(commandLineOption.getOptionType(),
                                           commandLineOption);
                    return commandLineOptions;

                } else if (NEW_OPTION == state) {
                    optionBundle = new ArrayList();
                    optionBundle.add(args[i]);
                    state = SUB_PARAM_OF_OPTION;

                } else if (SUB_PARAM_OF_OPTION == state) {
                    optionBundle.add(args[i]);
                }

            }


        }

        commandLineOption = new CommandLineOption(optionType, optionBundle);
        commandLineOptions.put(commandLineOption.getOptionType(), commandLineOption);
        return commandLineOptions;
    }

    public Map getAllOptions() {
        return this.commandLineOptions;
    }

    public List getInvalidOptions(OptionsValidator validator) {
        List faultList = new ArrayList();
        Iterator iterator = this.commandLineOptions.values().iterator();
        while (iterator.hasNext()) {
            CommandLineOption commandLineOption = ((CommandLineOption) (iterator.next()));
            if (validator.isInvalid(commandLineOption)) {
                faultList.add(commandLineOption);
            }
        }

        return faultList;
    }


}