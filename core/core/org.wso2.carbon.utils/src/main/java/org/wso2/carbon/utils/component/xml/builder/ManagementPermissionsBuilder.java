/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.utils.component.xml.builder;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.utils.component.xml.config.ComponentConfig;
import org.wso2.carbon.utils.component.xml.config.ManagementPermission;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManagementPermissionsBuilder extends ComponentConfigBuilder {

    public static final String LOCALNAME_MGT_PERMISSIONS = "ManagementPermissions";
    public static final String LOCALNAME_MGT_PERMISSION = "ManagementPermission";
    public static final String LOCALNAME_DISPLAY_NAME = "DisplayName";
    public static final String LOCALNAME_RESOURCE_ID = "ResourceId";

    public ComponentConfig[] build(OMElement omElement) throws CarbonException {
        List<ManagementPermission> permissionList = new ArrayList<ManagementPermission>();
        for (Iterator iterator = omElement.getChildrenWithName(new QName(NS_WSO2CARBON,
                LOCALNAME_MGT_PERMISSION)); iterator.hasNext();) {
            OMElement configElement = (OMElement) iterator.next();

            // display name
            OMElement displayNameElement = configElement.getFirstChildWithName(new QName(
                    NS_WSO2CARBON, LOCALNAME_DISPLAY_NAME));
            String name = null;
            if (displayNameElement != null) {
                name = displayNameElement.getText().trim();
            }

            if (name == null) {
                throw new CarbonException("Mandatory attribute DisplayName "
                        + "does not exist or is empty in the component.xml");
            }
            
            // name element
            OMElement resourceIdElement = configElement.getFirstChildWithName(new QName(
                    NS_WSO2CARBON, LOCALNAME_RESOURCE_ID));
            String resourceId = null;
            if (resourceIdElement != null) {
                resourceId = resourceIdElement.getText().trim();
            }

            if (resourceId == null) {
                throw new CarbonException("Mandatory attribute EJBApplicationServer/Name entry "
                        + "does not exist or is empty in the component.xml");
            }

            ManagementPermission uiPermission = new ManagementPermission(name, resourceId);

            permissionList.add(uiPermission);
        }

        if (permissionList.size() == 0) {
            return null;
        } else {
            ManagementPermission[] uiPermissions = new ManagementPermission[permissionList.size()];
            return permissionList.toArray(uiPermissions);
        }
    }

    public String getLocalNameOfComponentConfigElement() {
        return LOCALNAME_MGT_PERMISSIONS;
    }

}
