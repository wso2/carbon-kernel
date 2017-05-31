/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.core.services.callback;

import org.wso2.carbon.registry.core.Registry;

import java.util.ArrayList;

public class LoginSubscriptionManagerServiceImpl implements LoginSubscriptionManagerService {
    ArrayList<LoginListener> subscriptions;

    public LoginSubscriptionManagerServiceImpl() {
        subscriptions = new ArrayList<LoginListener>();
    }

    public void subscribe(LoginListener listener) {
        subscriptions.add(listener);
    }

    public void triggerEvent(Registry configRegistry, String username, int tenantId, String tenantDomain) {
        for (int i = 0; i < subscriptions.size(); i ++) {
            LoginEvent event = new LoginEvent();
            event.setTenantDomain(tenantDomain);
            event.setUsername(username);
            event.setTenantId(tenantId);
            subscriptions.get(i).onLogin(configRegistry, event);
        }
    }
}
