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
package org.apache.axis2.transport.base.threads;

/**
 * If certain Threads in a ThreadPool need to be cleaned up before being reused, an implementation
 * of this interface should be provided so that the relevant cleaning up could be performed.
 * 
 * An instance of the implementation class of this interface should be added to
 * {@link ThreadCleanupContainer#addThreadCleanup(ThreadCleanup)}
 *
 * Typical items that will be cleaned up will include ThreadLocal variables
 * 
 * @see ThreadCleanupContainer
 */
public interface ThreadCleanup {

    /**
     * Cleanup the Threads
     */
    void cleanup();
}
