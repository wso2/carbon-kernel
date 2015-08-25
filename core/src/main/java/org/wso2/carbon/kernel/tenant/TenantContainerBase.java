/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.kernel.tenant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation of the TenantContainer interface. This class provide generic implementations of the methods.
 *
 * @see TenantContainer
 * @since 5.0.0
 */
public class TenantContainerBase implements TenantContainer {

    private String id;
    private TenantContainer parent = null;
    private Map<String, TenantContainer> children = new HashMap<>();
    private int depthOfHierarchy = -1;

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public TenantContainer getParent() {
        return parent;
    }

    @Override
    public void setParent(TenantContainer parent) {
        this.parent = parent;
        // TODO Notify.
    }

    @Override
    public Map<String, TenantContainer> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    @Override
    public int getDepthOfHierarchy() {
        return depthOfHierarchy;
    }

    @Override
    public void setDepthOfHierarchy(int depthOfHierarchy) {
        this.depthOfHierarchy = depthOfHierarchy;
    }

    @Override
    public void addChild(TenantContainer child) {
        children.put(child.getID(), child);
        child.setParent(this);
        //TODO Notify
    }

    @Override
    public void unsetParent(TenantContainer parent) {
        this.parent = null;
        // TODO Notify.
    }

    @Override
    public TenantContainer removeChild(TenantContainer child) {
        return children.remove(child.getID());
        // TODO Notify.
    }
}
