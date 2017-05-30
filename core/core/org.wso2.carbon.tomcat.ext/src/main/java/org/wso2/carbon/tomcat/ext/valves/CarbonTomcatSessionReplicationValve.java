/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.tomcat.ext.valves;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.valves.ValveBase;
import org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager;

import javax.servlet.ServletException;
import java.io.IOException;


public class CarbonTomcatSessionReplicationValve extends ValveBase {

    public CarbonTomcatSessionReplicationValve() {
        //enable async support
        super(true);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            getNext().invoke(request, response);

        } finally {
            // Call the session manager and replicate sessions
            sendSessionReplicationMessage(request);
        }
    }


    private void sendSessionReplicationMessage(Request request) throws ClusteringFault {
        Context context = request.getContext();
        Session session = request.getSessionInternal(false);
        Manager manager = context.getManager();
        if (manager != null && manager instanceof ClusterManager) {
            CarbonTomcatClusterableSessionManager sessionManager =
                    (CarbonTomcatClusterableSessionManager) manager;
            if (session != null) {
                sessionManager.replicateSessions(session);
            }
        }
    }
}
