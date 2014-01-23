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

package org.apache.axis2.engine.util.profiling;

public class Client {

    public static void main(String[] args) {
        ContextMemoryHandlingUtil contextMemoryHandlingTest = new ContextMemoryHandlingUtil();

        try {
            long initialMemory = Runtime.getRuntime().freeMemory();
            System.out.println("initialMemory = " + initialMemory);
            int numberOfTimes = 0;

            while (true) {
//            while (numberOfTimes < 50) {

                System.out.println("Iterations # = " + ++numberOfTimes);
                contextMemoryHandlingTest.runOnce();
                Runtime.getRuntime().gc();
                System.out.println(
                        "Memory Usage = " + (initialMemory - Runtime.getRuntime().freeMemory()));

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
