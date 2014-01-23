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

package org.apache.axis2.maven2.wsdl2code;

/** Data class for specifying URI->Package mappings. */
public class NamespaceURIMapping {
    private String uri, packageName;

    /** Returns the package name, to which the URI shall be mapped. */
    public String getPackageName() {
        return packageName;
    }

    /** Sets the package name, to which the URI shall be mapped. */
    public void setPackageName(String pPackageName) {
        packageName = pPackageName;
    }

    /** Returns the URI, which shall be mapped. */
    public String getUri() {
        return uri;
    }

    /** Sets the URI, which shall be mapped.
     */
    public void setUri(String pUri) {
        uri = pUri;
	}
}
