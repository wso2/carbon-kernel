/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.authorization;

import org.junit.Assert;
import org.wso2.carbon.user.core.BaseTestCase;
import org.wso2.carbon.user.core.util.UserCoreUtil;

public class HybridRoleAuthorizationTest extends BaseTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testAuthorizationForActions() throws Exception {
        String roleName = "Internal/testRole";
        String resourceId = "/permission/test/testPermission";
        String actionConsume = "consume";
        String actionUIExecute = "ui.execute";


        TreeNode treeNode = new TreeNode(resourceId);
        treeNode.authorizeRole(roleName, PermissionTreeUtil.actionToPermission(actionConsume));
        Assert.assertTrue(treeNode.isRoleAuthorized(roleName, PermissionTreeUtil.actionToPermission(actionConsume)));
        Assert.assertNull(treeNode.isRoleAuthorized(roleName, PermissionTreeUtil.actionToPermission(actionUIExecute)));
    }


    public void testLowerCaseDomainName() throws Exception {
        String roleName = "Internal/testRole";
        String resourceId = "/permission/test/testPermission";
        String actionConsume = "consume";


        TreeNode treeNode = new TreeNode(resourceId);
        treeNode.authorizeRole(roleName, PermissionTreeUtil.actionToPermission(actionConsume));

        String modifiedRoleName = UserCoreUtil.addDomainToName(UserCoreUtil.removeDomainFromName(roleName),
                UserCoreUtil.extractDomainFromName(roleName).toLowerCase());

        Assert.assertNull(treeNode.isRoleAuthorized(modifiedRoleName, PermissionTreeUtil.actionToPermission(actionConsume)));

    }

    public void testUpperCaseDomainName() throws Exception {
        String roleName = "Internal/testRole";
        String resourceId = "/permission/test/testPermission";
        String actionConsume = "consume";


        TreeNode treeNode = new TreeNode(resourceId);
        treeNode.authorizeRole(roleName, PermissionTreeUtil.actionToPermission(actionConsume));

        String modifiedRoleName = UserCoreUtil.addDomainToName(UserCoreUtil.removeDomainFromName(roleName),
                UserCoreUtil.extractDomainFromName(roleName).toUpperCase());

        Assert.assertNull(treeNode.isRoleAuthorized(modifiedRoleName, PermissionTreeUtil.actionToPermission(actionConsume)));
    }

    public void testLowerCaseRoleName() throws Exception {
        String roleName = "Internal/testRole";
        String resourceId = "/permission/test/testPermission";
        String actionConsume = "consume";


        TreeNode treeNode = new TreeNode(resourceId);
        treeNode.authorizeRole(roleName, PermissionTreeUtil.actionToPermission(actionConsume));

        Assert.assertNull(treeNode.isRoleAuthorized(roleName.toLowerCase(), PermissionTreeUtil.actionToPermission
                (actionConsume)));
    }

    public void testUpperCaseRoleName() throws Exception {
        String roleName = "Internal/testRole";
        String resourceId = "/permission/test/testPermission";
        String actionConsume = "consume";


        TreeNode treeNode = new TreeNode(resourceId);
        treeNode.authorizeRole(roleName, PermissionTreeUtil.actionToPermission(actionConsume));

        Assert.assertNull(treeNode.isRoleAuthorized(roleName.toUpperCase(), PermissionTreeUtil.actionToPermission
                (actionConsume)));
    }

}
