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
package org.apache.axis2.clustering;

/**
 * This is the interface which will be notified when memership changes.
 * If some specific activities need to be performed when membership changes occur,
 * you can provide an implementation of this interface in the axis2.xml
 */
public interface MembershipListener {

    /**
     * Method which will be called when a member is added
     *
     * @param member The member which was added
     * @param isLocalMemberCoordinator true - if the local member is the coordinator
     */
    public void memberAdded(Member member, boolean isLocalMemberCoordinator);

    /**
     * Method which will be called when a member dissapears
     *
     * @param member The member which disappeared
     * @param isLocalMemberCoordinator true - if the local member is the coordinator
     */
    public void memberDisappeared(Member member,  boolean isLocalMemberCoordinator);
}
