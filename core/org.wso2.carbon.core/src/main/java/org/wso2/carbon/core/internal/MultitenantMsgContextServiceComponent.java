/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * This service component is responsible for loading the list of additional multitenant message context property names
 * from multitenant-msg-context.properties file if given in Carbon config directory. It will be read in Carbon
 * server startup. These property names are the properties which we need to additionally copy in to tenant message
 * context if available in original message context.
 *
 */
@Component(name="org.wso2.carbon.core.internal.MultitenantMsgContextServiceComponent", immediate=true)
public class MultitenantMsgContextServiceComponent {

    private static final Log log = LogFactory.getLog(MultitenantMsgContextServiceComponent.class);

    private MultitenantMsgContextDataHolder dataHolder = MultitenantMsgContextDataHolder.getInstance();

    private static String MULTITENANT_MSG_CONTEXT_PROPERTIES_FILE = "multitenant-msg-context.properties";

    @Activate
    protected void activate(ComponentContext context)
    {
        //load the additional multitenant context property name list from property file if given and add to data holder
        loadTenantMessageContextProperties();
    }
    @Deactivate
    protected void deactivate(ComponentContext context) { }

    /**
     * This method is used to load the additional multitenant context property name list from the
     * multitenant-msg-context.properties file and add the list to the MultitenantMsgContextDataHolder.
     */
    private void loadTenantMessageContextProperties() {
        Properties properties = new Properties();
        List<String> tenantMsgContextProperties = dataHolder.getTenantMsgContextProperties();
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + MULTITENANT_MSG_CONTEXT_PROPERTIES_FILE;
        File file = new File(filePath);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
                //get only the keys of property file (multitenant message context property names)
                for (Object key : properties.keySet()) {
                    tenantMsgContextProperties.add((String) key);
                    if (log.isDebugEnabled()) {
                        log.debug((String) key +
                                " is added to MultitenantMsgContextDataHolder.tenantMsgContextProperties list");
                    }
                }
            } catch (IOException e) {
                log.warn("Error while reading file from " + filePath, e);
            }
        }
    }

}
