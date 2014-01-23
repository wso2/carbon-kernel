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

package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;

public interface ObjectSupplier {
    /**
     * If someone want to write service impl class with interface
     * being there method parameter , then at the time of deserilization
     * this method will provide the impl class for that interface.
     *
     * @param clazz Type
     * @return
     * @throws AxisFault : will throw an exception when something goes wrong
     */
    Object getObject(Class clazz) throws AxisFault;
}
