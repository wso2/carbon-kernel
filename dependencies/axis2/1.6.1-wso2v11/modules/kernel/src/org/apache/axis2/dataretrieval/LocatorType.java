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

package org.apache.axis2.dataretrieval;

/**
 * Defines Data Locator types as described below:
 * DEFAULT_AXIS - Default Axis2 data locator
 * GLOBAL_LEVEL - Global level data locator i.e.,plug-in data locator registered
 * using <dataLocator> element in axis2.xml.
 * GLOBAL_DIALECT - Global level dialect specfic data locator i.e.,plug-in
 * data locator registered using <dialectLocator> element in axis2.xml.
 * SERVICE_LEVEL - Service level data locator i.e. plug-in data locator registered
 * using <dataLocator> element in services.xml.
 * SERVICE_DIALECT - Service level dialect specific data locator i.e. plug-in data locator registered
 * using <dialectLocator> element in services.xml.
 */

public class LocatorType {
    int type;
    public final static LocatorType DEFAULT_AXIS = new LocatorType(0);
    public final static LocatorType GLOBAL_LEVEL = new LocatorType(1);
    public final static LocatorType GLOBAL_DIALECT = new LocatorType(2);
    public final static LocatorType SERVICE_LEVEL = new LocatorType(3);
    public final static LocatorType SERVICE_DIALECT = new LocatorType(4);


    protected LocatorType(int intype) {
        this.type = intype;
    }

    public int getType() {
        return type;
    }


}
