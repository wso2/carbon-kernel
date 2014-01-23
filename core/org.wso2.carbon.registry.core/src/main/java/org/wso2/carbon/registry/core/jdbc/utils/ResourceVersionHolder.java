/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.registry.core.jdbc.utils;

@Deprecated
public class ResourceVersionHolder {

    private String resourceID;

    private long version;

    public ResourceVersionHolder(String resourceID, long version) {
        this.resourceID = resourceID;
        this.version = version;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object obj) {

        if (obj instanceof ResourceVersionHolder) {

            ResourceVersionHolder rsv = (ResourceVersionHolder) obj;
            return (resourceID.equals(rsv.getResourceID()) && version == rsv.getVersion());

        } else {
            return super.equals(obj);
        }
    }
}
