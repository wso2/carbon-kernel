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

package org.apache.axis2.transport;

import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisServer;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.util.OptionsValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SimpleAxis2Server extends AxisServer {

    private static final Log log = LogFactory.getLog(SimpleAxis2Server.class);

    int port = -1;

    public static int DEFAULT_PORT = 8080;


    public SimpleAxis2Server (
            String repoLocation,
            String confLocation) throws Exception {
        super(false);
        configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repoLocation,
                        confLocation);
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String repoLocation = null;
        String confLocation = null;

        CommandLineOptionParser optionsParser = new CommandLineOptionParser(args);
        List invalidOptionsList = optionsParser.getInvalidOptions(new OptionsValidator() {
            public boolean isInvalid(CommandLineOption option) {
                String optionType = option.getOptionType();
                return !("repo".equalsIgnoreCase(optionType) || "conf"
                        .equalsIgnoreCase(optionType));
            }
        });

        if ((invalidOptionsList.size() > 0) || (args.length > 4)) {
            printUsage();
            return;
        }

        Map optionsMap = optionsParser.getAllOptions();

        CommandLineOption repoOption = (CommandLineOption) optionsMap
                .get("repo");
        CommandLineOption confOption = (CommandLineOption) optionsMap
                .get("conf");

        log.info("[SimpleAxisServer] Starting");
        if (repoOption != null) {
            repoLocation = repoOption.getOptionValue();
            log.info("[SimpleAxisServer] Using the Axis2 Repository"
                    + new File(repoLocation).getAbsolutePath());
            System.out.println("[SimpleAxisServer] Using the Axis2 Repository"
                    + new File(repoLocation).getAbsolutePath());
        }
        if (confOption != null) {
            confLocation = confOption.getOptionValue();
            System.out
                    .println("[SimpleAxisServer] Using the Axis2 Configuration File"
                            + new File(confLocation).getAbsolutePath());
        }

        try {
            SimpleAxis2Server server = new SimpleAxis2Server(repoLocation, confLocation);
            server.start();
            log.info("[SimpleAxisServer] Started");
            System.out.println("[SimpleAxisServer] Started");
        } catch (Throwable t) {
            log.fatal("[SimpleAxisServer] Shutting down. Error starting SimpleAxisServer", t);
            System.err.println("[SimpleAxisServer] Shutting down. Error starting SimpleAxisServer");
        }
    }

    public static void printUsage() {
        System.out.println(
                "Usage: SimpleAxisServer -repo <repository>  -conf <axis2 configuration file>");
        System.out.println();
        System.exit(1);
    }
}
