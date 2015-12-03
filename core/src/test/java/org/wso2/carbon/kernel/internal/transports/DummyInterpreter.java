/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.internal.transports;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

import java.util.Dictionary;

/**
 * This class acts as a dummy command interpreter for the test case
 * org.wso2.carbon.kernel.internal.transports.TransportMgtCommandProviderTest.
 *
 * @since 5.0.0
 */
public class DummyInterpreter implements CommandInterpreter {
    private String[] transportIdList;
    private int counter;

    public void setTransportIdList(String[] list) {
        transportIdList = list;
        counter = 0;
    }

    public void resetCounter() {
        counter = 0;
    }

    public void setTransportIdListValuesToNull() {
        transportIdList = new String[2];
        transportIdList[0] = null;
        transportIdList[1] = null;
    }

    public void setTransportIdListValuesToEmptyString() {
        transportIdList = new String[2];
        transportIdList[0] = "";
        transportIdList[1] = "";
    }


    @Override
    public String nextArgument() {
        String id = transportIdList[counter];
        counter++;
        return id;
    }

    @Override
    public Object execute(String s) {
        return null;
    }

    @Override
    public void print(Object o) {

    }

    @Override
    public void println() {

    }

    @Override
    public void println(Object o) {

    }

    @Override
    public void printStackTrace(Throwable throwable) {

    }

    @Override
    public void printDictionary(Dictionary<?, ?> dictionary, String s) {

    }

    @Override
    public void printBundleResource(Bundle bundle, String s) {

    }
}
