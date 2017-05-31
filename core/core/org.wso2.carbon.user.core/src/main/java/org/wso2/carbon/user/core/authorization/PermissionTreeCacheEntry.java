package org.wso2.carbon.user.core.authorization;


import java.io.Serializable;


/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

//TODO create base cache entry class
public class PermissionTreeCacheEntry implements Serializable {

    private static final long serialVersionUID = 3730841109352949847L;
    private int treeHash;

    public PermissionTreeCacheEntry(int treeHash) {
        this.treeHash = treeHash;
    }

    public int getPermissionTreeCacheEntry() {
        return treeHash;
    }

}