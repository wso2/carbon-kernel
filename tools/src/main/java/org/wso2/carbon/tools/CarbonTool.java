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
package org.wso2.carbon.tools;

/**
 * This is a Java interface which has to be implemented by any WSO2 Carbon optional tool.
 *
 * @since 5.1.0
 */
public interface CarbonTool {
    /**
     * Executes the tool based on the arguments provided.
     *
     * @param toolArgs the arguments needed for the tool to function (optional)
     */
    void execute(String... toolArgs);
}
