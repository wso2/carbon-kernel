/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.tomcat.internal;

import javax.servlet.ServletContainerInitializer;

/**
 * @scr.component name="org.wso2.carbon.tomcat.internal.CarbonTomcatServiceComponent" immediate="true"
 *
 * @scr.reference name="sci" interface="javax.servlet.ServletContainerInitializer"
 * cardinality="0..n" policy="dynamic" bind="setServletContainerInitializer" unbind="unsetServletContainerInitializer"
 */
public class CarbonTomcatServiceComponent {

    public void setServletContainerInitializer(ServletContainerInitializer sci) {
        DataHolder.getInstance().addServletContainerInitializer(sci);
    }

    public void unsetServletContainerInitializer(ServletContainerInitializer sci) {
        DataHolder.getInstance().removeServletContainerInitializer(sci);
    }
}
