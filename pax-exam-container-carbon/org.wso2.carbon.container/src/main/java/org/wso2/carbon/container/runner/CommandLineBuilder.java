/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.container.runner;

/**
 * Command line builder to build command line commands.
 */
public class CommandLineBuilder {

    /**
     * The command line array.
     */
    private String[] commandLine;

    /**
     * Creates a new command line builder.
     */
    public CommandLineBuilder() {
        commandLine = new String[0];
    }

    /**
     * Appends an array of strings to command line.
     *
     * @param segments array to append
     * @return CommandLineBuilder for fluent api
     */
    public CommandLineBuilder append(final String[] segments) {
        if (segments != null && segments.length > 0) {
            final String[] command = new String[commandLine.length + segments.length];
            System.arraycopy(commandLine, 0, command, 0, commandLine.length);
            System.arraycopy(segments, 0, command, commandLine.length, segments.length);
            commandLine = command;
        }
        return this;
    }

    /**
     * Appends a string to command line.
     *
     * @param segment string to append
     * @return CommandLineBuilder for fluent api
     */
    public CommandLineBuilder append(final String segment) {
        if (segment != null && !segment.isEmpty()) {
            return append(new String[] { segment });
        }
        return this;
    }

    /**
     * Returns the command line.
     *
     * @return command line
     */
    public String[] toArray() {
        return commandLine.clone();
    }

}
