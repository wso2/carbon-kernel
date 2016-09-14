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

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Runner class to execute the carbon.sh.
 */
public class CarbonRunner implements Runner {

    private static final boolean IS_WINDOWS_OS = System.getProperty("os.name").toLowerCase(Locale.getDefault())
            .contains("windows");
    private InternalRunner runner;

    public CarbonRunner() {
        runner = new InternalRunner();
    }

    @Override
    public synchronized void exec(String[] environment, Path carbonHome, List<String> options) {
        Thread thread = new Thread("CarbonRunner") {
            @Override
            public void run() {
                CommandLineBuilder commandLine = new CommandLineBuilder();

                if (IS_WINDOWS_OS) {
                    commandLine.append("cmd.exe").append("/c").append(carbonHome.toAbsolutePath() + "/bin/carbon.bat");
                } else {
                    commandLine.append(carbonHome.toAbsolutePath() + "/bin/carbon.sh");
                }

                commandLine.append(options.toArray(new String[options.size()]));
                runner.exec(commandLine, environment);
            }
        };
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        runner.shutdown();
    }
}
