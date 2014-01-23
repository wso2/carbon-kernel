/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.deployment;

/**
 * Interface for OSGi services which provide DeploymentSynchronization.
 * Deployment synchronization refers to synchronizing artifact deployment repositories
 * across multiple nodes in a cluster
 */
public interface DeploymentSynchronizer {

    /**
     * Do an update of the deployment repository
     *
     * @param tenantId The ID of the tenant which has to be synchronized
     * @return true if files were actually updated, false otherwise
     */
    boolean update(int tenantId);

    /**
     * Do a commit of the deployment repository
     * 
     * @param tenantId The ID of the tenant which has to be synchronized
     * @return true if file changes were committed, false otherwise
     */
    boolean commit(int tenantId);

    /**
     * Checks whether a deployment synchronizer has been engaged on the specified path
     *
     * @param filePath Location of the repository in file system
     * @return true if a DeploymentSynchronizer instance has been created on the specified path
     */
    public boolean synchronizerExists(String filePath);

    /**
     * Check whether auto commit has been engaged on the specified repository
     *
     * @param filePath Location of the repository in file system
     * @return true if auto commit is enabled on repository and false otherwise
     */
    public boolean isAutoCommitOn(String filePath);

    /**
     * Check whether auto checkout has been engaged on the specified repository
     *
     * @param filePath Location of the repository in file system
     * @return true if auto checkout is enabled on repository and false otherwise
     */
    public boolean isAutoCheckoutOn(String filePath);

    /**
     * Gets the time at which the commit operation was last invoked on the given repository
     *
     * @param filePath Location of the repository in file system
     * @return a long timestamp value
     */
    public long getLastCommitTime(String filePath);

    /**
     * Gets the time at which the checkout operation was last invoked on the given repository
     *
     * @param filePath Location of the repository in file system
     * @return a long timestamp value
     */
    public long getLastCheckoutTime(String filePath);

    /**
     * Invoke the checkout operation on the specified repository
     *
     * @param filePath Location of the repository in file system
     * @return true if files were checked out or updated, false otherwise
     */
    public boolean checkout(String filePath);

    /**
     * Invoke the checkout operation on the specified repository, with given depth
     *
     * @param filePath Location of the repository in file system
     * @param depth Depth given to check-out, eg 0 - empty, 3 - infinite
     * @return true if files were checked out or updated, false otherwise
     */
    public boolean checkout(String filePath, int depth);


    /**
     * Invoke the update operation on the specified file in the repository, with given depth
     *
     * @param rootPath - root path of the repository of which the dep-sychronizer is registered
     * @param filePath - location of the file in the repository
     * @param depth Depth given to update, (eg 0 - empty, 3 - infinite)
     * @return true if files were updated, false otherwise
     */
    public boolean update(String rootPath, String filePath, int depth);


    /**
     * Invoke the commit operation on the specified repository.
     * This will commit the whole repository at given filePath
     *
     * @param filePath Location of the repository in file system
     * @return true if file changes were committed, false otherwise
     */
    public boolean commit(String filePath);

    /**
     * Invoke the commit operation on the specified repository at rootPath, with the given
     * filePath to commit. This will only commit the filePath in the given repository.
     *
     * @param rootPath repo path at which the dep synch is registered
     * @param filePath Location of the repository in file system
     * @return true if file changes were committed, false otherwise
     */
    public boolean commit(String rootPath, String filePath);
}
