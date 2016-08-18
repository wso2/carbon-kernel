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
package org.wso2.carbon.container.options;

import org.ops4j.pax.exam.Option;

/**
 * Start the distribution in debug mode.
 */
public class DebugOption implements Option {

    private int port;

    /**
     * Activates debugging on the Carbon container using the standard 5005 port.
     */
    public DebugOption() {
        port = 5005;
    }

    /**
     * Activates debugging on the Carbon container using the given port.
     *
     * @param port remote debugger port
     */
    public DebugOption(int port) {
        this.port = port;
    }

    /**
     * @return the string contains the debug configuration
     */
    public String getDebugConfiguration() {
        return String.format("-debug %s", port);
    }

}
