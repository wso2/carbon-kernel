/*
*  Copyright (c) $today.year, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.deployment.config;

import java.util.Optional;
import java.util.Properties;

/**
 * JAXB mapping for deployment notifier configuration.
 *
 * @since 5.1.0
 */
public class DeploymentNotifierConfig {

    private boolean jmsPublishingEnabled = false;

    private String destinationJNDIName = "topic0";

    private String destinationType = "topic";

    private String javaNamingFactoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";

    private String javaNamingProviderURL = "conf/jndi.properties";

    private Optional<String> jmsUsername = Optional.empty();

    private Optional<String> jmsPassword = Optional.empty();

    private String connectionFactoryJNDIName = "TopicConnectionFactory";

    private Properties staticMessageContent = new Properties();

    public boolean isJmsPublishingEnabled() {
        return jmsPublishingEnabled;
    }

    public String getDestinationJNDIName() {
        return destinationJNDIName;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public Optional<String> getJmsUsername() {
        return jmsUsername;
    }

    public Optional<String> getJmsPassword() {
        return jmsPassword;
    }

    public String getJavaNamingFactoryInitial() {
        return javaNamingFactoryInitial;
    }

    public String getJavaNamingProviderURL() {
        return javaNamingProviderURL;
    }

    public String getConnectionFactoryJNDIName() {
        return connectionFactoryJNDIName;
    }

    public Properties getStaticMessageContent() {
        return Optional.ofNullable(staticMessageContent).orElse(new Properties());
    }
}
