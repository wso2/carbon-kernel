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
package sample.rmi.server;

import java.util.List;
import java.util.Map;


public class Service4 implements Service4Interface {

    public Object method1(Object param1){
        return param1;
    }

    public List method2(List param1, List param2){
        if (param1 != null){
            if (param2 != null){
                param1.addAll(param2);
                return param1;
            } else {
                return param1;
            }
        } else {
            return param2;
        }
    }

    public String[] method3(String param1,String param2,String param3){
        String[] returnArray = new String[3];
        returnArray[0] = param1;
        returnArray[1] = param2;
        returnArray[2] = param3;
        return returnArray;
    }

    public Map method4(Map param1) {
        return param1;
    }


}
