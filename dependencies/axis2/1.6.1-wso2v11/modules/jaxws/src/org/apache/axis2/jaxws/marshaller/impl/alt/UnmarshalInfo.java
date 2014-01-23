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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import java.util.TreeSet;

/**
 * Information saved from a prior unmarshal that is used
 * to speed up subsequent unmarshalling.
 */
public class UnmarshalInfo {
    // The UnmarshalInfo is saved on the AxisService with the following KEY
    public static final String KEY = "org.apache.axis2.jaxws.marshaller.impl.alt.UnmarshalInfo";
    
    private TreeSet<String> packages;
    private String packagesKey;
    
    /**
     * @param packages
     * @param packagesKey
     */
    UnmarshalInfo(TreeSet<String> packages, String packagesKey) {
        this.packages = packages;
        this.packagesKey = packagesKey;
    }

    /**
     * @return list of packages...used by caller to build a JAXBContext
     */
    public TreeSet<String> getPackages() {
        return packages;
    }

    /**
     * @return packagesKey...used by caller as a key to get JAXBContext
     */
    public String getPackagesKey() {
        return packagesKey;
    }
    
    
}
