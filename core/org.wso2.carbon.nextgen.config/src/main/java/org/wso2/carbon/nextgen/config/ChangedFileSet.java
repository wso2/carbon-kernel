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
class ChangedFileSet {

    private boolean changed;
    private List<String> changedFiles;
    private List<String> newFiles;

    ChangedFileSet() {
        this(false, new ArrayList<>(), new ArrayList<>());
    }

    ChangedFileSet(boolean changed, List<String> changedFiles, List<String> newFiles) {

        this.changed = changed;
        this.changedFiles = changedFiles;
        this.newFiles = newFiles;
    }

    boolean isChanged() {

        return changed;
    }

    List<String> getChangedFiles() {

        return changedFiles;
    }

    List<String> getNewFiles() {

        return newFiles;
    }

    void addChangedFile(String path) {
        changed =  true;
        changedFiles.add(path);

    }

    void addNewFile(String path) {
        changed = true;
        newFiles.add(path);
    }
}
