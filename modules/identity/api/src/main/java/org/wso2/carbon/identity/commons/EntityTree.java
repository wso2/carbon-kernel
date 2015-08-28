/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.commons;

import java.util.Collections;
import java.util.List;

public class EntityTree {

    private EntityIdentifier node;
    private EntityIdentifier parentNode;
    private List<EntityTree> children;

    /**
     * @param node
     * @param treeList
     */
    public EntityTree(EntityIdentifier node, List<EntityTree> treeList) {
        this.node = node;
        this.children = treeList;
    }

    /**
     * @param parentIdentifier
     * @param enitityIdentifier
     * @param treeList
     */
    public EntityTree(EntityIdentifier parentIdentifier, EntityIdentifier enitityIdentifier,
                      List<EntityTree> treeList) {
        this.node = enitityIdentifier;
        this.children = treeList;
        this.parentNode = parentIdentifier;
    }

    /**
     * @return
     */
    public EntityIdentifier getNode() {
        return node;
    }

    /**
     * @return
     */
    public List<EntityTree> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * @return
     */
    public EntityIdentifier getParentNode() {
        return parentNode;
    }

}