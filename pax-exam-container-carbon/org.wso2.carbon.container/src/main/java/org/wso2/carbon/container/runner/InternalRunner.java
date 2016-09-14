/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.container.runner;
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

import org.ops4j.io.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal runner to create the framework process to start the distribution.
 */
public class InternalRunner {

    private static final Logger logger = LoggerFactory.getLogger(InternalRunner.class);

    private final Object shutdownHookMonitor = new Object();
    private final Object frameworkProcessMonitor = new Object();
    private Process frameworkProcess;
    private Thread shutdownHook;

    /**
     * Execute the command and create a process for that.
     *
     * @param commandLine command to be executed
     * @param envOptions  environment options
     */
    public synchronized void exec(CommandLineBuilder commandLine, final String[] envOptions) {
        if (frameworkProcess != null) {
            throw new IllegalStateException("Server is already started");
        }

        try {
            frameworkProcess = Runtime.getRuntime()
                    .exec(commandLine.toArray(), createEnvironmentVars(envOptions), null);
        } catch (IOException e) {
            throw new IllegalStateException("Could not start up the process", e);
        }

        shutdownHook = createShutdownHook(frameworkProcess);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        waitForExit();
    }

    private String[] createEnvironmentVars(String[] envOptions) {
        List<String> env = new ArrayList<>();
        Map<String, String> getenv = System.getenv();
        env.addAll(getenv.keySet().stream().map(key -> key + "=" + getenv.get(key)).collect(Collectors.toList()));
        if (envOptions != null) {
            Collections.addAll(env, envOptions);
        }
        return env.toArray(new String[env.size()]);
    }

    /**
     * Properly destroy the process.
     */
    public synchronized void shutdown() {
        try {
            if (shutdownHook != null) {
                synchronized (shutdownHookMonitor) {
                    if (shutdownHook != null) {
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        frameworkProcess = null;
                        shutdownHook.start();
                        shutdownHook = null;
                    }
                }
            }
        } catch (IllegalStateException ignore) {
            logger.error("Already started the process.", ignore);
        }
    }

    /**
     * Wait till the framework process exits.
     */
    private void waitForExit() {
        if (shutdownHook != null) {
            synchronized (shutdownHookMonitor) {
                if (shutdownHook != null) {
                    synchronized (frameworkProcessMonitor) {
                        try {
                            if (frameworkProcess != null) {
                                frameworkProcess.waitFor();
                            }
                            shutdown();
                        } catch (Throwable e) {
                            shutdown();
                        }
                    }
                }
            }
        }
    }

    /**
     * Create helper thread to safely shutdown the external framework process.
     *
     * @param process framework process
     * @return stream handler
     */
    private Thread createShutdownHook(final Process process) {
        final Pipe errPipe = new Pipe(process.getErrorStream(), System.err).start("Error pipe");
        final Pipe outPipe = new Pipe(process.getInputStream(), System.out).start("Out pipe");
        final Pipe inPipe = new Pipe(process.getOutputStream(), System.in).start("In pipe");

        return new Thread(() -> {
            inPipe.stop();
            outPipe.stop();
            errPipe.stop();

            try {
                process.destroy();
            } catch (Exception e) {
                // ignore if already shutting down.
                logger.error("Already shutting down the process.", e);
            }
        });
    }

}
