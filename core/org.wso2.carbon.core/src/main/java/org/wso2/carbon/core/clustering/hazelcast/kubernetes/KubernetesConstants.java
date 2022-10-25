/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.core.clustering.hazelcast.kubernetes;

/**
 * Constants used by the Kubernetes membership schema implementation.
 */
public class KubernetesConstants {

    public static final String NAMESPACE_PROPERTY = "KUBERNETES_NAMESPACE";
    public static final String NAMESPACE = "namespace";

    public static final String SERVICE_NAME_PROPERTY = "KUBERNETES_SERVICES";
    public static final String SERVICE_NAME = "service-name";

    public static final String SERVICE_PORT = "service-port";
    public static final String DEFAULT_SERVICE_PORT = "4000";

    public static final int MEMBER_JOIN_WAIT_TIME = 5000;
}
