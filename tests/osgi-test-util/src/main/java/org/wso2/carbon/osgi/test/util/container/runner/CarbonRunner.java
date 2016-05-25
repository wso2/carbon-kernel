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
package org.wso2.carbon.osgi.test.util.container.runner;

import java.io.File;
import java.util.List;

public class CarbonRunner implements Runner {

    private static final boolean IS_WINDOWS_OS = System.getProperty("os.name").toLowerCase().contains("windows");
    private InternalRunner runner;

    public CarbonRunner() {
        runner = new InternalRunner();
    }

    @Override
    public synchronized void exec(String[] environment, File carbonHome, String javaHome, List<String> javaOpts) {
        Thread thread = new Thread("CarbonJavaRunner") {
            @Override
            public void run() {
                if (javaHome == null) {
                    throw new IllegalStateException("JAVA_HOME is not set.");
                }

                CommandLineBuilder commandLine = new CommandLineBuilder();

                if (IS_WINDOWS_OS) {
                    commandLine.append(carbonHome.getPath() + "/bin/carbon.bat");
                } else {
                    commandLine.append(carbonHome.getPath() + "/bin/carbon.sh");
                }

                javaOpts.forEach(commandLine::append);
                runner.exec(commandLine, carbonHome, environment);
            }
        };
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        runner.shutdown();
    }

}
