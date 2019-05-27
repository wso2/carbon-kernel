/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.nextgen.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of Changed files model.
 */
public class ChangedFileSet {

    private boolean changed;
    private List changedFiles = new ArrayList();
    private List newFiles = new ArrayList();

    public ChangedFileSet() {

    }

    public ChangedFileSet(boolean changed, List changedFiles, List newFiles) {

        this.changed = changed;
        this.changedFiles = changedFiles;
        this.newFiles = newFiles;
    }

    public boolean isChanged() {

        return changed;
    }

    public List getChangedFiles() {

        return changedFiles;
    }

    public List getNewFiles() {

        return newFiles;
    }

    public void addChangedFile(String path) {
        changed =  true;
        changedFiles.add(path);

    }

    public void addNewFile(String path) {
        changed = true;
        newFiles.add(path);
    }
}
