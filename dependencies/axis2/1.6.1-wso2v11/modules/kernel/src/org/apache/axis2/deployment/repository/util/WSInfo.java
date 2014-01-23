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


package org.apache.axis2.deployment.repository.util;

import org.apache.axis2.deployment.Deployer;

public class WSInfo {
    private String fileName;
    private long lastModifiedDate;

    public static final int TYPE_SERVICE = 0;
    public static final int TYPE_MODULE = 1;
    public static final int TYPE_CUSTOM = 2;

    /**
     * To check whether the file is a module or a service
     */
    private int type = TYPE_SERVICE;

    private Deployer deployer;

    public WSInfo(String fileName, long lastModifiedDate, Deployer deployer ,int type) {
        this.fileName = fileName;
        this.lastModifiedDate = lastModifiedDate;
        this.deployer = deployer;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getType() {
        return type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLastModifiedDate(long lastmodifieddate) {
        this.lastModifiedDate = lastmodifieddate;
    }
    
    public Deployer getDeployer() {
        return deployer;
    }
}
