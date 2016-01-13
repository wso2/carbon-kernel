/**
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.datasource.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.datasource.common.DataSourceException;
import org.wso2.carbon.datasource.core.DataSourceManager;

/**
 *
 */
@Component(name = "org.wso2.carbon.datasource", immediate = true)
public class DataSourceComponent {

    private static final Log log = LogFactory.getLog(DataSourceComponent.class);


    @Activate
    protected synchronized void activate(ComponentContext ctx) {
        try {
            DataSourceManager.getInstance().initSystemDataSources();
        } catch (DataSourceException e) {
            log.error("Unable to initialize data sources", e);
        }
    }

    @Deactivate
    protected synchronized void deactivate(ComponentContext ctx) {

    }
}
