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

package org.wso2.carbon.kernel.internal.tenant;

import org.wso2.carbon.kernel.tenant.PrivilegedTenant;
import org.wso2.carbon.kernel.tenant.Tenant;

import java.util.Date;
import java.util.List;

public class DefaultTenant implements PrivilegedTenant {


    @Override
    public void setID(String id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDomain(String domain) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setName(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDescription(String description) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCreatedDate(Date date) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAdminUsername(String adminUsername) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAdminUserEmailAddress(String emailAddress) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setParent(PrivilegedTenant tenant) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addChild(PrivilegedTenant tenant) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PrivilegedTenant removeChild(String tenantID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setDepthOfHierarchy(int depthOfHierarchy) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getID() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDomain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date getCreatedDate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAdminUsername() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAdminUserEmailAddress() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Tenant getParent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Tenant getChild(String tenantID) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List getChildren() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getDepthOfHierarchy() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
